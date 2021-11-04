package io.github.tanguygab.armenu.actions.messages;

import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TitleAction extends Action {

    private final Pattern pattern = Pattern.compile("(?i)title:( )?");

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
        String[] matches = match.split(";");

        String title = matches[0];
        String subtitle = "";
        int fadein = 5;
        int stay = 5;
        int fadeout = 5;
        if (matches.length > 1)
            subtitle = matches[1];
        if (matches.length > 2)
            fadein = parseInt(matches[2]);
        if (matches.length > 3)
            stay = parseInt(matches[3]);
        if (matches.length > 4)
            fadeout = parseInt(matches[4]);

        ((Player)p).sendTitle(title,subtitle,fadein,stay,fadeout);
    }

    private int parseInt(String str) {
        try {return Integer.parseInt(str);}
        catch (Exception e) {return 5;}
    }
}
