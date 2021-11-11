package io.github.tanguygab.armenu.actions.menus;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.actions.Action;
import io.github.tanguygab.armenu.menus.menu.Menu;
import io.github.tanguygab.armenu.menus.menu.MenuManager;
import io.github.tanguygab.armenu.menus.menu.Page;
import me.neznamy.tab.api.TabPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class OpenAction extends Action {

    private final Pattern pattern = Pattern.compile("(i?)open: ");

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String getSuggestion() {
        return "open: <menu>[:<page>] [args...]";
    }

    @Override
    public boolean replaceMatch() {
        return true;
    }

    @Override
    public void execute(String match, TabPlayer p) {
        if (p == null) return;

        MenuManager mm = ARMenu.get().getMenuManager();
        boolean closed = true;
        if (mm.sessions.containsKey(p))
            closed = mm.sessions.get(p).onClose();

        if (!closed) return;

        p.sendMessage("hi"+match,false);

        String[] args = match.split(" ");

        String[] menupage = args[0].split(":");
        String menu = menupage[0];
        Menu m = mm.getMenu(menu);
        p.sendMessage("hi"+ Arrays.toString(args),false);
        if (m == null) return;

        String page = menupage.length > 1 ? menupage[1] : null;
        Page mp = m.getPages().get(page);
        p.sendMessage("hi"+mp,false);

        List<String> menuArgs = null;
        if (args.length > 1)
            menuArgs = (Arrays.asList(args).subList(1, args.length));


        p.sendMessage("hi"+menuArgs,false);

        mm.newMenuSession(p,m,mp,menuArgs);
    }
}
