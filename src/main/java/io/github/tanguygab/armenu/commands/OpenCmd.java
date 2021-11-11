package io.github.tanguygab.armenu.commands;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.menus.menu.Menu;
import io.github.tanguygab.armenu.menus.menu.Page;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class OpenCmd {

    public OpenCmd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("You need to provide a menu!");
            return;
        }

        String[] menuPage = args[1].split(":");
        String m = menuPage[0];
        String page = menuPage.length > 1 ? menuPage[1] : null;

        Menu menu = ARMenu.get().getMenuManager().getMenu(m);
        if (menu == null) {
            sender.sendMessage("That menu doesn't exist!");
            return;
        }
        Page mp = menu.getPages().get(page);
        if (page != null && mp == null) {
            sender.sendMessage("This page doesn't exist!");
            return;
        }

        TabPlayer p = TAB.getInstance().getPlayer(sender.getName());
        if (args.length > 2) {
            p = TAB.getInstance().getPlayer(args[2]);
            if (p == null) {
                sender.sendMessage("This player isn't online");
                return;
            }
        }

        List<String> menuArgs = null;
        if (args.length > 3)
            menuArgs = (Arrays.asList(args).subList(3, args.length));

        ARMenu.get().getMenuManager().newMenuSession(p,menu,mp,menuArgs);
    }
}
