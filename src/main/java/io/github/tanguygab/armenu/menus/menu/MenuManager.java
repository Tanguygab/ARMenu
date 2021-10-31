package io.github.tanguygab.armenu.menus.menu;

import io.github.tanguygab.armenu.ARMenu;
import me.neznamy.tab.api.PlaceholderManager;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.api.config.YamlConfigurationFile;
import me.neznamy.tab.api.TabFeature;
import net.minecraft.network.protocol.game.PacketPlayInCloseWindow;
import net.minecraft.network.protocol.game.PacketPlayInWindowClick;
import net.minecraft.world.inventory.InventoryClickType;
import net.minecraft.world.item.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class MenuManager extends TabFeature {

    public ConfigurationFile config;
    public Map<String,Menu> menus = new HashMap<>();

    public MenuManager() {
        super("&2ARMenu&r");
        try {
            config = new YamlConfigurationFile(ARMenu.class.getClassLoader().getResourceAsStream("config.yml"), new File(ARMenu.get().getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        load();
    }
    public List<String> getMenus() {
        return new ArrayList<>(menus.keySet());
    }
    public Menu getMenu(String name) {
        return menus.get(name);
    }

    @Override
    public void load() {
        try {
            Path path = Path.of(ARMenu.get().getDataFolder().getAbsolutePath()+"/menus");
            File[] files = path.toFile().listFiles();
            if (files == null) return;

            for (File file : files) {
                ConfigurationFile cfg = new YamlConfigurationFile(null, file);
                String name = file.getName().replace(".yml","");
                menus.put(name,new Menu(name,cfg));
            }

            PlaceholderManager pm = TabAPI.getInstance().getPlaceholderManager();
            pm.registerServerPlaceholder("%armenu_menus_all%",999999900,()->menus.size()+"");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unload() {}

    @Override
    public void onJoin(TabPlayer p) {
        String m = config.getString("auto-open.on-join","");
        if (m.equalsIgnoreCase("")) return;
        Menu menu = menus.get(m);
        if (menu == null) return;
        menu.openMenu(p);
    }

    public Menu getMenu(TabPlayer p) {
        String menuproperty = p.getProperty("armenu").get();
        if (menuproperty == null) return null;
        return getMenu(menuproperty);
    }

    @Override
    public boolean onPacketReceive(TabPlayer p, Object packet) {
        Menu menu;
        if (packet instanceof PacketPlayInWindowClick click && click.b() == 66 && (menu = getMenu(p)) != null) {
            int slot = click.c();
            int button = click.d();
            InventoryClickType mode = click.g();
            ItemStack item = click.e();
            //Int2ObjectMap<ItemStack> menuitems = click.f(); keeping this to know what it is in case I need it x)
            return menu.clicked(slot,button,mode,item,p);
        }
        if (packet instanceof PacketPlayInCloseWindow close && close.b() == 66 && (menu = getMenu(p)) != null) {
            if (!menu.onClose(p)) {
                menu.openMenu(p);
                return true;
            }
            p.setProperty(this,"armenu",null);
        }
        return false;

    }
}
