package io.github.tanguygab.armenu.commands;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.Utils;
import org.bukkit.command.CommandSender;

public class ReloadCmd {

    public ReloadCmd(CommandSender p) {
        ARMenu.get().reload();
        p.sendMessage("Reloaded!");
    }
}
