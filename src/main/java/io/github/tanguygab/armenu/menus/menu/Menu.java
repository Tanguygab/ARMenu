package io.github.tanguygab.armenu.menus.menu;

import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import io.github.tanguygab.armenu.menus.item.Item;
import io.github.tanguygab.armenu.menus.item.ListItem;
import io.github.tanguygab.armenu.menus.menu.InventoryEnums.InventoryProperty;
import io.github.tanguygab.armenu.menus.menu.InventoryEnums.InventoryType;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

import java.util.*;

public class Menu {

    private final String name;
    public final ConfigurationFile config;

    private InventoryType type = null;
    private final Map<InventoryProperty,Object> properties = new HashMap<>();
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
    public String getTitle(int frame) {
        return titles.get(Utils.frame(frame,titles.size()));
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

    public Map<InventoryProperty,Object> getInventoryProperties() {
        return properties;
    }

    public List<Item> getItems() {
        return new ArrayList<>(items.values());
    }

    public Map<String,Item> getItemsMap() {
        return items;
    }

    public boolean onOpen(TabPlayer p) {
        return onEvent(p,"events.open",null);
    }

    public boolean onClose(TabPlayer p) {
        return onEvent(p,"events.close", null);
    }


    public boolean onEvent(TabPlayer p, String path, String click, String where) {
        Map<String,String> map = new HashMap<>();
        map.put("%click%",click);
        map.put("%where%",where);
        return onEvent(p,path,map);
    }

    public boolean onEvent(TabPlayer p, String path, Map<String,String> replacements) {
        if (!config.hasConfigOption(path)) return true;

        List<Object> cfg = (List<Object>) config.getObject(path);

        List<Map<Action,String>> list = new ArrayList<>();
        for (Object element : cfg) {
            if (element instanceof String el)
                list.add(Utils.map(Utils.replacements(el,replacements)));
            if (element instanceof Map<?,?> condmap) {
                String cond = condmap.get("condition")+"";
                Condition condition = Condition.getCondition(Utils.replacements(cond,replacements));
                String section = (condition.isMet(p) ? "" : "deny-") + "actions";
                if (!condmap.containsKey(section)) continue;
                ((List<String>)condmap.get(section)).forEach(str->list.add(Utils.map(Utils.replacements(str,replacements))));
            }
        }

        for (Map<Action,String> map : list) {
            for (Action ac : map.keySet()) {
                String str = map.get(ac);
                if (str.equalsIgnoreCase("stop")) return false;
                if (str.equalsIgnoreCase("continue")) return true;
                if (str.equalsIgnoreCase("close") && path.equals("events.close"))
                    continue;
                Action.execute(str,ac,p);
            }
        }


        return true;
    }


    public void createMenu() {
        if (config.hasConfigOption("type"))
            type = InventoryType.get(config.getString("type"));
        if (config.hasConfigOption("menu-properties")) {
            Map<String,Object> props = config.getConfigurationSection("menu-properties");
            props.forEach((prop,value) -> {
                InventoryProperty property = InventoryProperty.get(prop);
                if (property != null && property.getTypes().contains(type))
                    properties.put(property,value);
            });
        }
        if (config.hasConfigOption("title")) {
            Object title = config.getObject("title");
            if (title instanceof List<?>)
                titles.addAll((List<String>) title);
            else titles.add(title + "");
        }
        if (config.hasConfigOption("commands")) {
            Object command = config.getObject("commands");
            if (command instanceof String)
                commands.add(command + "");
            if (command instanceof List<?>)
                commands.addAll((List<String>) command);
        }
        if (config.hasConfigOption("items")) {
            Map<String, Map<String, Object>> list = config.getConfigurationSection("items");
            list.forEach((item, itemcfg) -> {
                Item i;
                if (itemcfg.containsKey("type") && itemcfg.get("type").toString().equalsIgnoreCase("LIST"))
                    i = new ListItem(item, itemcfg);
                else i = new Item(item, itemcfg);
                items.put(item, i);
            });
        }
        if (config.hasConfigOption("pages")) {
            Map<String, Map<String, Object>> pages = config.getConfigurationSection("pages");
            pages.forEach((page, cfg) -> this.pages.put(page, new Page(page, this, cfg)));
        } else
            pages.put("0", new Page("0",this,new HashMap<>()));
    }
}
