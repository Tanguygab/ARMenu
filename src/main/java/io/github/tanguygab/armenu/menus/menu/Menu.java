package io.github.tanguygab.armenu.menus.menu;

import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import io.github.tanguygab.armenu.menus.item.Item;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

import java.util.*;

public class Menu {

    private final String name;
    public final ConfigurationFile config;

    private InventoryType type = null;
    private final List<String> commands = new ArrayList<>();
    private final List<String> titles = new ArrayList<>();
    private final Map<String,Page> pages = new LinkedHashMap<>();
    public final Map<String,Item> items = new HashMap<>();

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
    public Map<String,Page> getPages() {
        return pages;
    }
    public List<String> getCommands() {
        return commands;
    }
    public InventoryType getType() {
        return type;
    }

    public List<Item> getItems() {
        return new ArrayList<>(items.values());
    }

    public boolean onOpen(TabPlayer p) {
        return onEvent(p,"events.open");
    }

    public boolean onClose(TabPlayer p) {
        return onEvent(p,"events.close");
    }

    public boolean onEvent(TabPlayer p, String path) {
        if (!config.hasConfigOption(path)) return true;

        List<Object> cfg = (List<Object>) config.getObject(path);

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


    public void createMenu() {
        if (config.hasConfigOption("type"))
            type = InventoryType.get(config.getString("type"));
        if (config.hasConfigOption("title")) {
            Object title = config.getObject("title");
            if (title instanceof List<?>)
                titles.addAll((List<String>)title);
            else titles.add(title+"");
        }
        if (config.hasConfigOption("commands")) {
            Object command = config.getObject("commands");
            if (command instanceof String)
                commands.add(command+"");
            if (command instanceof List<?>)
                commands.addAll((List<String>) command);
        }
        if (config.hasConfigOption("items")) {
            Map<String,Map<String, Object>> list = config.getConfigurationSection("items");
            list.forEach((item,itemcfg)->items.put(item,new Item(item, itemcfg)));
        }
        if (config.hasConfigOption("pages")) {
            Map<String, Map<String,Object>> pages = config.getConfigurationSection("pages");
            pages.forEach((page,cfg)-> this.pages.put(page,new Page(page,this,cfg)));

        }
    }
}
