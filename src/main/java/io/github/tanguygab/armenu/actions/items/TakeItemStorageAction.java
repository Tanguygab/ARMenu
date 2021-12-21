package io.github.tanguygab.armenu.actions.items;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Pattern;

public class TakeItemStorageAction extends Action {

    private final Pattern pattern = Pattern.compile("(i?)take-item-storage:( )?");

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String getSuggestion() {
        return "take-item-storage: <name>";
    }

    @Override
    public boolean replaceMatch() {
        return true;
    }

    @Override
    public void execute(String match, TabPlayer player) {
        if (player == null) return;

        String[] args = match.split(" ");
        String name = args[0];
        int amt = 1;
        if (args.length > 1)
            amt = Utils.parseInt(args[1],1);

        ItemStack item = ARMenu.get().getItemStorage().getItem(name);
        if (item == null) return;

        Player p = (Player) player.getPlayer();
        ItemStack[] items = p.getInventory().getContents();
        for (int i = 0; i < items.length; i++) {
            if (amt <= 0) break;

            if (item.isSimilar(items[i])) {
                ItemStack found = items[i];
                if (found.getAmount() <= amt) {
                    p.getInventory().setItem(i,null);
                    amt = amt-found.getAmount();
                }
                else if (found.getAmount() > amt) {
                    found.setAmount(found.getAmount()-amt);
                    amt = 0;
                }
            }
        }
    }

}
