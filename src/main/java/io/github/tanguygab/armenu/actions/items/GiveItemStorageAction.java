package io.github.tanguygab.armenu.actions.items;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Pattern;

public class GiveItemStorageAction extends Action {

    private final Pattern pattern = Pattern.compile("(i?)give-item-storage:( )?");

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public String getSuggestion() {
        return "give-item-storage: <name>";
    }

    @Override
    public boolean replaceMatch() {
        return true;
    }

    @Override
    public void execute(String match, TabPlayer p) {
        if (p == null) return;

        String[] args = match.split(" ");
        String name = args[0];
        int amt = 1;
        if (args.length > 1)
            amt = Utils.parseInt(args[1],1);

        ItemStack item = ARMenu.get().getItemStorage().getItem(name);
        if (item == null) return;

        Player player = ((Player)p.getPlayer());
        for (int i = 0; i < amt; i++)
            player.getInventory().addItem(item);

    }

}
