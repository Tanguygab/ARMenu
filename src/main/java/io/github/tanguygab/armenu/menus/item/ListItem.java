package io.github.tanguygab.armenu.menus.item;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import io.github.tanguygab.armenu.menus.menu.Menu;
import io.github.tanguygab.armenu.menus.menu.Page;
import me.neznamy.tab.api.TabPlayer;

import java.util.*;

public class ListItem extends Item {

    private int autoIncrement = 0;
    private boolean loop = false;
    private boolean vertical = false;
    private String input;
    private String separator;
    private final Map<TabPlayer,Integer> currentIncrement = new HashMap<>();

    public ListItem(String name, Map<String,Object> config) {
        super(name,config);
        getList();
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

        String in = Utils.parsePlaceholders(input
                .replace("%page%",page.getName())
                .replace("%slot%",slot+"")
                ,p);

        String[] list = in.split(separator);

        List<String> l = getLayoutSlots(page.getMenu());

        //if (vertical) {}

        int pos = l.indexOf(page.getName()+" ||| "+slot)+currentIncrement.getOrDefault(p,0);
        if (pos < 0) return new String[]{"0",""};
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
        Map<String,String> replacements = replacements(p,page,slot,listPos,listItem);

        List<String> lore = new ArrayList<>(this.lores.get(Utils.frame(frame,lores.size())));
        lore.forEach(l->lore.set(lore.indexOf(l),placeholders(l,p,replacements)));

        Map<String,String> enchants = new HashMap<>(this.enchants);
        enchants.forEach((enchant,lvl)->enchants.put(placeholders(enchant,p,replacements),placeholders(lvl,p,replacements)));

        List<String> flags = new ArrayList<>(this.flags);
        flags.forEach(flag->flags.set(flags.indexOf(flag),placeholders(flag,p,replacements)));

        Map<String,Map<String,String>> attributes = new HashMap<>(this.attributes);
        attributes.forEach((attribute,cfg)->{
            cfg.forEach((opt,value)->{
                cfg.put(placeholders(opt,p,replacements),placeholders(value,p,replacements));
            });
            attributes.put(placeholders(attribute,p,replacements),cfg);
        });

        return getItem(placeholders(materials.get(Utils.frame(frame,materials.size())),p,replacements),
                names.isEmpty() ? null : placeholders(names.get(Utils.frame(frame,names.size())),p,replacements),
                amounts.isEmpty() ? null : placeholders(amounts.get(Utils.frame(frame,amounts.size())),p,replacements),
                lore,
                enchants,
                flags,
                attributes,
                slot
                );
    }

    private Map<String,String> replacements(TabPlayer p, Page page, int slot, String listPos, String listItem) {
        Map<String,String> replacements = replacements(p,page,slot);
        replacements.put("%list-pos%",listPos);
        replacements.put("%list-item%",listItem);
        return replacements;
    }

    @Override
    public List<Map<Action,String>> getClickActions(ClickType clickType, TabPlayer p, int slot, Page page) {
        List<Map<Action,String>> list = new ArrayList<>();
        Map<String,Object> actions = (Map<String, Object>) config.get("actions");
        if (actions == null) return list;

        String[] listList = getItemInList(p, slot, page);
        String listPos = listList[0];
        String listItem = listList[1];
        Map<String,String> replacements = replacements(p,page,slot,listPos,listItem);
        replacements.put("%click%",clickType+"");

        for (String type : actions.keySet()) {
            for (String type2 : type.split(",")) {
                ClickType click = ClickType.get(type2);
                if (click == null) {
                    ARMenu.get().getLogger().info("Click type "+type2+" does not exist! Skipped");
                    continue;
                }
                if (click == clickType) continue;
                page.getMenu().onEvent(p,"items."+name+".actions."+type,replacements);
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
