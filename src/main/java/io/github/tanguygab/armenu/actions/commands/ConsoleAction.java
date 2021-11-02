package io.github.tanguygab.armenu.actions.commands;

import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.Bukkit;

import java.util.regex.Pattern;

public class ConsoleAction extends Action {

    private final Pattern pattern = Pattern.compile("(?i)console:( )?");

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
        if (p != null)
            match = Utils.parsePlaceholders(match,p);
        String finalMatch = match;
        runSync(()->Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), finalMatch));
    }
}
