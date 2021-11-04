package io.github.tanguygab.armenu.actions.menus;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.actions.Action;
import io.github.tanguygab.armenu.menus.menu.MenuSession;
import me.neznamy.tab.api.TabPlayer;

import java.util.regex.Pattern;

public class RefreshAction extends Action {

    private final Pattern pattern = Pattern.compile("(i?)refresh");

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String getSuggestion() {
        return "refresh";
    }

    @Override
    public boolean replaceMatch() {
        return false;
    }

    @Override
    public void execute(String match, TabPlayer p) {
        MenuSession session;
        if (p != null && (session = ARMenu.get().getMenuManager().sessions.get(p)) != null)
            session.sendPackets(true);
    }
}
