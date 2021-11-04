package io.github.tanguygab.armenu.actions.messages;

import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;

import java.util.regex.Pattern;

public class BroadcastAction extends Action {

    private final Pattern pattern = Pattern.compile("(?i)(broadcast|bc):( )?");

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String getSuggestion() {
        return "broadcast: <text>";
    }

    @Override
    public boolean replaceMatch() {
        return true;
    }

    @Override
    public void execute(String match, TabPlayer p) {
        for (TabPlayer all : TabAPI.getInstance().getOnlinePlayers()) {
            match = Utils.parsePlaceholders(match,p);
            all.sendMessage(match, true);
        }
    }
}
