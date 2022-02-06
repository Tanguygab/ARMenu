package io.github.tanguygab.armenu;

import io.github.tanguygab.armenu.actions.Action;
import io.github.tanguygab.armenu.commands.*;
import io.github.tanguygab.armenu.events.EventsManager;
import io.github.tanguygab.armenu.menus.menu.MenuManager;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.event.plugin.TabLoadEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class ARMenu extends JavaPlugin implements CommandExecutor {

    private static ARMenu plugin;
    private MenuManager mm;
    private ItemStorage itemStorage;
    private EventsManager events;
    public Data data;
    public NamespacedKey namespacedKey;

    @Override
    public void onEnable() {
        plugin = this;
        namespacedKey = new NamespacedKey(this,"armenu-item-id");
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);

        mm = new MenuManager();
        TabAPI.getInstance().getFeatureManager().registerFeature(mm.getFeatureName(),mm);
        itemStorage = new ItemStorage();
        Action.registerAll();
        events = new EventsManager(this);
        getServer().getPluginManager().registerEvents(events.getListener(),this);
        data = new Data();

        for (Player player : getServer().getOnlinePlayers()) player.updateCommands();

        TabAPI.getInstance().getEventBus().register(TabLoadEvent.class,e->{if (isEnabled()) reload();});
    }

    @Override
    public void onDisable() {
        TabAPI.getInstance().getFeatureManager().unregisterFeature(mm.getFeatureName());
        mm.unload();
        HandlerList.unregisterAll(this);
        data.unload();
    }

    public static ARMenu get() {
        return plugin;
    }
    public void reload() {
        onDisable();
        onEnable();
    }

    public ItemStorage getItemStorage() {
        return itemStorage;
    }
    public MenuManager getMenuManager() {
        return mm;
    }
    public EventsManager getEventsManager() {
        return events;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1 || args[0].equalsIgnoreCase("help")) {
            new HelpCmd(sender, getDescription().getVersion());
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "list" -> new ListCmd(sender);
            case "open" -> new OpenCmd(sender,args);
            case "create" -> new CreateCmd(sender,args);
            case "execute" -> new ExecuteCmd(sender,args);
            case "items" -> new ItemCmd(sender, args);
            case "events" -> new EventsCmd(sender,args);
            case "reload" -> new ReloadCmd(sender);
            case "test" -> sender.sendMessage("Nope :D");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1)
            return List.of("help","open","list","create","execute","items","events","reload");
        switch (args[0]) {
            case "open" -> {if (args.length == 2) return mm.getMenus();}
            case "execute" -> {if (args.length == 3) return Action.getSugestions(args[2]);}
            case "items" -> {
                if (args.length == 2) return List.of("give","take","save","delete","list");
                if (args.length == 3 && !args[1].equalsIgnoreCase("list") && !args[1].equalsIgnoreCase("save")) return new ArrayList<>(itemStorage.getItems().keySet());
                if (args.length == 4 && (args[1].equalsIgnoreCase("give") || args[1].equalsIgnoreCase("take")))
                    return List.of("<amount>");
            }
            case "events" -> {return events.getSuggestions(args);}
        }
        return null;
    }


}
