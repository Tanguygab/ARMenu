package io.github.tanguygab.armenu.menus.item;

import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import io.github.tanguygab.armenu.menus.menu.Page;
import me.neznamy.tab.api.TabPlayer;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public class InvItem extends Item {

    public final org.bukkit.inventory.ItemStack itemStack;

    public InvItem(org.bukkit.inventory.ItemStack item, int slot) {
        super("inv-"+slot,null);
        itemStack = item;
    }
    public InvItem(ItemStack item, int slot) {
        super("inv-"+slot,null);
        itemStack = Utils.asBukkitCopy(item);
    }

    @Override
    public Map<String, List<List<String>>> getSlots() {
        return Map.of();
    }

    @Override
    public boolean isMovable() {
        return true;
    }

    public InvItem split(int amount, int slot) {
        org.bukkit.inventory.ItemStack newItemStack = itemStack.clone();
        if (newItemStack.getType() != Material.AIR)
            newItemStack.setAmount(amount);
        return new InvItem(newItemStack,slot);
    }

    @Override
    public ItemStack getItem(int frame, TabPlayer p, Page page, int slot) {
        return getItem();
    }

    public ItemStack getItem() {
        return Utils.asNMSCopy(itemStack);
    }

    @Override
    public List<Map<Action, String>> getClickActions(ClickType clickType, TabPlayer p, int slot, Page page) {
        return List.of();
    }
}
