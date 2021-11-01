package io.github.tanguygab.armenu.actions.pages;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import io.github.tanguygab.armenu.menus.menu.MenuSession;
import io.github.tanguygab.armenu.menus.menu.Page;
import me.neznamy.tab.api.TabPlayer;

import java.util.regex.Pattern;

public class SetPageAction extends Action {

    @Override
    public Pattern getPattern() {
        return Pattern.compile("(?i)set-page:( )?");
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
