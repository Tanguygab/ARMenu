package io.github.tanguygab.armenu;

import io.github.tanguygab.armenu.actions.Action;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.shared.PropertyImpl;
import me.neznamy.tab.shared.TAB;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    public static void senderMsg(CommandSender sender, Object msg) {
        if (sender instanceof Player) {
            TabPlayer p = TAB.getInstance().getPlayer(((Player) sender).getUniqueId());
            if (msg instanceof IChatBaseComponent comp)
                p.sendMessage(comp);
            else p.sendMessage(msg+"", true);
        }
        else {
            if (msg instanceof IChatBaseComponent comp)
                msg = comp.toLegacyText();
            TAB.getInstance().getPlatform().sendConsoleMessage(msg+"",true);
        }
    }

    public static IChatBaseComponent newComp(String str) {
        return IChatBaseComponent.fromColoredText(str);
    }

    public static String parsePlaceholders(String str, TabPlayer p) {
        if (str == null) return "";
        if (!str.contains("%")) return EnumChatFormat.color(str);
        str = new PropertyImpl(ARMenu.get().getMenuManager(),p,str).get();
        return EnumChatFormat.color(str);
    }

    public static String parsePlaceholders(String str, TabPlayer sender, TabPlayer viewer) {
        List<String> list = TabAPI.getInstance().getPlaceholderManager().detectPlaceholders(str);
        for (String pl : list) {
            if (pl.startsWith("%sender:") && sender != null) {
                String pl2 = pl.replace("%sender:", "%");
                str = str.replace(pl,new PropertyImpl(ARMenu.get().getMenuManager(),sender,pl2).getFormat(viewer));
                continue;
            }
            else if (pl.startsWith("%viewer:") && viewer != null) {
                String pl2 = pl.replace("%viewer:", "%");
                str = str.replace(pl,new PropertyImpl(ARMenu.get().getMenuManager(),viewer,pl2).getFormat(sender));
                continue;
            }
            str = str.replace(pl,new PropertyImpl(ARMenu.get().getMenuManager(),sender,pl).getFormat(viewer));
        }
        return EnumChatFormat.color(str);
    }

    public static Map<Action,String> map(Object action) {
        Map<Action,String> map = new HashMap<>();
        map.put(Action.find(action+""),action+"");
        return map;
    }

    public static String replacements(String str, Map<String,String> replacements) {
        if (replacements == null) return str;
        for (String placeholder : replacements.keySet())
            str = str.replace(placeholder,replacements.get(placeholder));
        return str;
    }

    public static int parseInt(String arg, int i) {
        try {return Integer.parseInt(arg);}
        catch (Exception e) {return i;}
    }

    public static int frame(int frame, int length) {
        while (frame >= length)
            frame = frame-length;
        return frame;
    }

    public static int slotNMStoSpigot(int slot) {
        if (slot > 35 && slot < 45) return slot-36;
        return slot;
    }
}
