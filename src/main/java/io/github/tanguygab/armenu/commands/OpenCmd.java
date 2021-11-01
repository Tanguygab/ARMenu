package io.github.tanguygab.armenu.commands;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.menus.menu.Menu;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import org.bukkit.command.CommandSender;

public class OpenCmd {

    public OpenCmd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("You need to provide a menu!");
            return;
        }

        Menu menu = ARMenu.get().getMenuManager().getMenu(args[1]);
        if (menu == null) {
            sender.sendMessage("That menu doesn't exist!");
            return;
        }

        TabPlayer p = TAB.getInstance().getPlayer(sender.getName());
        if (args.length > 3) {
            p = TAB.getInstance().getPlayer(args[2]);
            if (p == null) {
                sender.sendMessage("This player isn't online");
                return;
            }
        }
        ARMenu.get().getMenuManager().newMenuSession(p,menu);
    }
}
