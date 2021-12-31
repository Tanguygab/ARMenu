package io.github.tanguygab.armenu.actions.bungee;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.actions.Action;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class ServerAction extends Action {

    private final Pattern pattern = Pattern.compile("(?i)server:( )?");

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String getSuggestion() {
        return "server: <server>";
    }

    @Override
    public boolean replaceMatch() {
        return true;
    }

    @Override
    public void execute(String match, TabPlayer p) {
        if (p == null) return;
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(match);
        ((Player)p.getPlayer()).sendPluginMessage(ARMenu.get(),"BungeeCord",out.toByteArray());
    }
}
