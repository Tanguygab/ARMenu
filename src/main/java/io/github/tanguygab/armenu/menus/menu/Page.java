package io.github.tanguygab.armenu.menus.menu;

import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.menus.item.Item;
import me.neznamy.tab.api.TabPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Page {

    private final String name;
    private final Menu menu;
    private final Map<String,Object> config;
    private final List<Item> items = new ArrayList<>();
    private final List<Item> playerInvItems;
    private final List<Item> setslots = new ArrayList<>();

    public Page(String name, Menu menu, Map<String,Object> config) {
        this.name = name;
        this.menu = menu;
        this.config = config;

        createItems("menu-layout",items);
        if (!config.containsKey("player-layout") || ((List<String>)config.get("player-layout")).isEmpty())
            playerInvItems = null;
        else {
            playerInvItems = new ArrayList<>();
            createItems("player-layout",playerInvItems);
            while (playerInvItems.size() < 36) playerInvItems.add(null);
        }

        menu.getItems().forEach(item -> {
            if (!item.getSlots(this).isEmpty())
                setslots.add(item);
        });
    }

    public Menu getMenu() {
        return menu;
    }

    public int getLayoutSize() {
        return items.size();
    }

    public List<Item> getItems() {
        return items;
    }

    public List<Item> getPlayerInvItems() {
        return playerInvItems;
    }

    private void createItems(String path, List<Item> items) {
        List<String> rows = (List<String>) config.get(path);
        if (rows == null) return;

        for (String row : rows) {
            List<String> row2 = List.of(row.split(","));
            for (String i : row2) {
                Item item = menu.items.get(i);
                items.add(item);
            }
            if (row2.size() < 9) {
                for (int i = row2.size(); i < 9 ; i++)
                    items.add(null);
            }
        }
    }

    public String getName() {
        return name;
    }

    public NonNullList<ItemStack> getItems(TabPlayer p, int frame) {
        NonNullList<ItemStack> list = NonNullList.a();
        int slot = 0;
        for (Item item : items) {
            if (item == null) list.add(ItemStack.b);
            else list.add(item.getItem(frame,p,this,slot));
            slot++;
        }
        return list;
    }

    public NonNullList<ItemStack> getPlayerInvItems(TabPlayer p, int frame) {
        NonNullList<ItemStack> list = NonNullList.a();
        if (playerInvItems == null)
            return null;
        int slot = 0;
        for (Item item : playerInvItems) {
            if (item == null) list.add(ItemStack.b);
            else list.add(item.getItem(frame,p,this,slot));
            slot++;
        }
        return list;
    }

    public Map<Item,List<Integer>> getSetSlots(TabPlayer p, int frame) {
        Map<Item,List<Integer>> map = new HashMap<>();

        setslots.forEach(item->
                item.getSlots(this).forEach(list->{
                    String slot = list.get(Utils.frame(frame,list.size()));
                    if (slot == null) return;
                    slot = Utils.parsePlaceholders(slot,p);
                    try {
                        int i = Integer.parseInt(slot);
                        List<Integer> l = map.getOrDefault(item,new ArrayList<>());
                        l.add(i);
                        map.put(item,l);
                    } catch (Exception ignored) {}
        }));

        return map;
    }

    public void onOpen(TabPlayer p) {
        menu.onEvent(p,"pages."+name+".events.open",null);
    }

    public void onClose(TabPlayer p) {
        menu.onEvent(p,"pages."+name+".events.close",null);
    }
}
