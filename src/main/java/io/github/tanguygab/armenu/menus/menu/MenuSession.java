package io.github.tanguygab.armenu.menus.menu;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import io.github.tanguygab.armenu.menus.item.ClickType;
import io.github.tanguygab.armenu.menus.menu.InventoryEnums.InventoryButton;
import io.github.tanguygab.armenu.menus.menu.InventoryEnums.InventoryProperty;
import io.github.tanguygab.armenu.menus.menu.InventoryEnums.InventoryType;
import me.neznamy.tab.api.TabPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.network.protocol.game.PacketPlayOutWindowData;
import net.minecraft.network.protocol.game.PacketPlayOutWindowItems;
import net.minecraft.world.inventory.InventoryClickType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MenuSession {

    private final TabPlayer p;
    private final Menu menu;

    private Page page;
    private List<Packet<PacketListenerPlayOut>> lastSentPacket = null;

    private final List<PacketPlayOutWindowData> customInventoryProperties = new ArrayList<>();

    public MenuSession(TabPlayer p, Menu menu) {
        this.p = p;
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

        p.setProperty(ARMenu.get().getMenuManager(),"armenu",menu.getName());

        page = new ArrayList<>(menu.getPages().values()).get(0);
        sendPackets(true);
    }

    public void onClosePacket() {
        if (menu.onClose(p)) {
            ARMenu.get().getMenuManager().sessions.remove(p);
            p.setProperty(ARMenu.get().getMenuManager(),"armenu","");
            return;
        }
        if (lastSentPacket != null)
            sendPackets(true);
    }

    public void sendPackets(boolean refresh) {
        List<Packet<PacketListenerPlayOut>> packets = refresh ? getInventoryPackets() : lastSentPacket;
        packets.forEach(packet-> p.sendPacket(packet,ARMenu.get().getMenuManager()));
        lastSentPacket = packets;
    }

    public void setPage(Page page) {
        if (this.page == page) return;
        this.page.onClose(p);
        this.page = page;
        sendPackets(true);
    }

    public void updatePage(int i) {
        List<Page> pages = new ArrayList<>(menu.getPages().values());
        i = pages.indexOf(page)+i; // new page number
        if (i < 0) return; // if page number < 0
        if (i >= pages.size()) // if page number > pages amount -> set last page
            setPage(pages.get(pages.size()-1));
        else setPage(pages.get(i));
    }

    public void setInventoryProperty(InventoryProperty prop, int value) {
        PacketPlayOutWindowData packet = new PacketPlayOutWindowData(66,prop.getProperty(),value);
        customInventoryProperties.add(packet);
        lastSentPacket.add(packet);
        p.sendPacket(packet,ARMenu.get().getMenuManager());
    }

    public List<Packet<PacketListenerPlayOut>> getInventoryPackets() {
        List<Packet<PacketListenerPlayOut>> list = new ArrayList<>();
        int frame = 0;
        page.onOpen(p);
        NonNullList<ItemStack> pageItems = page.getItems(p,frame);
        InventoryType type = menu.getType() != null ? menu.getType() : InventoryType.get(""+pageItems.size());
        if (type == null) type = InventoryType.NORMAL_54;

        list.addAll(page.getSetSlots(p, frame));
        list.addAll(getInventoryProperties());
        NonNullList<ItemStack> pInvItems = NonNullList.a();
        while (pInvItems.size() < 9) pInvItems.add(ItemStack.b);
        pInvItems.addAll(page.getPlayerInvItems(p,frame));
        if (pInvItems.size() > 9)
            list.add(new PacketPlayOutWindowItems(0, 1, pInvItems, ItemStack.b));
        list.add(new PacketPlayOutWindowItems(66, 1, pageItems, ItemStack.b));


        IChatBaseComponent title = IChatBaseComponent.a(menu.getTitles().get(frame));
        PacketPlayOutOpenWindow open = new PacketPlayOutOpenWindow(66, type.container, title);
        list.add(open);
        Collections.reverse(list);
        return list;
    }

    public List<PacketPlayOutWindowData> getInventoryProperties() {
        List<PacketPlayOutWindowData> list = new ArrayList<>();
        menu.getInventoryProperties().forEach((property,value)-> {
            String val = Utils.parsePlaceholders(value+"",p);
            try {
                list.add(new PacketPlayOutWindowData(66, property.getProperty(), Integer.parseInt(val)));
            } catch (Exception ignored) {}
        });
        if (!customInventoryProperties.isEmpty())
            list.addAll(customInventoryProperties);
        return list;
    }

    public boolean onClickPacket(int slot, int button, InventoryClickType mode) {
        sendPackets(false);
        menu.onEvent(p,"events.click", ClickType.get(mode,button,slot)+"",(slot+"").replace("-999","out"));
        menu.getItems().forEach(i-> {
            if (page.getItemAtSlot(slot) == i)
                i.getClickActions(button,mode,p).forEach(map->map.forEach((ac,str)-> Action.execute(str,ac,p)));
        });

        return true;
    }

    public void onMenuButton(int buttonId) {
        InventoryButton button = InventoryButton.get(menu.getType(),buttonId);
        if (button == null) return;

    }
}
