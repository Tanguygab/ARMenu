package io.github.tanguygab.armenu.menus.menu;

import io.github.tanguygab.armenu.menus.item.Item;
import me.neznamy.tab.api.TabPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Page {

    private final String name;
    private final Menu menu;
    private final Map<String,Object> config;
    private final List<Item> items;

    public Page(String name, Menu menu, Map<String,Object> config) {
        this.name = name;
        this.menu = menu;
        this.config = config;

        items = new ArrayList<>();
        List<String> rows = (List<String>) config.get("menu-layout");
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

    public List<Item> getItems() {
        return items;
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

    public Item getItemAtSlot(int slot) {
        if (items.size() <= slot) return null;
        return items.get(slot);
    }


    public void onOpen(TabPlayer p) {
        menu.onEvent(p,"pages."+name+".events.open");
    }

    public void onClose(TabPlayer p) {
        menu.onEvent(p,"pages."+name+".events.close");
    }

}
