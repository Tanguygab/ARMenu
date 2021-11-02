package io.github.tanguygab.armenu.menus.menu;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.actions.Action;
import me.neznamy.tab.api.TabPlayer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.network.protocol.game.PacketPlayOutWindowItems;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.inventory.Containers;
import net.minecraft.world.inventory.InventoryClickType;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;

import java.util.ArrayList;
import java.util.List;

public class MenuSession {

    private final TabPlayer p;
    private final PlayerConnection connection;
    private final Menu menu;

    public Page page;
    public PacketPlayOutWindowItems lastSentPacket = null;

    public MenuSession(TabPlayer p, Menu menu) {
        this.p = p;
        connection = ((CraftPlayer)p.getPlayer()).getHandle().b;
        this.menu = menu;
    }

    public TabPlayer getPlayer() {
        return p;
    }
    public Menu getMenu() {
        return menu;
    }

    public void openMenu() {
        if (!menu.onOpen(p)) {
            ARMenu.get().getMenuManager().sessions.remove(p);
            return;
        }

        IChatBaseComponent title = IChatBaseComponent.a(menu.getTitles().get(0));
        PacketPlayOutOpenWindow open = new PacketPlayOutOpenWindow(66, Containers.f, title);
        p.setProperty(ARMenu.get().getMenuManager(),"armenu",menu.getName());
        connection.sendPacket(open);

        page = new ArrayList<>(menu.getPages().values()).get(0);
        sendPackets(true);
    }

    public void onClosePacket() {
        if (menu.onClose(p)) {
            ARMenu.get().getMenuManager().sessions.remove(p);
            p.setProperty(ARMenu.get().getMenuManager(),"armenu",null);
            return;
        }
        if (lastSentPacket != null)
            connection.sendPacket(lastSentPacket);
    }

    public void setPage(Page page) {
        if (this.page == page) return;
        this.page = page;
        sendPackets(true);
    }

    public void sendPackets(boolean refresh) {
        PacketPlayOutWindowItems packet = refresh ? getInventoryPacket() : lastSentPacket;
        connection.sendPacket(packet);
        lastSentPacket = packet;
    }

    public void updatePage(int i) {
        List<Page> pages = new ArrayList<>(menu.getPages().values());
        i = pages.indexOf(page)+i; // new page number
        if (i < 0) return; // if page number < 0
        if (i >= pages.size()) // if page number > pages amount -> set last page
            setPage(pages.get(pages.size()-1));
        else setPage(pages.get(i));
    }

    public PacketPlayOutWindowItems getInventoryPacket() {
        page.onOpen(p);
        return new PacketPlayOutWindowItems(66, 1, page.getItems(p,0) ,ItemStack.b);
    }

    public boolean onClickPacket(int slot, int button, InventoryClickType mode, ItemStack item) {
        menu.getItems().forEach(i-> {
            if (page.getItemAtSlot(slot) == i)
                i.getClickActions(slot,button,mode,item,p).forEach(map->map.forEach((ac,str)-> Action.execute(str,ac,p)));

        });
        sendPackets(false);
        return true;
    }
}
