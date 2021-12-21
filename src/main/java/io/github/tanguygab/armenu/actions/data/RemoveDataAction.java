package io.github.tanguygab.armenu.actions.data;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.actions.Action;
import me.neznamy.tab.api.TabPlayer;

import java.util.regex.Pattern;

public class RemoveDataAction extends Action {

    private final Pattern pattern = Pattern.compile("(?i)remove-data: ");

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String getSuggestion() {
        return "remove-data: <player|global> <data>";
    }

    @Override
    public boolean replaceMatch() {
        return true;
    }

    @Override
    public void execute(String match, TabPlayer p) {
        String[] args = match.split(" ");
        if (args.length < 2) return;
        String name = args[0];
        String data = args[1];
        ARMenu.get().data.removeData(name,data);
    }
}
