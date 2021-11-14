package io.github.tanguygab.armenu.commands;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.Utils;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import org.bukkit.command.CommandSender;

import java.util.List;

public class    ListCmd {

    public ListCmd(CommandSender p) {
        List<String> list = ARMenu.get().getMenuManager().getMenus();
        IChatBaseComponent txt = Utils.newComp("&aList of menus &8(&7"+list.size()+"&8)&a:");

        list.forEach(menu->{
            IChatBaseComponent comp = Utils.newComp("\n &8- &3"+menu);
            comp.getModifier().onHoverShowText(Utils.newComp("Click to open!"));
            comp.getModifier().onClickRunCommand("/armenu open "+menu);
            txt.addExtra(comp);
        });
        Utils.senderMsg(p,txt);
    }

}
