package io.github.tanguygab.armenu.actions.menus;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import io.github.tanguygab.armenu.menus.item.Item;
import io.github.tanguygab.armenu.menus.item.ListItem;
import io.github.tanguygab.armenu.menus.menu.MenuSession;
import me.neznamy.tab.api.TabPlayer;

import java.util.regex.Pattern;

public class UpdateListAction extends Action {

    private final Pattern pattern = Pattern.compile("(?i)update-list:( )?");

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String getSuggestion() {
        return "update-list: <list> <increment>";
    }

    @Override
    public boolean replaceMatch() {
        return true;
    }

    @Override
    public void execute(String match, TabPlayer p) {
        MenuSession session;
        if (p == null || (session = ARMenu.get().getMenuManager().sessions.get(p)) == null) return;
        match = Utils.parsePlaceholders(match, p);
        String[] args = match.split(" ");
        if (args.length < 2) return;
        String list = args[0];
        int index = Utils.parseInt(args[1], 0);
        Item item = session.getMenu().getItemsMap().get(list);
        if (!(item instanceof ListItem)) return;
        ((ListItem) item).updateCurrentIncrement(p,index);
    }
}