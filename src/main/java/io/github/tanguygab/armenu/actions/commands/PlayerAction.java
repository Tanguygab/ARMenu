package io.github.tanguygab.armenu.actions.commands;

import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class PlayerAction extends Action {

    private final Pattern pattern = Pattern.compile("(?i)(player|cmd|command):( )?");

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String getSuggestion() {
        return "player: <command>";
    }

    @Override
    public boolean replaceMatch() {
        return true;
    }

    @Override
    public void execute(String match, TabPlayer p) {
        if (p == null) return;
        match = Utils.parsePlaceholders(match,p);
        String finalMatch = match;
        runSync(()->Bukkit.getServer().dispatchCommand(((Player)p.getPlayer()), finalMatch));
    }
}
