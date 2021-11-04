package io.github.tanguygab.armenu.actions;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.actions.commands.ConsoleAction;
import io.github.tanguygab.armenu.actions.commands.PermissionAction;
import io.github.tanguygab.armenu.actions.commands.PlayerAction;
import io.github.tanguygab.armenu.actions.menus.InventoryPropertyAction;
import io.github.tanguygab.armenu.actions.menus.RefreshAction;
import io.github.tanguygab.armenu.actions.messages.*;
import io.github.tanguygab.armenu.actions.menus.SetPageAction;
import io.github.tanguygab.armenu.actions.menus.UpdatePageAction;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Action {

    private static final List<Action> actions = new ArrayList<>();
    public static final List<String> suggestions = new ArrayList<>();

    public static Action find(String action) {
        for (Action ac : actions) {
            Matcher matcher = ac.getPattern().matcher(action);
            if (matcher.find())
                return ac;
        }
        return null;
    }

    public static void execute(String action, Action ac, TabPlayer p) {
        if (ac == null) return;
        if (ac.replaceMatch())
            action = action.replaceAll(ac.getPattern().pattern(),"");
        ac.execute(action,p);
    }

    public static void findAndExecute(String action, TabPlayer p) {
        Action ac = find(action);
        execute(action,ac,p);
    }

    public static void register(Action... action) {
        actions.addAll(List.of(action));
    }


    public static void registerAll() {
        Action.register(
                new ConsoleAction(),
                new PlayerAction(),

                new InventoryPropertyAction(),
                new RefreshAction(),
                new SetPageAction(),
                new UpdatePageAction(),

                new ActionBarAction(),
                new BroadcastAction(),
                new ChatAction(),
                new MessageAction(),
                new TitleAction()

        );
        getSugestions();
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("LuckPerms"))
            Action.register(new PermissionAction());
    }

    public static void getSugestions() {
        actions.forEach(action->{
            if (action.getSuggestion() != null)
                suggestions.add(action.getSuggestion());
        });
    }

    public abstract Pattern getPattern();

    public abstract String getSuggestion();

    public abstract boolean replaceMatch();

    public abstract void execute(String match, TabPlayer p);

    public void runSync(Runnable r) {
        Bukkit.getServer().getScheduler().runTask(ARMenu.get(),r);
    }

    public <T> Future<T> runSyncFuture(Callable<T> c) {
        return Bukkit.getServer().getScheduler().callSyncMethod(ARMenu.get(),c);
    }

}
