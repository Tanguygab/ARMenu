package io.github.tanguygab.armenu.actions.messages;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutChat;
import me.neznamy.tab.api.protocol.TabPacket;

import java.util.regex.Pattern;

public class BroadcastActionBarAction extends Action {

    private final Pattern pattern = Pattern.compile("(?i)((broadcast|bc)-actionbar):( )?");

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String getSuggestion() {
        return "broadcast-actionbar: <text>";
    }

    @Override
    public boolean replaceMatch() {
        return true;
    }

    @Override
    public void execute(String match, TabPlayer p) {
        match = Utils.parsePlaceholders(match,p);
        TabPacket packet = new PacketPlayOutChat(IChatBaseComponent.optimizedComponent(match), PacketPlayOutChat.ChatMessageType.GAME_INFO);
        for (TabPlayer all : TabAPI.getInstance().getOnlinePlayers()) {
            all.sendCustomPacket(packet, ARMenu.get().getMenuManager());
        }
    }
}
