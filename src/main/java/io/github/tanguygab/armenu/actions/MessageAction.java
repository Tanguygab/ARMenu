package io.github.tanguygab.armenu.actions;

import io.github.tanguygab.armenu.Utils;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class MessageAction extends Action {

    @Override
    public Pattern getPattern() {
        return Pattern.compile("(?i)(message|msg|tell):( )?");
    }

    @Override
    public boolean replaceMatch() {
        return true;
    }

    @Override
    public void execute(String match, TabPlayer p) {
        if (p == null) return;
        match = Utils.parsePlaceholders(match,p);
        p.sendMessage(match,true);
    }
}
