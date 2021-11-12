package io.github.tanguygab.armenu.menus.item;

import io.github.tanguygab.armenu.actions.Action;
import io.github.tanguygab.armenu.menus.menu.Page;
import me.neznamy.tab.api.TabPlayer;
import net.minecraft.world.inventory.InventoryClickType;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;

import java.util.List;
import java.util.Map;

public class InvItem extends Item {

    public final org.bukkit.inventory.ItemStack itemStack;

    public InvItem(org.bukkit.inventory.ItemStack item, int slot) {
        super("inv-"+slot,null);
        itemStack = item;
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
        return CraftItemStack.asNMSCopy(itemStack);
    }

    @Override
    public List<Map<Action, String>> getClickActions(int button, InventoryClickType mode, TabPlayer p, int slot, Page page) {
        return List.of();
    }
}
