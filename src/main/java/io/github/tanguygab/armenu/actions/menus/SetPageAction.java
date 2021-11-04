package io.github.tanguygab.armenu.actions.menus;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import io.github.tanguygab.armenu.menus.menu.MenuSession;
import io.github.tanguygab.armenu.menus.menu.Page;
import me.neznamy.tab.api.TabPlayer;

import java.util.regex.Pattern;

public class SetPageAction extends Action {

    private final Pattern pattern = Pattern.compile("(?i)set-page:( )?");

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public boolean replaceMatch() {
        return true;
    }

    @Override
    public void execute(String match, TabPlayer p) {
        MenuSession session;
        if (p == null || (session = ARMenu.get().getMenuManager().sessions.get(p)) == null) return;
        match = Utils.parsePlaceholders(match,p);
        Page page = session.getMenu().getPages().get(match);
        if (page == null) return;
        session.setPage(page);
    }
}
