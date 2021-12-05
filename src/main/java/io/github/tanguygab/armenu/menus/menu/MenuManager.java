package io.github.tanguygab.armenu.menus.menu;

import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.commands.CreateCmd;
import io.github.tanguygab.armenu.menus.item.ClickType;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.api.config.YamlConfigurationFile;
import me.neznamy.tab.shared.features.layout.skin.SkinManager;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.inventory.InventoryClickType;
import net.minecraft.world.item.ItemStack;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuManager extends TabFeature {

    public ConfigurationFile config;
    public Map<String, Menu> menus = new HashMap<>();
    public Map<TabPlayer, MenuSession> sessions = new HashMap<>();
    public Map<TabPlayer, CreateCmd> creators = new HashMap<>();
    public SkinManager skins;

    public MenuManager() {
        super("ARMenu","&2ARMenu&r");
        try {
            config = new YamlConfigurationFile(ARMenu.class.getClassLoader().getResourceAsStream("config.yml"), new File(ARMenu.get().getDataFolder(), "config.yml"));
            skins = new SkinManager("texture:f3d5e43de5d4177c4baf2f44161554473a3b0be5430998b5fcd826af943afe3");
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
            pm.registerServerPlaceholder("%armenu-menus-all%",999999900,()->menus.size()+"");
            pm.registerPlayerPlaceholder("%menu%",1000,p->sessions.containsKey(p) ? sessions.get(p).getMenu().getName() : "");
            pm.registerPlayerPlaceholder("%menu-page%",1000,p->sessions.containsKey(p) ? sessions.get(p).getPage().getName() : "");
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
        newMenuSession(p,menu,null, null);
    }

    public void newMenuSession(TabPlayer p, Menu menu, Page page, List<String> args) {
        MenuSession session = new MenuSession(p,menu,page,args);
        ARMenu.get().getMenuManager().sessions.put(p,session);
        session.openMenu();
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

        if (packet instanceof PacketPlayInWindowClick click && click.b() == 66 && creators.containsKey(p)) {
            CreateCmd creator = creators.get(p);
            ItemStack placed = ItemStack.b;
            try {
                Object map = click.getClass().getDeclaredMethod("f").invoke(click);
                placed = (ItemStack) map.getClass().getMethod("getOrDefault", int.class, Object.class).invoke(map,click.c(),ItemStack.b);
            }
            catch (Exception e) {e.printStackTrace();}
            creator.addItem(placed,click.c());
        }
        if (packet instanceof PacketPlayInCloseWindow click && click.b() == 66 && creators.containsKey(p)) {
            CreateCmd creator = creators.get(p);
            creator.close();
        }

        if (packet instanceof PacketPlayInWindowClick click && click.b() == 66 && (session = sessions.get(p)) != null) {
            int slot = click.c();
            int button = click.d();
            InventoryClickType mode = click.g();
            ItemStack item = click.e();
            Map<Integer,ItemStack> placed;
            try {
                placed = (Map<Integer, ItemStack>) click.getClass().getDeclaredMethod("f").invoke(click);
            } catch (Exception e) {
                placed = new HashMap<>();
            }

            ClickType clickType = ClickType.get(mode,button,slot);
            return session.onClickPacket(slot,clickType,item,placed);
        }
        if (packet instanceof PacketPlayInEnchantItem click && click.b() == 66 && (session = sessions.get(p)) != null) {
            int buttonId = click.c();
            session.onMenuButton(buttonId);
            return true;
        }
        if (packet instanceof PacketPlayInCloseWindow close && close.b() == 66 && (session = sessions.get(p)) != null) {
            session.onClose();
        }
        return false;
    }

    @Override
    public void onPacketSend(TabPlayer p, Object packet) {
        MenuSession session;
        if (packet instanceof PacketPlayOutSetSlot pickup && pickup.b() == 0 && (session = sessions.get(p)) != null) {
            session.pickedUpItem(pickup.c(),pickup.d());
        }
    }
}
