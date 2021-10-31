package io.github.tanguygab.armenu;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.RelationalPlaceholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class Utils {

    public static void senderMsg(CommandSender sender, String msg) {
        if (sender instanceof Player)
            TAB.getInstance().getPlayer(((Player) sender).getUniqueId()).sendMessage(msg,true);
        else TAB.getInstance().getPlatform().sendConsoleMessage(msg,true);
    }

    public static String parsePlaceholders(String str, TabPlayer p) {
        if (str == null) return "";
        //if (p == null) return str;
        if (!str.contains("%")) return EnumChatFormat.color(str);
        for (String pl : TabAPI.getInstance().getPlaceholderManager().detectPlaceholders(str))
            str = str.replace(pl,getLastPlaceholderValue(pl,p,null));
        return EnumChatFormat.color(str);
    }

    public static String parsePlaceholders(String str, TabPlayer sender, TabPlayer viewer, TabPlayer def) {
        List<String> list = TabAPI.getInstance().getPlaceholderManager().detectPlaceholders(str);
        TabPlayer def2 = def == viewer ? sender : viewer;
        for (String pl : list) {
            if (pl.startsWith("%sender:") && sender != null) {
                String pl2 = pl.replace("%sender:", "%");
                str = str.replace(pl,getLastPlaceholderValue(pl2,sender,viewer));
                continue;
            }
            else if (pl.startsWith("%viewer:") && viewer != null) {
                String pl2 = pl.replace("%viewer:", "%");
                str = str.replace(pl,getLastPlaceholderValue(pl2,viewer,sender));
                continue;
            }
            str = str.replace(pl,getLastPlaceholderValue(pl,def,def2));
        }
        return EnumChatFormat.color(str);
    }

    private static String getLastPlaceholderValue(String str, TabPlayer p, TabPlayer viewer) {
        TabAPI.getInstance().getPlaceholderManager().addUsedPlaceholder(str,ARMenu.get().getMenuManager());
        Placeholder pl = TAB.getInstance().getPlaceholderManager().getPlaceholder(str);
        if (pl instanceof RelationalPlaceholder) {
            if (p == null || viewer == null) return str;
            return ((RelationalPlaceholder) pl).getLastValue(p, viewer);
        }
        String value = pl.getLastValue(p);
        String newValue = TabAPI.getInstance().getPlaceholderManager().findReplacement(pl.getIdentifier(), value);
        if (newValue.contains("%value%"))
            newValue = newValue.replace("%value%", value);

        return newValue;
    }

}
