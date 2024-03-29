package io.github.tanguygab.armenu.actions.menus;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import io.github.tanguygab.armenu.menus.menu.MenuSession;
import me.neznamy.tab.api.TabPlayer;

import java.util.regex.Pattern;

public class UpdatePageAction extends Action {

    private final Pattern pattern = Pattern.compile("(?i)update-page:( )?");

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String getSuggestion() {
        return "update-page: <amount>";
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
        session.updatePage(Utils.parseInt(match,0));
    }
}
