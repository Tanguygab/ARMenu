package io.github.tanguygab.armenu.actions.items;

import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.regex.Pattern;

public class EnchantItemAction extends Action {

    private final String type;
    private final Pattern pattern;

    private final String suggestion;

    public EnchantItemAction(String type) {
        this.type = type;
        pattern = Pattern.compile("(i?)"+type+"-enchant-item:( )?");

        if (type.equals("set"))
            suggestion = type+"-enchant-item: <slot> <enchant> <level>";
        else suggestion = type+"-enchant-item: <slot> <enchant> [level]";
    }

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String getSuggestion() {
        return suggestion;
    }

    @Override
    public boolean replaceMatch() {
        return true;
    }

    @Override
    public void execute(String match, TabPlayer p) {
        if (p == null) return;
        String[] args = match.split(" ");
        if (args.length < 2) return;

        PlayerInventory inv =  ((Player)p.getPlayer()).getInventory();
        int slot = Utils.parseInt(args[0],-1);
        if (slot == -1) return;
        ItemStack item = inv.getItem(slot);
        if (item == null || item.getType().isAir()) return;

        String enchant = args[1];
        Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft(enchant.toLowerCase()));
        if (ench == null) return;
        int lvl = Utils.parseInt(args[2],1);

        enchant(item,ench,lvl);
    }

    public void enchant(ItemStack item, Enchantment enchant, int lvl) {
        int oldlvl = item.getEnchantmentLevel(enchant);
        switch (type) {
            case "set" -> {
                if (lvl <= 0)
                    item.removeEnchantment(enchant);
                else item.addUnsafeEnchantment(enchant,lvl);
            }
            case "add" -> item.addUnsafeEnchantment(enchant,oldlvl+lvl);
            case "take" -> {
                int newlvl = oldlvl-lvl;
                if (newlvl <= 0)
                    item.removeEnchantment(enchant);
                else item.addUnsafeEnchantment(enchant,newlvl);
            }
        }
    }

}
