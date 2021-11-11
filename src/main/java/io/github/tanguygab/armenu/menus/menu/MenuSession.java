package io.github.tanguygab.armenu.menus.menu;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import io.github.tanguygab.armenu.menus.item.ClickType;
import io.github.tanguygab.armenu.menus.item.InvItem;
import io.github.tanguygab.armenu.menus.item.Item;
import io.github.tanguygab.armenu.menus.menu.InventoryEnums.InventoryButton;
import io.github.tanguygab.armenu.menus.menu.InventoryEnums.InventoryProperty;
import io.github.tanguygab.armenu.menus.menu.InventoryEnums.InventoryType;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.task.RepeatingTask;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.inventory.InventoryClickType;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class MenuSession {

    private final TabPlayer p;
    private final Menu menu;
    private RepeatingTask task;

    private Page page;
    private final List<String> arguments;
    private List<Packet<PacketListenerPlayOut>> lastSentPacket = null;
    private NonNullList<ItemStack> lastSentItems = null;
    private final List<PacketPlayOutWindowData> customInventoryProperties = new ArrayList<>();

    public final Map<Integer,Item> currentItems = new HashMap<>();

    public final Map<Integer,Item> playerInventoryOnOpen = new HashMap<>();

    public MenuSession(TabPlayer p, Menu menu, Page page, List<String> args) {
        this.p = p;
        this.menu = menu;
        this.page = page;
        this.arguments = args != null ? args : new ArrayList<>();
    }

    public TabPlayer getPlayer() {
        return p;
    }
    public Menu getMenu() {
        return menu;
    }
    public Page getPage() {
        return page;
    }
    public List<String> getArguments() {
        return arguments;
    }
    public String getArgument(int i) {
        return arguments.size() > i ? arguments.get(i) : "";
    }
    public void setArgument(int i, String arg) {
        if (arguments.size() > i) arguments.set(i,arg);
    }

    public void openMenu() {
        if (!menu.onOpen(p)) {
            ARMenu.get().getMenuManager().sessions.remove(p);
            return;
        }

        p.setProperty(ARMenu.get().getMenuManager(),"armenu",menu.getName());


        org.bukkit.inventory.ItemStack[] items = ((Player)p.getPlayer()).getInventory().getStorageContents();
        List<Item> list = new ArrayList<>();
        List<Item> hotbar = new ArrayList<>();
        for (int i = 0; i < items.length; i++) {
            if (i < 9)
                hotbar.add(new InvItem(items[i], i));
            else
                list.add(new InvItem(items[i], i));
        }
        list.addAll(hotbar);
        for (int i = 0; i < list.size(); i++)
            playerInventoryOnOpen.put(i,list.get(i));

        if (page == null)
            setPage(new ArrayList<>(menu.getPages().values()).get(0));
        else { // otherwise it doesn't send the packets D:
            Page newPage = page;
            page = null;
            setPage(newPage);
        }

        task = TabAPI.getInstance().getThreadManager().startRepeatingMeasuredTask(500,"refreshing ARMenu menu for player "+p.getName(),ARMenu.get().getMenuManager(),"refreshing",()->{
            sendPackets(true);
        });
    }

    public void forceCloseMenu() {
        task.cancel();
        TabAPI.getInstance().getThreadManager().runTaskLater(100,"closing menu for "+p.getName(),()->{
            p.sendPacket(new PacketPlayOutCloseWindow(66));
            ((Player)p.getPlayer()).updateInventory();
        });
        ARMenu.get().getMenuManager().sessions.remove(p);
        p.setProperty(ARMenu.get().getMenuManager(),"armenu",menu.getName());
    }

    public boolean onClose() {
        if (menu.onClose(p)) {
            forceCloseMenu();
            return true;
        }
        if (lastSentPacket != null)
            sendPackets(true);
        return false;
    }

    public void sendPackets(boolean refresh) {
        List<Packet<PacketListenerPlayOut>> packets = refresh ? getInventoryPackets() : lastSentPacket;
        MenuManager mm = ARMenu.get().getMenuManager();
        packets.forEach(packet-> p.sendPacket(packet,mm));
        if (!refresh) {
            if (lastSentItems != null)
                p.sendPacket(new PacketPlayOutWindowItems(66, 1, lastSentItems, ItemStack.b),mm);
        }
        lastSentPacket = packets;
    }

    public void setPage(Page page) {
        if (page == null || this.page == page ) return;
        if (this.page != null)
            this.page.onClose(p);
        this.page = page;
        currentItems.clear();
        for (int i = 0; i < page.getItems().size(); i++)
            currentItems.put(i,page.getItems().get(i));

        int size = menu.getType() != null ? menu.getType().getSize() : page.getLayoutSize();

        if (page.getPlayerInvItems() != null) {
            for (int i = 0; i < page.getPlayerInvItems().size(); i++) {
                currentItems.put(i+size, page.getPlayerInvItems().get(i));
            }
            NonNullList<ItemStack> list = page.getPlayerInvItems(p, 0);

            if (lastSentItems != null)
                list.forEach(itemStack -> lastSentItems.set(list.indexOf(itemStack)+size,itemStack));
        } else {
            playerInventoryOnOpen.forEach((slot,i)->currentItems.put(slot+size,i));
        }
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
        page.onOpen(p);
        int frame = 0;

        List<Packet<PacketListenerPlayOut>> list = new ArrayList<>();

        IChatBaseComponent title = IChatBaseComponent.a(menu.getTitles().get(frame));
        InventoryType type = menu.getType() != null ? menu.getType() : InventoryType.get("" + page.getLayoutSize());
        if (type == null)
            type = InventoryType.NORMAL_54;
        list.add(new PacketPlayOutOpenWindow(66, type.container, title));

        NonNullList<ItemStack> pageItems = NonNullList.a();

        page.getSetSlots(p,frame).forEach((item,slots)-> slots.forEach(slot-> currentItems.put(slot,item)));

        currentItems.forEach((slot, item) -> {
            if (item != null)
                pageItems.add(item.getItem(frame, p, page, slot));
            else pageItems.add(ItemStack.b);
        });
        lastSentItems = pageItems;


        list.add(new PacketPlayOutWindowItems(66, 1, pageItems, ItemStack.b));

        list.addAll(getInventoryProperties());

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

    public ItemStack heldItem = ItemStack.b;
    public int lastClickedSlot = -2;

    public boolean onClickPacket(int slot, int button, InventoryClickType mode, ItemStack held, ItemStack placed) {

        menu.onEvent(p,"events.click", ClickType.get(mode,button,slot)+"",(slot+"").replace("-999","out").replace("-1","border"));

        Item i;
        boolean equal = heldItem != ItemStack.b && itemsEquals(placed,heldItem);
        if (equal)
            i = getSlot(lastClickedSlot);
        else i = getSlot(slot);
        Item i2 = getSlot(slot);

        if (!equal && i != null)
            i.getClickActions(button, mode, p, slot, page).forEach(map -> map.forEach((ac, str) -> Action.execute(str, ac, p)));
        else {
            if (i2 != null)
                i2.getClickActions(button, mode, p, lastClickedSlot, page).forEach(map -> map.forEach((ac, str) -> Action.execute(str, ac, p)));
        }
        if ((i == null || i.isMovable()) && slot != -999 && slot != -1) {
            if (!changeSlots(slot, equal, i, i2, placed, held)) {
                p.sendPacket(new PacketPlayOutSetSlot(66, -1, slot, held));
                p.sendPacket(new PacketPlayOutWindowItems(66, 1, lastSentItems, placed), ARMenu.get().getMenuManager());
                return true;
            }
        } else if (slot == -1) {
            return true;
        } else sendPackets(false);
        heldItem = held;
        lastClickedSlot = slot;
        return true;
    }

    public boolean changeSlots(int slot, boolean equal, Item i, Item i2, ItemStack placed, ItemStack held) {
        if (equal)
            if (i2 == null || i2.isMovable())
                setSlot(lastClickedSlot,i2,heldItem);
            else return false;
        setSlot(slot,i,placed);
        return true;
    }

    public boolean itemsEquals(ItemStack placed, ItemStack heldItem) {
        return Objects.equals(getMeta(placed),getMeta(heldItem));
    }

    public String getMeta(ItemStack item) {
        ItemMeta meta = CraftItemStack.getItemMeta(item);
        if (meta == null) return "";
        return meta.getPersistentDataContainer().get(ARMenu.get().namespacedKey, PersistentDataType.STRING);
    }

    public void setSlot(int slot, Item i, ItemStack item) {
        currentItems.put(slot, i);
        lastSentItems.set(slot,item);
    }
    public Item getSlot(int slot) {
        return currentItems.get(slot);
    }

    public void onMenuButton(int buttonId) {
        InventoryButton button = InventoryButton.get(menu.getType(),buttonId);
        if (button == null) return;
        menu.onEvent(p,"events.click", button.toString(),button.getId()+"");
    }
}
