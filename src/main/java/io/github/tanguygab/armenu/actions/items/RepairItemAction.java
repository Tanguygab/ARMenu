package io.github.tanguygab.armenu.actions.items;

import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;

import java.util.regex.Pattern;

public class RepairItemAction extends Action {

    private final Pattern pattern = Pattern.compile("(i?)repair-item:( )?");

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String getSuggestion() {
        return "repair-item: <slot>";
    }

    @Override
    public boolean replaceMatch() {
        return true;
    }

    @Override
    public void execute(String match, TabPlayer p) {
        if (p == null) return;
        PlayerInventory inv =  ((Player)p.getPlayer()).getInventory();
        int slot = Utils.parseInt(match,-1);
        if (slot == -1) return;
        ItemStack item = inv.getItem(slot);
        if (item == null || item.getType().isAir()) return;
        if (item.getItemMeta() instanceof Damageable d && d.hasDamage()) {
            d.setDamage(0);
            item.setItemMeta(d);
        }

    }
}
