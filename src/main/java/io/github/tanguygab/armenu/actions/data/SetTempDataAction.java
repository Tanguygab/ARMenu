package io.github.tanguygab.armenu.actions.data;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.actions.Action;
import me.neznamy.tab.api.TabPlayer;

import java.util.regex.Pattern;

public class SetTempDataAction extends Action {

    private final Pattern pattern = Pattern.compile("(?i)set-temp-data: ");

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String getSuggestion() {
        return "set-temp-data: <player|global> <data> <value>";
    }

    @Override
    public boolean replaceMatch() {
        return true;
    }

    @Override
    public void execute(String match, TabPlayer p) {
        String[] args = match.split(" ");
        if (args.length < 3) return;
        String name = args[0];
        String data = args[1];
        String value = args[2];
        ARMenu.get().data.setData(name,data,value,true);
    }
}
