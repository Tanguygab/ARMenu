package io.github.tanguygab.armenu.commands;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.actions.Action;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class ExecuteCmd {

    public ExecuteCmd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("You need to provide a player!");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("You need to provide an action!");
            return;
        }
        TabPlayer p = TAB.getInstance().getPlayer(args[1]);
        String action = String.join(" ",Arrays.asList(args).subList(2, args.length));

        Bukkit.getServer().getScheduler().runTaskAsynchronously(ARMenu.get(),()->Action.findAndExecute(action,p));
    }
}
