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
import net.minecraft.world.item.ItemStack;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class MenuSession {

    private final TabPlayer p;
    private final Menu menu;
    private RepeatingTask task;
    private double frame = 0;

    private Page page;
    private final List<String> arguments;
    private List<Packet<PacketListenerPlayOut>> lastSentPacket = null;
    private NonNullList<ItemStack> lastSentItems = null;
    private final List<PacketPlayOutWindowData> customInventoryProperties = new ArrayList<>();

    public final List<Integer> oldSetSlots = new ArrayList<>();
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
            if (ARMenu.get().getMenuManager().config.getBoolean("refresh",false))
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
        packets.forEach(packet->p.sendPacket(packet,mm));
        if (!refresh) {
            if (lastSentItems != null)
                p.sendPacket(new PacketPlayOutWindowItems(66, 1, lastSentItems, heldItemStack),mm);
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
        page.onOpen(p);
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
        int frame = (int) this.frame;

        List<Packet<PacketListenerPlayOut>> list = new ArrayList<>();

        IChatBaseComponent title = IChatBaseComponent.a(menu.getTitle(frame));
        InventoryType type = menu.getType() != null ? menu.getType() : InventoryType.get("" + page.getLayoutSize());
        if (type == null)
            type = InventoryType.NORMAL_54;
        list.add(new PacketPlayOutOpenWindow(66, type.container, title));

        NonNullList<ItemStack> pageItems = NonNullList.a();

        oldSetSlots.forEach(slot->currentItems.put(slot,null));
        oldSetSlots.clear();
        page.getSetSlots(p,frame).forEach((item,slots)-> slots.forEach(slot-> {
            currentItems.put(slot,item);
            oldSetSlots.add(slot);
        }));

        currentItems.forEach((slot, item) -> {
            if (item != null)
                pageItems.add(item.getItem(frame, p, page, slot));
            else pageItems.add(ItemStack.b);
        });
        lastSentItems = pageItems;

        list.addAll(getInventoryProperties());

        list.add(new PacketPlayOutWindowItems(66, 1, pageItems, ItemStack.b));

        this.frame = frame == 10000 ? 0 : this.frame+0.5;
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

    public ItemStack heldItemStack = ItemStack.b;
    public Item heldItem = null;
    public Map<Integer,ItemStack> placedItems = new HashMap<>();
    public int lastClickedSlot = -2;

    public void execute(Item item, ClickType click, int slot) {
        if (item == null) return;
        item.getClickActions(click,p,slot,page)
                .forEach(map->map
                        .forEach((ac,str)->Action.execute(str,ac,p)));
    }

    public boolean onClickPacket(int slot, ClickType click, ItemStack held, Map<Integer,ItemStack> placed) {
        menu.onEvent(p, "events.click", click+"", (slot + "").replace("-999", "out").replace("-1", "border"));

        if (slot == -999 || slot == -1) {
            sendPackets(false);
            return true;
        }

        if (click == ClickType.OFFHAND || click == ClickType.DROP_KEY || click == ClickType.CONTROL_DROP_KEY || click == ClickType.DOUBLE_CLICK) {
            execute(currentItems.get(slot),click,slot);
            sendPackets(false);
            return true;
        }


        if (click.getNames().get(0).startsWith("num_")) {
            List<Integer> slots = new ArrayList<>(placed.keySet());
            if (slots.isEmpty()) return true;
            int slot1 = slots.get(0);
            int slot2 = slots.get(1);
            org.bukkit.inventory.ItemStack itemStack1 = CraftItemStack.asBukkitCopy(placed.get(slot1));
            org.bukkit.inventory.ItemStack itemStack2 = CraftItemStack.asBukkitCopy(placed.get(slot2));
            Item item1 = currentItems.get(slot1);
            Item item2 = currentItems.get(slot2);
            execute(currentItems.get(slot),click,slot);
            if ((item1 != null && !item1.isMovable()) || (item2 != null && !item2.isMovable())) {
                sendPackets(false);
                return true;
            }
            PlayerInventory inv = ((Player)p.getPlayer()).getInventory();

            int index1 = slot1-page.getLayoutSize()+9;
            if (index1 >= 36 && index1 < 45) index1 -= 36;
            int index2 = slot2-page.getLayoutSize()+9;
            if (index2 >= 36 && index2 < 45) index2 -= 36;

            if (item2 instanceof InvItem) {
                inv.setItem(index1, itemStack1);
                currentItems.put(slot1,item2);
                lastSentItems.set(slot1,placed.get(slot1));
            } else inv.setItem(index1,new org.bukkit.inventory.ItemStack(Material.AIR));
            if (item1 instanceof InvItem) {
                inv.setItem(index2, itemStack2);
                currentItems.put(slot2,item1);
                lastSentItems.set(slot2,placed.get(slot2));
            } else inv.setItem(index2,new org.bukkit.inventory.ItemStack(Material.AIR));
            return true;
        }


        int heldCount = heldItemStack.I();
        placedItems.clear();

        Item currentHeldItem = currentItems.get(slot);
        if (currentHeldItem != null) {
            execute(currentHeldItem,click,slot);
            if (!currentHeldItem.isMovable()) {
                sendPackets(false);
                return true;
            }
        }


        placed.forEach((placedSlot, placedItem) -> {
            Item itemAtSlot = currentItems.get(placedSlot);
            execute(itemAtSlot,click,slot);

            if (itemAtSlot == null || itemAtSlot.isMovable()) {
                Item newItem = heldItem;
                if (heldItem instanceof InvItem item) {
                    if (item.itemStack != null && item.itemStack.getAmount() != heldCount) {
                        newItem = item.split(placedItem.I(), placedSlot);

                        int amt = heldItemStack.I() - placedItem.I();
                        if (amt < 0) amt = 1;
                        heldItemStack.e(amt);
                    }
                }



                currentItems.put(placedSlot, newItem);
                placedItems.put(placedSlot,placedItem);

                int index = placedSlot-page.getLayoutSize()+9;
                if (index >= 36 && index < 45) index -= 36;

                ((Player)p.getPlayer()).getInventory().setItem(index,newItem instanceof InvItem i ? i.itemStack : null);
            }
        });

        heldItemStack = held;
        heldItem = currentHeldItem;
        updateLastSentItems();
        return true;
    }

    public void updateLastSentItems() {
        placedItems.forEach(lastSentItems::set);
        sendPackets(false);
    }

    //still being worked on
    public void pickedUpItem(int slot, ItemStack item) {
        slot += page.getLayoutSize()-9;
        if (placedItems.containsKey(slot)) return;

        if (item == ItemStack.b) {
            currentItems.put(slot,new InvItem(item, slot));
            lastSentItems.set(slot,item);
            return;
        }

        if (currentItems.get(slot) == null || (currentItems.get(slot) instanceof InvItem invi && invi.getItem() == ItemStack.b)) {
            currentItems.put(slot, new InvItem(item, slot));
            lastSentItems.set(slot,item);
        }
    }

    public void onMenuButton(int buttonId) {
        InventoryButton button = InventoryButton.get(menu.getType(),buttonId);
        if (button == null) return;
        menu.onEvent(p,"events.click", button+"",button.getId()+"");
    }
}
