package io.github.tanguygab.armenu.commands;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.events.EventsManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventsCmd {

    public EventsCmd(CommandSender sender, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("You have to be a player to use that!");
            return;
        }
        if (args.length < 2) {
            p.sendMessage("You have to provide an action!");
            return;
        }
        String action = args[1];
        if (args.length < 3) {
            p.sendMessage("You have to provide a type of event!");
            return;
        }
        String type = args[2];

        EventsManager events = ARMenu.get().getEventsManager();
        if (action.equalsIgnoreCase("add")) {
            String condition = getCondition(type, p);
            if (condition == null) return;

            if (args.length < 4) {
                p.sendMessage("You have to provide an action to execute on click!");
                return;
            }
            List<String> args2 = new ArrayList<>(Arrays.asList(args));
            args2.remove(0);args2.remove(0);args2.remove(0);
            String ex = String.join(" ",args2);
            events.addEvent(type,condition,ex);
            p.sendMessage("Event added!");
            return;
        }
        if (args.length < 4) {
            p.sendMessage("You have to provide an event to remove!");
            return;
        }
        boolean removed = events.removeEvent(type,args[3]);
        if (removed) p.sendMessage("Event removed!");
        else p.sendMessage("This event doesn't exist!");
    }

    public String getCondition(String type, Player p) {
        switch (type) {
            case "block" -> {
                Block block = p.getTargetBlockExact(5);
                if (block == null) {
                    p.sendMessage("You have to look at a block!");
                    return null;
                }
                String blockType = block.getType().getKey().getKey().toLowerCase().replace("_", " ");
                Location loc = block.getLocation();
                String x = loc.getX()+"";
                String y = loc.getY()+"";
                String z = loc.getZ()+"";
                return "%where%=block;%block%="+blockType+";"+"%x%="+x+";"+"%y%="+y+";"+"%z%="+z;
            }
            case "entity" -> {
                Entity entity = getLookingAt(p);
                if (entity == null) {
                    p.sendMessage("You have to look at an entity!");
                    return null;
                }
                String entityType = entity.getType().getKey().getKey().toLowerCase().replace("_", " ");
                String entityName = entity.getCustomName() == null ? entityType : entity.getCustomName();
                Location loc = entity.getLocation();
                String x = loc.getX()+"";
                String y = loc.getY()+"";
                String z = loc.getZ()+"";
                return "%entity%="+entityName+";%entity-type%="+entityType+";"+"%x%="+x+";"+"%y%="+y+";"+"%z%="+z;
            }
            case "item" -> {
                ItemStack item = p.getInventory().getItemInMainHand();
                if (item.getType().isAir()) {
                    p.sendMessage("You have to hold an item!");
                    return null;
                }
                String itemType = item.getType().getKey().getKey().toLowerCase().replace("_", " ");
                String itemName = item.getItemMeta() != null && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : itemType;
                return "%item%="+itemName+";%item-type%="+itemType;
            }
        }
        return "";
    }

    // https://www.spigotmc.org/threads/tutorial-get-the-entity-another-entity-is-looking-at.202495/
    // thank you x)
    private Entity getLookingAt(Player player) {
        Vector playerLookDir = player.getEyeLocation().getDirection();
        Vector playerEyeLoc = player.getEyeLocation().toVector();

        for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
            Vector entityLoc = entity.getLocation().toVector();
            Vector playerEntityVec = entityLoc.subtract(playerEyeLoc);
            float angle = playerLookDir.angle(playerEntityVec);
            if (angle > 0.2f) return entity;
        }
        return null;
    }

}
