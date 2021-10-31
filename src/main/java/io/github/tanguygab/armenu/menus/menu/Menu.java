package io.github.tanguygab.armenu.menus.menu;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import io.github.tanguygab.armenu.menus.item.Item;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import net.minecraft.core.NonNullList;
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
import java.util.Map;

public class Menu {

    private final String name;
    public final ConfigurationFile config;

    public final List<String> titles = new ArrayList<>();
    public final List<String> layouts = new ArrayList<>();
    public final List<Item> items = new ArrayList<>();

    public Menu(String name, ConfigurationFile config) {
        this.name = name;
        this.config = config;
        createMenu();
    }

    public String getName() {
        return name;
    }
    public List<String> getTitles() {
        return titles;
    }
    public List<String> getLayout() {
        return layouts;
    }

    public boolean onOpen(TabPlayer p) {
        return onEvent(p,"open");
    }

    public boolean onClose(TabPlayer p) {
        return onEvent(p,"close");
    }

    public boolean onEvent(TabPlayer p, String path) {
        if (!config.hasConfigOption("events."+path)) return true;

        List<Object> cfg = (List<Object>) config.getObject("events."+path);

        List<Map<Action,String>> list = new ArrayList<>();
        for (Object element : cfg) {
            if (element instanceof String el)
                list.add(Utils.map(el));
            if (element instanceof Map<?,?> condmap) {
                String cond = condmap.get("condition")+"";
                Condition condition = Condition.getCondition(cond);
                String section = (condition.isMet(p) ? "" : "deny-") + "actions";
                if (!condmap.containsKey(section)) continue;
                ((List<String>)condmap.get(section)).forEach(str->list.add(Utils.map(str)));
            }
        }

        boolean canOpen = true;
        for (Map<Action,String> map : list) {
            for (Action ac : map.keySet()) {
                String str = map.get(ac);
                if (str.equalsIgnoreCase("return")) {
                    canOpen = false;
                    continue;
                }
                Action.execute(str,ac,p);
            }
        }


        return canOpen;
    }

    public List<Item> getItems() {
        return items;
    }

    public void createMenu() {
        if (config.hasConfigOption("title")) {
            Object title = config.getObject("title");
            if (title instanceof List<?>)
                titles.addAll((List<String>)title);
            else titles.add(title+"");
        }
        if (config.hasConfigOption("items")) {
            Map<String,Object> list = config.getConfigurationSection("items");
            for (String item : list.keySet()) {
                items.add(new Item(item, (Map<String, Object>) list.get(item)));
            }
        }
    }

    public void openMenu(TabPlayer player) {
        if (!onOpen(player)) return;

        PlayerConnection p = ((CraftPlayer)player.getPlayer()).getHandle().b;

        IChatBaseComponent title = IChatBaseComponent.a(titles.get(0));
        PacketPlayOutOpenWindow open = new PacketPlayOutOpenWindow(66, Containers.f, title);
        player.setProperty(ARMenu.get().getMenuManager(),"armenu",name);
        p.sendPacket(open);

        NonNullList<ItemStack> list = NonNullList.a();
        items.forEach(item -> list.add(item.getItem(0,player)));
        PacketPlayOutWindowItems windowitems = new PacketPlayOutWindowItems(66, 1, list ,ItemStack.b);

        p.sendPacket(windowitems);
    }

    public Item getItemAtSlot(int slot) {
        return items.get(slot);
    }

    public boolean clicked(int slot, int button, InventoryClickType mode, ItemStack item, TabPlayer p) {
        items.forEach(i-> {
            if (getItemAtSlot(slot) == i)
                i.getClickActions(slot,button,mode,item,p).forEach(map->map.forEach((ac,str)->Action.execute(str,ac,p)));

        });
        return true;
    }
}
