package io.github.tanguygab.armenu.commands;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.menus.menu.InventoryEnums.InventoryType;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.api.config.YamlConfigurationFile;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.world.item.ItemStack;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CreateCmd {

    public String name;
    public InventoryType type;
    public Map<Integer, ItemStack> items = new HashMap<>();
    public TabPlayer p;

    public CreateCmd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("You have to provide a name.");
            return;
        }
        name = args[1];
        if (ARMenu.get().getMenuManager().getMenus().contains(name)) {
            sender.sendMessage("This menu already exists.");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("You have to provide a type.");
            return;
        }
        String t = args[2];
        type = InventoryType.get(t);
        if (type == null) {
            sender.sendMessage("This type doesn't exist.");
            return;
        }

        p = TabAPI.getInstance().getPlayer(sender.getName());
        ARMenu.get().getMenuManager().creators.put(p,this);
        p.sendPacket(new PacketPlayOutOpenWindow(66,type.container, IChatBaseComponent.a("Menu Creator")));
    }

    public void addItem(ItemStack item, int slot) {
        if (slot >= type.getSize() || slot == -999 || slot == -1) return;

        for (int i : items.keySet()) {
            if (i != slot) continue;
            ItemStack it = items.get(i);
            if (CraftItemStack.asBukkitCopy(item).isSimilar(CraftItemStack.asBukkitCopy(it))) {
                int count = it.I() + item.I();
                if (count > 64) count = 64;
                it.e(count);
                return;
            }
        }
        items.put(slot,item);
    }

    public void close() {
        try {
            File file = new File(ARMenu.get().getDataFolder(),"/menus/"+name+".yml");
            file.createNewFile();
            ConfigurationFile config = new YamlConfigurationFile(null,file);

            Map<Integer, Character> chars = new HashMap<>();
            char c = 48;
            config.set("title",name);
            config.set("type",type.name.get(0));
            for (int slot : items.keySet()) {
                ItemStack item = items.get(slot);
                if (item == ItemStack.b) continue;
                chars.put(slot,c);
                org.bukkit.inventory.ItemStack i = CraftItemStack.asBukkitCopy(item);
                config.set("items."+c+".material",i.getType().toString());
                config.set("items."+c+".amount",i.getAmount());
                ItemMeta meta = i.getItemMeta();
                if (meta.hasDisplayName())
                    config.set("items."+c+".name",meta.getDisplayName());
                if (meta.hasLore())
                    config.set("items."+c+".lore",meta.getLore());
                for (Enchantment enchant : i.getEnchantments().keySet())
                    config.set("items."+c+".enchantments."+enchant.getKey().getKey(),i.getEnchantmentLevel(enchant));
                c++;
            }

            List<String> layout = new ArrayList<>();


            for (int i = 0; i < type.getSize(); i++) {

                String ch = chars.containsKey(i) ? chars.get(i)+"" : "";

                addChar(ch,i,layout);
            }
            config.set("pages.page1.menu-layout",layout);
            ((Player)p.getPlayer()).updateInventory();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addChar(String ch, int i, List<String> layout) {
        int line = i/9;
        while (layout.size() <= line) layout.add("");
        String str = layout.get(line);
        List<String> currentLine = new ArrayList<>(Arrays.asList(str.split(",")));
        int pos = i-9*line;
        while (currentLine.size() <= pos) currentLine.add("");
        currentLine.set(pos,ch);

        layout.set(line,String.join(",",currentLine));
    }


}
