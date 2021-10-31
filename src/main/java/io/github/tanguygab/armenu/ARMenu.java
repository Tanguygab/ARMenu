package io.github.tanguygab.armenu;

import io.github.tanguygab.armenu.actions.Action;
import io.github.tanguygab.armenu.commands.*;
import io.github.tanguygab.armenu.item.ItemStorage;
import io.github.tanguygab.armenu.menus.menu.MenuManager;
import me.neznamy.tab.api.TabAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ARMenu extends JavaPlugin implements CommandExecutor {

    private static ARMenu plugin;
    private MenuManager mm;
    private ItemStorage itemStorage;

    @Override
    public void onEnable() {
        plugin = this;
        mm = new MenuManager();
        TabAPI.getInstance().getFeatureManager().registerFeature(mm.getFeatureName(),mm);
        itemStorage = new ItemStorage();
        Action.registerAll();
    }

    @Override
    public void onDisable() {
        TabAPI.getInstance().getFeatureManager().unregisterFeature(mm.getFeatureName());
        mm.unload();
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1 || args[0].equalsIgnoreCase("help")) {
            new HelpCmd(sender, getDescription().getVersion());
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "list" -> new ListCmd(sender);
            case "open" -> new OpenCmd(sender,args);
            case "create" -> sender.sendMessage("create cmd");
            case "execute" -> new ExecuteCmd(sender,args);
            case "items" -> new ItemCmd(sender, args);
            case "reload" -> new ReloadCmd(sender);
            case "test" -> sender.sendMessage("Nope :D");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1)
            return Arrays.asList("help","open","list","create","execute","items","reload");
        switch (args[0]) {
            case "open" -> {if (args.length == 2) return mm.getMenus();}
            case "execute" -> {if (args.length == 3) return Collections.singletonList("<action>");}
            case "items" -> {
                if (args.length == 2) return Arrays.asList("give","take","save","delete","list");
                if (args.length == 3 && !args[1].equalsIgnoreCase("list")) return itemStorage.getItems();
                if (args.length == 4 && (args[1].equalsIgnoreCase("give") || args[1].equalsIgnoreCase("take")))
                    return Collections.singletonList("<amount>");
            }
        }
        return null;
    }


}
