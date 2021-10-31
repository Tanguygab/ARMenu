package io.github.tanguygab.armenu.commands;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.menus.menu.MenuManager;
import org.bukkit.command.CommandSender;

import java.util.List;

public class    ListCmd {

    public ListCmd(CommandSender p) {
        List<String> list = ARMenu.get().getMenuManager().getMenus();
        String txt = "&aList of menus &8(&7"+list.size()+"&8)&a:";
        for (String menu : list) {
            txt = txt + "\n - &3"+menu;
        }
        Utils.senderMsg(p,txt);
    }

}
