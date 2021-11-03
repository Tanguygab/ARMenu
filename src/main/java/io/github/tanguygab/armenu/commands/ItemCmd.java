package io.github.tanguygab.armenu.commands;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.itemstorage.ItemStorage;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemCmd {

    private int amt;
    private String name;
    private CommandSender sender;
    private Player p;

    public ItemCmd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("You need to provide an action!");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("You need to provide a name!");
            return;
        }
        Player p = (Player) sender;
        int amt = 1;
        if (args[1].equalsIgnoreCase("give") || args[1].equalsIgnoreCase("take")) {
            if (args.length > 3)
                try {amt = Integer.parseInt(args[3]);}
                catch (Exception ignored) {}
            if (args.length > 4) {
                Player p2 = Bukkit.getServer().getPlayer(args[3]);
                if (p2 != null) p = p2;
            }
        }
        String action = args[1];
        this.amt = amt;
        this.name = args[2];
        this.sender = sender;
        this.p = p;

        ItemStorage is = ARMenu.get().getItemStorage();
        ItemStack item = is.getItem(name);
        ItemStack hand = p.getInventory().getItemInMainHand();
        switch (action) {
            case "save" -> {
                is.saveItem(name,hand);
                sendMsg("Saved",hand,"");
            }
            case "delete" -> {
                is.removeItem(name);
                sendMsg("Deleted",hand,"");
            }
            case "give" -> {
                if (item == null) sender.sendMessage("Unknown item");
                else {
                    for (int i = 0; i < amt; i++)
                        p.getInventory().addItem(item);
                    sendMsg("Gave",item,"to");
                }
            }
            case "take" -> {
                if (item == null) p.sendMessage("Unknown item");
                else {
                    for (int i = 0; i < amt; i++)
                        p.getInventory().remove(item);
                    sendMsg("Took",item,"from");
                }
            }
        }
    }

    public void sendMsg(String txt, ItemStack item, String txt2) {
        if (sender instanceof Player) {
            IChatBaseComponent sub = new IChatBaseComponent(name);
            sub.getModifier().onHoverShowItem(getItemStack(item));
            IChatBaseComponent comp = new IChatBaseComponent(txt+" "+amt+" ").addExtra(sub);
            if (!txt2.equals("")) comp.addExtra(new IChatBaseComponent(" "+txt2+" "+p.getName()));
            TAB.getInstance().getPlayer(((Player) sender).getUniqueId()).sendMessage(comp);
        }
        else sender.sendMessage(txt+" "+amt+" "+name+ (txt2.equals("") ? "" : " "+txt2+" "+p.getName()));
    }

    public String getItemStack(ItemStack item) {
        return CraftItemStack.asNMSCopy(item).save(new NBTTagCompound()).toString();
    }
}
