package io.github.tanguygab.armenu.actions;

import io.github.tanguygab.armenu.Utils;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class ChatAction extends Action {

    private final Pattern pattern = Pattern.compile("(?i)chat:( )?");

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
        if (p == null) return;
        match = Utils.parsePlaceholders(match,p);
        String finalMatch = match;
        runSync(()->((Player)p.getPlayer()).chat(finalMatch));
    }
}
