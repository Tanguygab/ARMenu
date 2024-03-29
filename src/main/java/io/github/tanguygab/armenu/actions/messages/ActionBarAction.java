package io.github.tanguygab.armenu.actions.messages;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutChat;

import java.util.regex.Pattern;

public class ActionBarAction extends Action {

    private final Pattern pattern = Pattern.compile("(?i)(actionbar):( )?");

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String getSuggestion() {
        return "actionbar: <text>";
    }

    @Override
    public boolean replaceMatch() {
        return true;
    }

    @Override
    public void execute(String match, TabPlayer p) {
        if (p == null) return;
        match = Utils.parsePlaceholders(match,p);
        p.sendCustomPacket(new PacketPlayOutChat(Utils.newComp(match), PacketPlayOutChat.ChatMessageType.GAME_INFO), ARMenu.get().getMenuManager());
    }
}
