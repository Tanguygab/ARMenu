package io.github.tanguygab.armenu.events;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EventActions implements Listener {

    private final ConfigurationFile config;

    public EventActions() {
        config = ARMenu.get().getMenuManager().config;
    }

    public void onJoin(TabPlayer p) {
        if (disabled("player-join")) return;
        onEvent(p,"player-join",Map.of());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntity(PlayerInteractEntityEvent e) {
        if (disabled("entity-click")) return;

        Player p = e.getPlayer();
        Entity entity = e.getRightClicked();
        String type = entity.getType().getKey().getKey().replace("_"," ");
        String name = entity.getCustomName() == null ? type : entity.getCustomName();

        Map<String,String> map = new HashMap<>();
        map.put("%hand%",e.getHand().toString().toLowerCase().replace("_"," "));
        map.put("%entity%",name);
        map.put("%entity-type%",type);
        Location loc = entity.getLocation();
        map.put("%x%",loc.getX()+"");
        map.put("%y%",loc.getY()+"");
        map.put("%z%",loc.getZ()+"");

        e.setCancelled(onEvent(TabAPI.getInstance().getPlayer(p.getUniqueId()), "entity-click",map));
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(PlayerInteractEvent e) {
        if (disabled("click")) return;

        Player p = e.getPlayer();
        ItemStack item = e.getItem();
        String type = item == null ? "" : item.getType().getKey().getKey().replace("_"," ");
        String name = item == null ? "" : item.getItemMeta() == null || !item.getItemMeta().hasDisplayName() ? type : item.getItemMeta().getDisplayName();

        Map<String,String> map = new HashMap<>();
        map.put("%hand%",e.getHand().toString().toLowerCase().replace("_"," "));
        map.put("%item%",name);
        map.put("%item-type%",type);

        String action = e.getAction().toString().toLowerCase().replace("_"," ");
        String where = "";
        String block = "";
        String x = "";
        String y = "";
        String z = "";
        if (!action.equals("physical")) {
            where = action.split(" ")[2];
            action = action.replace(" "+where,"");
            if (e.getClickedBlock() != null && where.equals("block")) {
                block = e.getClickedBlock().getType().getKey().getKey().toLowerCase().replace("_", " ");
                Location loc = e.getClickedBlock().getLocation();
                x = loc.getX()+"";
                y = loc.getY()+"";
                z = loc.getZ()+"";
            }
        }
        map.put("%where%",where);
        map.put("%x%",x);
        map.put("%y%",y);
        map.put("%z%",z);
        map.put("%block%",block);
        map.put("%action%",action);

        e.setCancelled(onEvent(TabAPI.getInstance().getPlayer(p.getUniqueId()), "click",map));
    }

    private boolean disabled(String name) {
        return ((List<?>)config.getObject("event-based-actions."+name)).isEmpty();
    }

    public boolean onEvent(TabPlayer p, String path, Map<String,String> replacements) {
        path = "event-based-actions." + path;
        if (!config.hasConfigOption(path)) return false;

        List<Object> cfg = (List<Object>) config.getObject(path);

        List<Map<Action, String>> list = new ArrayList<>();
        for (Object element : cfg) {
            if (element instanceof String el)
                list.add(Utils.map(Utils.replacements(el, replacements)));
            if (element instanceof Map<?, ?> condmap) {
                String cond = condmap.get("condition") + "";
                Condition condition = Condition.getCondition(Utils.replacements(cond, replacements));
                String section = (condition.isMet(p) ? "" : "deny-") + "actions";
                if (!condmap.containsKey(section)) continue;
                ((List<String>) condmap.get(section)).forEach(str -> list.add(Utils.map(Utils.replacements(str, replacements))));
            }
        }

        for (Map<Action, String> map : list) {
            for (Action ac : map.keySet()) {
                String str = map.get(ac);
                if (str.equalsIgnoreCase("return"))
                    return false;
                Action.execute(str, ac, p);
            }
        }
        return true;
    }
}
