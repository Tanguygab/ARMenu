package io.github.tanguygab.armenu.actions.commands;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class PlayerAction extends Action {

    @Override
    public Pattern getPattern() {
        return Pattern.compile("(?i)(player|cmd|command):( )?");
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
