package io.github.tanguygab.armenu.menus.item;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import io.github.tanguygab.armenu.menus.menu.Menu;
import io.github.tanguygab.armenu.menus.menu.Page;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import net.minecraft.world.inventory.InventoryClickType;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ListItem extends Item{

    private final List<String> names;
    private final List<String> amounts;
    private final List<String> materials;
    private final List<List<String>> lores;
    private final Map<String,List<List<String>>> slots;

    private int autoIncrement = 0;
    private boolean loop = false;
    private boolean vertical = false;
    private String input;
    private String separator;
    private final Map<TabPlayer,Integer> currentIncrement = new HashMap<>();

    public ListItem(String name, Map<String,Object> config) {
        super(name,config);
        names = getNames();
        amounts = getAmounts();
        materials = getMaterials();
        lores = getLores();
        slots = getSlots();
        getList();
    }

    @Override
    public List<List<String>> getSlots(Page page) {
        List<List<String>> list = new ArrayList<>();
        if (slots.containsKey(page.getName()))
            list.addAll(slots.get(page.getName()));
        if (slots.containsKey("__ALL__"))
            list.addAll(slots.get("__ALL__"));
        return list;
    }

    private void getList() {
        Map<String,Object> list = (Map<String, Object>) config.get("list");
        if (list.containsKey("auto-increment")) autoIncrement = (int) list.get("auto-increment");
        if (list.containsKey("loop")) loop = (boolean) list.get("loop");
        if (list.containsKey("vertical")) vertical = (boolean) list.get("vertical");
        if (list.containsKey("input")) input = list.get("input")+"";
        if (list.containsKey("separator")) separator = list.get("separator")+"";
    }


    public List<String> getLayoutSlots(Menu menu) {
        List<String> list = new ArrayList<>();
        menu.getPages().forEach((str,page)->{
            int slot = 0;
            for (Item i : page.getItems()) {
                if (i != null && i.getConfigName().equals(name))
                    list.add(page.getName() + " ||| " + slot);
                slot++;
            }
        });
        return list;
    }

    public void setCurrentIncrement(TabPlayer p, int i) {
        if (i < 0) i = 0;
        currentIncrement.put(p,i);
    }

    public void updateCurrentIncrement(TabPlayer p, int i) {
        if (i+currentIncrement.getOrDefault(p,0) < 0) i = 0;
        setCurrentIncrement(p, i+currentIncrement.getOrDefault(p,0));
    }

    public String[] getItemInList(TabPlayer p, int slot, Page page) {

        String in = placeholders(input,p,page,slot);
        String[] list = in.split(separator);

        List<String> l = getLayoutSlots(page.getMenu());

        //if (vertical) {}

        int pos = l.indexOf(page.getName()+" ||| "+slot)+currentIncrement.getOrDefault(p,0);
        String item = "";
        if (pos < list.length)
            item = list[pos];
        else if (loop) {
            while (pos >= list.length)
                pos = pos-list.length;
            item = list[pos];
        }
        return new String[]{pos+"",item};
    }

    @Override
    public net.minecraft.world.item.ItemStack getItem(int frame, TabPlayer p, Page page, int slot) {
        if (materials.isEmpty()) return net.minecraft.world.item.ItemStack.b;

        String[] list = getItemInList(p, slot, page);
        String listPos = list[0];
        String listItem = list[1];

        String m = placeholders(materials.get(frame),p,page,slot,listPos,listItem);
        ItemStack item;

        if (isSkinMat(m)) {
            Object skin = ARMenu.get().getMenuManager().skins.getSkin(m);
            if (skin == null)
                return net.minecraft.world.item.ItemStack.b;
            item = getSkull(skin);
        } else {
            Material m2 = Material.getMaterial(m.replace(" ", "_").toUpperCase());
            if (m2 == null) return net.minecraft.world.item.ItemStack.b;
            item = new ItemStack(m2);
        }

        if (!amounts.isEmpty()) {
            String amount = placeholders(amounts.get(frame),p,page,slot,listPos,listItem);
            try {item.setAmount(Math.round(Float.parseFloat(amount)));}
            catch (Exception ignored) {}
        }
        ItemMeta meta = item.getItemMeta();
        if (!names.isEmpty())
            meta.setDisplayName(placeholders(names.get(frame),p,page,slot,listPos,listItem));
        if (!lores.isEmpty()) {
            List<String> lore = new ArrayList<>(lores.get(frame));
            lore.forEach(l->lore.set(lore.indexOf(l),placeholders(l,p,page,slot,listPos,listItem)));
            meta.setLore(lore);
        }
        item.setItemMeta(meta);


        return CraftItemStack.asNMSCopy(item);
    }

    private String placeholders(String text, TabPlayer p, Page page, Integer slot, String listPos, String listItem) {
        return Utils.parsePlaceholders(text
                .replace("%page%", page.getName())
                .replace("%slot%",slot+"")
                .replace("%list-pos%", listPos+"")
                .replace("%list-item%", listItem)
                ,p);
    }
    @Override
    public List<Map<Action,String>> getClickActions(int button, InventoryClickType mode, TabPlayer p, int slot, Page page) {
        List<Map<Action,String>> list = new ArrayList<>();
        Map<String,Object> actions = (Map<String, Object>) config.get("actions");
        if (actions == null) return list;

        String[] listList = getItemInList(p, slot, page);
        String listPos = listList[0];
        String listItem = listList[1];

        for (String type : actions.keySet()) {
            for (String type2 : type.split(",")) {
                ClickType click = ClickType.get(type2);
                if (click == null) {
                    ARMenu.get().getLogger().info("Click type "+type2+" does not exist! Skipped");
                    continue;
                }
                if (click.getMode() != mode || click.getButton() != button) continue;
                for (Object element : (List<Object>) actions.get(type)) {
                    if (element instanceof String el)
                        list.add(Utils.map(placeholders(el,page,slot,listPos,listItem)));
                    if (element instanceof Map<?,?> condmap) {
                        String cond = condmap.get("condition")+"";
                        Condition condition = Condition.getCondition(cond);
                        String section = (condition.isMet(p) ? "" : "deny-") + "actions";
                        if (!condmap.containsKey(section)) continue;
                        ((List<String>)condmap.get(section)).forEach(str->list.add(Utils.map(placeholders(str,page,slot,listPos,listItem))));
                    }
                }
            }
        }
        return list;
    }

    private String placeholders(String text, Page page, Integer slot, String listPos, String listItem) {
        return text.replace("%page%", page.getName())
                .replace("%slot%",slot+"")
                .replace("%list-pos%", listPos+"")
                .replace("%list-item%", listItem);
    }

}
