package io.github.tanguygab.armenu.commands;

import io.github.tanguygab.armenu.Utils;
import org.bukkit.command.CommandSender;

public class HelpCmd {

    public HelpCmd(CommandSender p, String version) {
        String txt = "&m                                        \n"
                + "&a[ARMenu] &7" + version + "\n"
                + " - &3/armenu [help]\n"
                + "   &8| &aDefault help page\n"
                + " - &3/armenu open <menu> [player] [page] [args...]\n"
                + "   &8| &aOpen the specified menu\n"
                + " - &3/armenu list\n"
                + "   &8| &aList of all loaded menus\n"
                + " - &3/armenu create <name> [size]\n"
                + "   &8| &aCreate a menu easily!\n"
                + " - &3/armenu execute <player> <action>\n"
                + "   &8| &aExecute the specified action as the specified player\n"
                + " - &3/armenu items <add/remove/give/take> <name> [player]\n"
                + "   &8| &aAdd/Remove/Give/Take an item to/from the Item Storage\n"
                + " - &3/armenu reload\n"
                + "   &8| &aReloads the configuration file\n"
                + "&m                                        ";
        Utils.senderMsg(p,txt);
    }
}
