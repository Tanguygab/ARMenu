package io.github.tanguygab.armenu.actions.items;

import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TakeItemAction extends Action {

    private final Pattern pattern = Pattern.compile("(i?)take-item:( )?");

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String getSuggestion() {
        return "take-item: <material>||<amount>||<name>||<lore>";
    }

    @Override
    public boolean replaceMatch() {
        return true;
    }

    @Override
    public void execute(String match, TabPlayer p) {
        if (p == null) return;
        String[] args = match.split("\\|\\|");
        String material = args[0].toUpperCase().replace(" ", "_");

        Material mat = Material.getMaterial(material);
        if (mat == null) return;

        int amt = Utils.parseInt(Utils.parsePlaceholders(args[1],p),1);

        String name = args.length > 2 ? Utils.parsePlaceholders(args[2],p) : null;

        List<String> lore = new ArrayList<>();
        if (args.length > 3)
            for (String line : args[3].split("\\\\n"))
                lore.add(Utils.parsePlaceholders(line,p));

        PlayerInventory inv = ((Player)p.getPlayer()).getInventory();
        ItemStack[] items = inv.getContents();
        for (int i = 0; i < items.length; i++) {
            if (amt <= 0) break;

            if (check(items[i],mat,name,lore)) {
                ItemStack found = items[i];
                if (found.getAmount() <= amt) {
                    inv.setItem(i,null);
                    amt = amt-found.getAmount();
                }
                else if (found.getAmount() > amt) {
                    found.setAmount(found.getAmount()-amt);
                    amt = 0;
                }
            }
        }
    }

    public boolean check(ItemStack item, Material mat, String name, List<String> lore) {
        if (item == null || item.getType() != mat) return false;
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        if (name != null && !meta.getDisplayName().equals(name)) return false;
        if (!lore.isEmpty()) {
            if (!meta.hasLore()) return false;
            List<String> itemLore = meta.getLore();
            if (itemLore == null) return false;
            if (lore.size() != itemLore.size()) return false;

            for (String line : itemLore) {
                int pos = itemLore.indexOf(line);
                if (!lore.get(pos).equals(line)) return false;
            }
        }

        return true;
    }

}
