package io.github.tanguygab.armenu.menus.item;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import net.minecraft.world.inventory.InventoryClickType;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class Item {

    public final String name;
    public final Map<String,Object> config;

    public List<String> names;
    public List<String> amounts;
    public List<String> materials;
    public List<List<String>> lores;

    public Item(String name, Map<String,Object> config) {
        this.name = name;
        this.config = config;

        names = getNames();
        amounts = getAmounts();
        materials = getMaterials();
        lores = getLores();
    }

    public String getConfigName() {
        return name;
    }

    public List<String> getNames() {
        Object name = config.get("name");
        if (name == null) return List.of();

        if (name instanceof String)
            return List.of(name+"");
        return (List<String>) name;
    }

    public List<String> getAmounts() {
        Object amount = config.get("amount");
        if (amount == null) return List.of();

        if (amount instanceof String || amount instanceof Integer)
            return List.of(amount+"");
        List<String> list = new ArrayList<>();
        ((List<?>)amount).forEach(i->list.add(i+""));
        return list;
    }

    public List<String> getMaterials() {
        Object mat = config.get("material");
        if (mat == null) return List.of();


        if (mat instanceof String)
            return List.of(mat+"");
        return (List<String>) mat;
    }
    public List<List<String>> getLores() {
        if (config.containsKey("lore")) {
            List<?> lore = (List<?>) config.get("lore");
            if (lore.isEmpty()) return List.of(List.of());
            if (lore.get(0) instanceof List<?>) return (List<List<String>>) lore;
            else return List.of((List<String>)lore);
        }
        return List.of();
    }

    public net.minecraft.world.item.ItemStack getItem(int frame, TabPlayer p) {
        if (materials.isEmpty())
            return net.minecraft.world.item.ItemStack.b;

        String m = Utils.parsePlaceholders(materials.get(frame),p);
        Material m2 = Material.getMaterial(m);
        if (m2 == null) return net.minecraft.world.item.ItemStack.b;
        ItemStack item = new ItemStack(m2);

        if (!amounts.isEmpty()) {
            String amount = Utils.parsePlaceholders(amounts.get(frame),p);
            try {
                item.setAmount(Math.round(Float.parseFloat(amount)));
            } catch (Exception ignore) {}
        }
        ItemMeta meta = item.getItemMeta();
        if (!names.isEmpty())
            meta.setDisplayName(Utils.parsePlaceholders(names.get(frame),p));
        if (!lores.isEmpty()) {
            List<String> lore = new ArrayList<>(lores.get(frame));
            lore.forEach(l->lore.set(lore.indexOf(l),Utils.parsePlaceholders(l,p)));
            meta.setLore(lore);
        }
        item.setItemMeta(meta);


        return CraftItemStack.asNMSCopy(item);
    }

    public List<Map<Action,String>> getClickActions(int slot, int button, InventoryClickType mode, net.minecraft.world.item.ItemStack item, TabPlayer p) {
        List<Map<Action,String>> list = new ArrayList<>();
        Map<String,Object> actions = (Map<String, Object>) config.get("actions");
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
                        list.add(map(el));
                    if (element instanceof Map<?,?> condmap) {
                        String cond = condmap.get("condition")+"";
                        Condition condition = Condition.getCondition(cond);
                        String section = (condition.isMet(p) ? "" : "deny-") + "actions";
                        if (!condmap.containsKey(section)) continue;
                        ((List<String>)condmap.get(section)).forEach(str->list.add(map(str)));
                    }
                }
            }
        }
        return list;
    }

    private Map<Action,String> map(Object action) {
        Map<Action,String> map = new HashMap<>();
        map.put(Action.find(action+""),action+"");
        return map;
    }

}
