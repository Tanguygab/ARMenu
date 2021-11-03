package io.github.tanguygab.armenu.menus.menu;

import io.github.tanguygab.armenu.ARMenu;
import me.neznamy.tab.api.PlaceholderManager;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.api.config.YamlConfigurationFile;
import net.minecraft.network.protocol.game.PacketPlayInCloseWindow;
import net.minecraft.network.protocol.game.PacketPlayInEnchantItem;
import net.minecraft.network.protocol.game.PacketPlayInWindowClick;
import net.minecraft.world.inventory.InventoryClickType;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuManager extends TabFeature {

    public ConfigurationFile config;
    public Map<String, Menu> menus = new HashMap<>();
    public Map<TabPlayer, MenuSession> sessions = new HashMap<>();

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
        newMenuSession(p,menu);
    }

    public void newMenuSession(TabPlayer p, Menu menu) {
        MenuSession session = new MenuSession(p,menu);
        ARMenu.get().getMenuManager().sessions.put(p,session);
        session.openMenu();
    }

    public Menu getMenu(TabPlayer p) {
        String menuproperty = p.getProperty("armenu").get();
        if (menuproperty == null) return null;
        return getMenu(menuproperty);
    }

    @Override
    public boolean onCommand(TabPlayer p, String message) {
        String[] msg = message.split(" ");
        for (Menu menu : menus.values()) {
            if (menu.getCommands().contains(msg[0].substring(1))) {
                newMenuSession(p,menu);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onQuit(TabPlayer p) {
        sessions.remove(p);
    }

    @Override
    public boolean onPacketReceive(TabPlayer p, Object packet) {
        MenuSession session;
        if (packet instanceof PacketPlayInWindowClick click && click.b() == 66 && (session = sessions.get(p)) != null) {
            int slot = click.c();
            int button = click.d();
            InventoryClickType mode = click.g();
            //ItemStack item = click.e();
            //Int2ObjectMap<ItemStack> menuitems = click.f(); keeping this to know what it is in case I need it x)
            return session.onClickPacket(slot,button,mode);
        }
        if (packet instanceof PacketPlayInEnchantItem click && click.b() == 66 && (session = sessions.get(p)) != null) {
            int buttonId = click.c();
            session.onMenuButton(buttonId);
            return true;
        }
        if (packet instanceof PacketPlayInCloseWindow close && close.b() == 66 && (session = sessions.get(p)) != null) {
            session.onClosePacket();
            ((Player)p.getPlayer()).updateInventory();
        }
        return false;
    }
}
