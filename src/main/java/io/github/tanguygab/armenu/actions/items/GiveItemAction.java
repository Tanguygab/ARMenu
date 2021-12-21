package io.github.tanguygab.armenu.actions.items;

import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GiveItemAction extends Action {

    private final Pattern pattern = Pattern.compile("(i?)give-item:( )?");

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String getSuggestion() {
        return "give-item: <material>||<amount>||<name>||<lore>";
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
        ItemStack item = new ItemStack(mat);

        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        switch (args.length) {
            default:
            case 4: {
                List<String> lore = new ArrayList<>();
                for (String line : args[3].split("\\\\n"))
                    lore.add(Utils.parsePlaceholders(line,p));
                meta.setLore(lore);
            }
            case 3: meta.setDisplayName(Utils.parsePlaceholders(args[2],p));
            case 2: item.setAmount(Utils.parseInt(Utils.parsePlaceholders(args[1],p),1));
            case 1:
        }
        item.setItemMeta(meta);
        ((Player)p.getPlayer()).getInventory().addItem(item);
    }
}
