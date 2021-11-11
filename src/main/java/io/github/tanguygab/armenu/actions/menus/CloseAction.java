package io.github.tanguygab.armenu.actions.menus;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.actions.Action;
import io.github.tanguygab.armenu.menus.menu.MenuSession;
import me.neznamy.tab.api.TabPlayer;

import java.util.regex.Pattern;

public class CloseAction extends Action {

    private final Pattern pattern = Pattern.compile("^(i?)close( true)?$");

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String getSuggestion() {
        return "close";
    }

    @Override
    public boolean replaceMatch() {
        return true;
    }

    @Override
    public void execute(String match, TabPlayer p) {
        MenuSession session;
        if (p != null && (session = ARMenu.get().getMenuManager().sessions.get(p)) != null) {
            if (Boolean.parseBoolean(match))
                session.forceCloseMenu();
            else session.onClose();
        }
    }
}
