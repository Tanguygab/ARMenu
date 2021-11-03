package io.github.tanguygab.armenu.itemstorage;

import io.github.tanguygab.armenu.ARMenu;
import me.neznamy.tab.api.config.YamlConfigurationFile;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ItemStorage {
    public YamlConfigurationFile file;

    public ItemStorage() {
        try {
            file = new YamlConfigurationFile(ARMenu.class.getClassLoader().getResourceAsStream("items.yml"), new File(ARMenu.get().getDataFolder(), "items.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public List<String> getItems() {
        return new ArrayList<>(file.getValues().keySet());
    }

    public void saveItem(String name, ItemStack item) {
        file.set(name,item);
    }
    public ItemStack getItem(String name) {
        if (file.hasConfigOption(name) && file.getObject(name) instanceof ItemStack)
            return (ItemStack) file.getObject(name);
        return null;
    }
    public void removeItem(String name) {
        file.set(name,null);
    }

}
