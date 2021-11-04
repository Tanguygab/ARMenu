package io.github.tanguygab.armenu.menus.item;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import io.github.tanguygab.armenu.menus.menu.Page;
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

    private final List<String> names;
    private final List<String> amounts;
    private final List<String> materials;
    private final List<List<String>> lores;
    private final Map<String,List<List<String>>> slots;

    public Item(String name, Map<String,Object> config) {
        this.name = name;
        this.config = config;

        names = getNames();
        amounts = getAmounts();
        materials = getMaterials();
        lores = getLores();
        slots = getSlots();
    }

    public String getConfigName() {
        return name;
    }

    public List<List<String>> getSlots(Page page) {
        List<List<String>> list = new ArrayList<>();
        if (slots.containsKey(page.getName()))
            list.addAll(slots.get(page.getName()));
        if (slots.containsKey("__ALL__"))
            list.addAll(slots.get("__ALL__"));
        return list;
    }

    private List<String> getNames() {
        Object name = config.get("name");
        if (name == null) return List.of();

        if (name instanceof String)
            return List.of(name+"");
        return (List<String>) name;
    }

    private List<String> getAmounts() {
        Object amount = config.get("amount");
        if (amount == null) return List.of();

        if (amount instanceof String || amount instanceof Integer)
            return List.of(amount+"");
        List<String> list = new ArrayList<>();
        ((List<?>)amount).forEach(i->list.add(i+""));
        return list;
    }
    private List<String> getMaterials() {
        Object mat = config.get("material");
        if (mat == null) return List.of();

        if (mat instanceof String)
            return List.of(mat+"");
        return (List<String>) mat;
    }

    private List<List<String>> getLores() {
        if (config.containsKey("lore")) {
            List<?> lore = (List<?>) config.get("lore");
            if (lore.isEmpty()) return List.of(List.of());
            if (lore.get(0) instanceof List<?>) return (List<List<String>>) lore;
            else return List.of((List<String>)lore);
        }
        return List.of(List.of());
    }

    private Map<String,List<List<String>>> getSlots() {
        Map<String,List<List<String>>> map = new HashMap<>();

        if (!config.containsKey("slot")) {
            return map;
        };

        Object opt = config.get("slot");
        if (opt instanceof String || opt instanceof Integer) {
            map.put("__ALL__",List.of(List.of(opt+"")));
            return map;
        }
        Map<String,Object> slots = (Map<String, Object>) opt;
        if (slots.isEmpty()) return map;

        slots.forEach((page,slot)->{
            List<List<String>> list = new ArrayList<>();
            ((List<?>)slot).forEach(s -> {
                if (s instanceof List<?>)
                    list.add((List<String>) s);
                else list.add(List.of(s+""));
            });

            map.put(page,list);
        });

        return map;
    }

    public net.minecraft.world.item.ItemStack getItem(int frame, TabPlayer p, Page page, int slot) {
        if (materials.isEmpty()) return net.minecraft.world.item.ItemStack.b;

        String m = placeholders(materials.get(frame),p,page,slot).replace(" ", "_").toUpperCase();
        Material m2 = Material.getMaterial(m);
        if (m2 == null) return net.minecraft.world.item.ItemStack.b;
        ItemStack item = new ItemStack(m2);

        if (!amounts.isEmpty()) {
            String amount = placeholders(amounts.get(frame),p,page,slot);
            try {item.setAmount(Math.round(Float.parseFloat(amount)));}
            catch (Exception ignored) {}
        }
        ItemMeta meta = item.getItemMeta();
        if (!names.isEmpty())
            meta.setDisplayName(placeholders(names.get(frame),p,page,slot));
        if (!lores.isEmpty()) {
            List<String> lore = new ArrayList<>(lores.get(frame));
            lore.forEach(l->lore.set(lore.indexOf(l),placeholders(l,p,page,slot)));
            meta.setLore(lore);
        }
        item.setItemMeta(meta);


        return CraftItemStack.asNMSCopy(item);
    }

    protected String placeholders(String text, TabPlayer p, Page page, int slot) {
        return Utils.parsePlaceholders(text
                .replace("%page%",page.getName())
                .replace("%slot%",slot+""),p);
    }

    public List<Map<Action,String>> getClickActions(int button, InventoryClickType mode, TabPlayer p, int slot) {
        List<Map<Action,String>> list = new ArrayList<>();
        Map<String,Object> actions = (Map<String, Object>) config.get("actions");
        if (actions == null) return list;

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
                        list.add(Utils.map(el));
                    if (element instanceof Map<?,?> condmap) {
                        String cond = condmap.get("condition")+"";
                        Condition condition = Condition.getCondition(cond);
                        String section = (condition.isMet(p) ? "" : "deny-") + "actions";
                        if (!condmap.containsKey(section)) continue;
                        ((List<String>)condmap.get(section)).forEach(str->list.add(Utils.map(str)));
                    }
                }
            }
        }
        return list;
    }

}
