package io.github.tanguygab.armenu.events;

import io.github.tanguygab.armenu.ARMenu;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventsManager extends TabFeature {

    private final ARMenu plugin;
    private final EventActions listener;

    public static Pattern blockSimplePattern =  Pattern.compile("(?<x>[0-9.-]+);(?<y>[0-9.-]+);(?<z>[0-9.-]+):(?<block>[a-z_]+)");
    public static Pattern blockConditionPattern = Pattern.compile("%where%=block;%block%=(?<block>[a-z ]+);%x%=(?<x>[0-9.-]+);%y%=(?<y>[0-9.-]+);%z%=(?<z>[0-9.-]+)");
    public List<String> blockSuggestions = new ArrayList<>();

    public static Pattern entitySimplePattern =  Pattern.compile("(?<x>[0-9.-]+);(?<y>[0-9.-]+);(?<z>[0-9.-]+):(?<entityType>[a-z_]+);(?<entityName>[^;]+)");
    public static Pattern entityConditionPattern = Pattern.compile("%entity%=(?<entityName>[^;]+);%entity-type%=(?<entityType>[a-z ]+);%x%=(?<x>[0-9.-]+);%y%=(?<y>[0-9.-]+);%z%=(?<z>[0-9.-]+)");
    public List<String> entitySuggestions = new ArrayList<>();

    public static Pattern itemSimplePattern =  Pattern.compile("(?<itemType>[a-z ]+);(?<itemName>[^;]+)");
    public static Pattern itemConditionPattern = Pattern.compile("%item%=(?<itemName>[^;]+);%item-type%=(?<itemType>[a-z_]+)");
    public List<String> itemSuggestions = new ArrayList<>();

    public EventsManager(ARMenu plugin) {
        super("ARMenu-EventsManager","&aARMenu-EventsManager&r");
        this.plugin = plugin;
        listener = new EventActions();
        load();
    }

    public Listener getListener() {
        return listener;
    }

    @Override
    public void load() {
        List<Object> clickcfg = (List<Object>) plugin.getMenuManager().config.getObject("event-based-actions.click");
        for (Object obj : clickcfg) {
            if (!(obj instanceof Map<?,?> map)) continue;
            String condition = map.get("condition")+"";
            Matcher blockMatcher = blockConditionPattern.matcher(condition);
            if (blockMatcher.find()) {
                String block = blockMatcher.group("block").replace(" ","_");
                String x = blockMatcher.group("x");
                String y = blockMatcher.group("y");
                String z = blockMatcher.group("z");
                blockSuggestions.add(x + ";" + y + ";" + z + ":" + block);
                continue;
            }
            Matcher itemMatcher = itemConditionPattern.matcher(condition);
            if (itemMatcher.find()) {
                String name = itemMatcher.group("itemName").replace(" ","_");
                String type = itemMatcher.group("itemType").replace(" ","_");
                itemSuggestions.add(type + ";" + name);
            }
        }
        List<Object> entitycfg = (List<Object>) plugin.getMenuManager().config.getObject("event-based-actions.entity-click");
        for (Object obj : entitycfg) {
            if (!(obj instanceof Map<?,?> map)) continue;
            String condition = map.get("condition")+"";
            Matcher entityMatcher = entityConditionPattern.matcher(condition);
            if (!entityMatcher.find()) continue;

            String name = entityMatcher.group("entityName").replace(" ","_");
            String type = entityMatcher.group("entityType").replace(" ","_");
            String x = entityMatcher.group("x");
            String y = entityMatcher.group("y");
            String z = entityMatcher.group("z");
            entitySuggestions.add(x + ";" + y + ";" + z + ":" + type + ";" + name);
        }
    }

    public List<String> getSuggestions(String[] args) {
        if (args.length == 2) return List.of("add","remove");
        if (args.length == 3) return List.of("block","entity","item");
        if (args.length == 4 && args[1].equals("remove")) {
            switch (args[2]) {
                case "block" -> {return blockSuggestions;}
                case "entity" -> {return entitySuggestions;}
                case "item" -> {return itemSuggestions;}
            }
        }
        return null;
    }

    @Override
    public void onJoin(TabPlayer p) {
        listener.onJoin(p);
    }

    public void addEvent(String type, String condition, String action) {
        String path = "event-based-actions."+(type.equalsIgnoreCase("entity") ? "entity-" : "")+"click";
        List<Object> list = (List<Object>) plugin.getMenuManager().config.getObject(path);
        for (Object obj : list) {
            if (!(obj instanceof Map<?,?> map)) continue;
            if (condition.equals(map.get("condition"))) {
                List<Object> actions = (List<Object>) map.get("actions");
                actions.add(action);
                plugin.getMenuManager().config.set(path,list);
                return;
            }
        }
        Map<String,Object> map = new HashMap<>();
        map.put("condition",condition);
        map.put("actions",List.of(action));
        if (!list.isEmpty() && list.get(list.size()-1).equals("return"))
            list.remove(list.get(list.size()-1));
        list.add(map);
        list.add("return");

        plugin.getMenuManager().config.set(path,list);
    }

    public boolean removeEvent(String type, String event) {
        String condition = toCondition(type,event);
        if (condition == null) return false;

        String path = "event-based-actions."+(type.equalsIgnoreCase("entity") ? "entity-" : "")+"click";
        List<Object> list = (List<Object>) plugin.getMenuManager().config.getObject(path);

        for (Object obj : list) {
            if (!(obj instanceof Map<?,?> map) || !condition.equals(map.get("condition"))) continue;
            list.remove(obj);
            plugin.getMenuManager().config.set(path,list);
            switch (type) {
                case "block" -> blockSuggestions.remove(event);
                case "entity" -> entitySuggestions.remove(event);
                case "item" -> itemSuggestions.remove(event);
            }
            return true;
        }
        return false;
    }

    public String toCondition(String type, String event) {
        String condition = null;
        switch (type) {
            case "block" -> {
                Matcher blockMatcher = blockSimplePattern.matcher(event);
                if (!blockMatcher.find()) return null;

                String block = blockMatcher.group("block").replace("_"," ");
                String x = blockMatcher.group("x");
                String y = blockMatcher.group("y");
                String z = blockMatcher.group("z");
                condition = "%where%=block;%block%="+block+";"+"%x%="+x+";"+"%y%="+y+";"+"%z%="+z;
            }
            case "entity" -> {
                Matcher entityMatcher = entitySimplePattern.matcher(event);
                if (!entityMatcher.find()) return null;

                String entityName = entityMatcher.group("entityName").replace("_"," ");
                String entityType = entityMatcher.group("entityType").replace("_"," ");
                String x = entityMatcher.group("x");
                String y = entityMatcher.group("y");
                String z = entityMatcher.group("z");
                condition = "%entity%="+entityName+";%entity-type%="+entityType+";"+"%x%="+x+";"+"%y%="+y+";"+"%z%="+z;
            }
            case "item" -> {
                Matcher itemMatcher = itemSimplePattern.matcher(event);
                if (!itemMatcher.find()) return null;
                String itemName = itemMatcher.group("itemName").replace("_"," ");
                String itemType = itemMatcher.group("itemType").replace("_"," ");
                condition = "%item%="+itemName+";%item-type%="+itemType;
            }
        }

        return condition;
    }
}
