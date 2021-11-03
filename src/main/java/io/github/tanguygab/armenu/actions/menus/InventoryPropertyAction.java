package io.github.tanguygab.armenu.actions.menus;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import io.github.tanguygab.armenu.menus.menu.InventoryEnums.InventoryProperty;
import io.github.tanguygab.armenu.menus.menu.MenuSession;
import me.neznamy.tab.api.TabPlayer;

import java.util.Arrays;
import java.util.regex.Pattern;

public class InventoryPropertyAction extends Action {

    private final Pattern pattern = Pattern.compile("(i?)set-inv(entory)?-prop(erty)?:( )?");

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
        String[] props = match.split(" ");
        if (props.length < 2) return;
        String prop = props[0];
        try {
            int value = Integer.parseInt(props[1]);
            InventoryProperty property = InventoryProperty.get(prop);
            if (property == null) return;
            session.setInventoryProperty(property,value);
        }
        catch (Exception ignored) {}

    }
}
