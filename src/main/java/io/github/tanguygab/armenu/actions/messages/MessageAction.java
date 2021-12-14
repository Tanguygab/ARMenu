package io.github.tanguygab.armenu.actions.messages;

import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class MessageAction extends Action {

    private final Pattern pattern = Pattern.compile("(?i)(message|msg|tell):( )?");

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String getSuggestion() {
        return "message: <text>";
    }

    @Override
    public boolean replaceMatch() {
        return true;
    }

    @Override
    public void execute(String match, TabPlayer p) {
        match = Utils.parsePlaceholders(match,p);
        if (p == null) TabAPI.getInstance().sendConsoleMessage(match,true);
        else p.sendMessage(match,true);
    }
}
