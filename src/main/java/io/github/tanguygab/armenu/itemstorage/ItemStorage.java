package io.github.tanguygab.armenu.itemstorage;

import io.github.tanguygab.armenu.ARMenu;
import me.neznamy.tab.api.config.YamlConfigurationFile;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemStorage {
    public YamlConfigurationFile file;
    private final Map<String,ItemStack> items = new HashMap<>();

    public ItemStorage() {
        try {
            file = new YamlConfigurationFile(ARMenu.class.getClassLoader().getResourceAsStream("items.yml"), new File(ARMenu.get().getDataFolder(), "items.yml"));
        } catch (IOException e) {e.printStackTrace();}
        file.getValues().keySet().forEach(this::loadItem);
    }

    public void loadItem(String name) {
        if (!file.hasConfigOption(name)) return;
        ItemStack item = new ItemStack(Material.valueOf(file.getString(name+".mat")));
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        if (file.hasConfigOption(name+".name"))
            meta.setDisplayName(file.getString(name+".name"));
        if (file.hasConfigOption(name+".lore"))
            meta.setLore(file.getStringList(name+".lore"));

        List<String> flags = file.getStringList(name+".flags");
        flags.forEach(flag->meta.addItemFlags(ItemFlag.valueOf(flag)));

        Map<String,Integer> enchants = file.getConfigurationSection(name+".enchantments");
        enchants.forEach((enchant, lvl)-> meta.addEnchant(Enchantment.getByKey(NamespacedKey.minecraft(enchant)),lvl,true));

        item.setItemMeta(meta);

        items.put(name,item);
    }


    public Map<String,ItemStack> getItems() {
        return items;
    }

    public void saveItem(String name, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        if (meta.hasDisplayName())
            file.set(name+".name",meta.getDisplayName());
        file.set(name+".mat",item.getType()+"");
        if (meta.hasLore())
            file.set(name+".lore",meta.getLore());

        List<String> flags = new ArrayList<>();
        meta.getItemFlags().forEach(flag->flags.add(flag+""));
        file.set(name+".flags",flags);

        meta.getEnchants().forEach((enchant, lvl) -> file.set(name+".enchantments."+enchant.getKey().getKey(),meta.getEnchantLevel(enchant)));

        loadItem(name);
    }

    public ItemStack getItem(String name) {
        if (!items.containsKey(name)) return null;
        return items.get(name).clone();
    }
    public void removeItem(String name) {
        items.remove(name);
        file.set(name,null);
    }

}
