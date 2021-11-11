package io.github.tanguygab.armenu.actions.menus;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.actions.Action;
import io.github.tanguygab.armenu.menus.menu.MenuSession;
import me.neznamy.tab.api.TabPlayer;

import java.util.regex.Pattern;

public class SetArgAction extends Action {

    private final Pattern pattern = Pattern.compile("(i?)set-arg: ");

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String getSuggestion() {
        return "set-arg: <arg> <value>";
    }

    @Override
    public boolean replaceMatch() {
        return true;
    }

    @Override
    public void execute(String match, TabPlayer p) {
        MenuSession session = ARMenu.get().getMenuManager().sessions.get(p);
        if (p == null || session == null) return;

        String[] args = match.split(" ");

        String arg = args[0];
        int arg2;
        try {arg2 = Integer.parseInt(arg);}
        catch (Exception e) {return;}

        String value = "";
        if (args.length > 1)
            value = args[1];

        session.setArgument(arg2,value);
    }
}
