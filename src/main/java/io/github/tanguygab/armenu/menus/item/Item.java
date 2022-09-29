package io.github.tanguygab.armenu.menus.item;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import io.github.tanguygab.armenu.ARMenu;
import io.github.tanguygab.armenu.Utils;
import io.github.tanguygab.armenu.actions.Action;
import io.github.tanguygab.armenu.menus.menu.Page;
import io.th0rgal.oraxen.items.OraxenItems;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.Skin;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.chat.IChatBaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Item {

    public final String name;
    public final Map<String,Object> config;
    protected int refresh;

    protected List<String> names;
    protected List<String> amounts;
    protected List<String> materials;
    protected List<List<String>> lores;
    protected Map<String,List<List<String>>> slots;
    protected Map<String,String> enchants;
    protected List<String> flags;
    protected Map<String,Map<String,String>> attributes;
    protected boolean isMovable;

    private final static Pattern customModelDataPattern = Pattern.compile(",MODEL:(?<data>[0-9])");
    private final static Pattern potionEffectPattern = Pattern.compile(",EFFECT:(?<data>[a-zA-Z_]+)");
    private final static Pattern colorPattern = Pattern.compile(",COLOR:(?<data>([0-9]{1,3},){2}[0-9])");

    public Item(String name, Map<String,Object> config) {
        this.name = name;
        this.config = config;

        if (config == null) return;
        refresh = (int) config.getOrDefault("refresh",0);
        names = getNames();
        amounts = getAmounts();
        materials = getMaterials();
        lores = getLores();
        slots = getSlots();
        enchants = getEnchants();
        flags = getFlags();
        attributes = getAttributes();
        isMovable = (boolean) config.getOrDefault("movable", false);
    }

    public String getConfigName() {
        return name;
    }

    public List<List<String>> getSlots(Page page) {
        List<List<String>> list = new ArrayList<>();
        if (slots.containsKey(page.getName()))
            list.addAll(slots.get(page.getName()));
        if (slots.containsKey("_DEFAULT_"))
            list.addAll(slots.get("_DEFAULT_"));
        return list;
    }

    protected List<String> getNames() {
        Object name = config.get("name");
        if (name == null) return List.of();

        if (name instanceof String)
            return List.of(name+"");
        return (List<String>) name;
    }

    protected List<String> getAmounts() {
        Object amount = config.get("amount");
        if (amount == null) return List.of();

        if (amount instanceof String || amount instanceof Integer)
            return List.of(amount+"");
        List<String> list = new ArrayList<>();
        ((List<?>)amount).forEach(i->list.add(i+""));
        return list;
    }
    protected List<String> getMaterials() {
        Object mat = config.get("material");
        if (mat == null) return List.of();

        if (mat instanceof String)
            return List.of(mat+"");
        return (List<String>) mat;
    }

    protected List<List<String>> getLores() {
        if (config.containsKey("lore")) {
            List<?> lore = (List<?>) config.get("lore");
            if (lore.isEmpty()) return List.of(List.of());
            if (lore.get(0) instanceof List<?>) return (List<List<String>>) lore;
            return List.of((List<String>)lore);
        }
        return List.of(List.of());
    }

    protected Map<String,List<List<String>>> getSlots() {
        Map<String,List<List<String>>> map = new HashMap<>();

        if (!config.containsKey("slot")) {
            return map;
        };

        Object opt = config.get("slot");
        if (opt instanceof String || opt instanceof Integer) {
            map.put("_DEFAULT_",List.of(List.of(opt+"")));
            return map;
        }
        Map<String,Object> slots = (Map<String, Object>) opt;
        if (slots.isEmpty()) return map;

        slots.forEach((page,slot)->{
            List<List<String>> list = new ArrayList<>();
            ((List<?>)slot).forEach(l -> {
                if (l instanceof List<?>) { // to account for direct ints in cfg
                    List<String> strs = new ArrayList<>();
                    ((List<?>) l).forEach(s->strs.add(s+""));
                    list.add(strs);
                }
                else list.add(List.of(l+""));
            });

            map.put(page,list);
        });

        return map;
    }

    protected Map<String,String> getEnchants() {
        if (config.containsKey("enchantments")) {
            Map<String,Object> enchantments = (Map<String,Object>) config.get("enchantments");
            if (enchantments == null || enchantments.isEmpty()) return Map.of();

            Map<String,String> output = new HashMap<>();
            enchantments.forEach((enchant,lvl)-> output.put(enchant,lvl+""));
            return output;
        }
        return Map.of();
    }

    protected List<String> getFlags() {
        if (config.containsKey("flags")) {
            List<String> flags = (List<String>) config.get("flags");
            if (flags == null || flags.isEmpty()) return List.of();
            return flags;
        }
        return List.of();
    }

    protected Map<String,Map<String,String>> getAttributes() {
        if (config.containsKey("attributes")) {
            Map<String,Map<String,String>> attributes = (Map<String,Map<String,String>>) config.get("attributes");
            if (attributes == null || attributes.isEmpty()) return Map.of();
            return attributes;
        }
        return Map.of();
    }

    public boolean isMovable() {
        return isMovable;
    }

    public ItemStack getSkull(Object url) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);

        ItemMeta skullMeta = head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().putAll((PropertyMap) url);

        try {
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, profile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        head.setItemMeta(skullMeta);
        return head;
    }

    public boolean isSkinMat(String mat) {
        return mat.startsWith("texture:")
                || mat.startsWith("mineskin:")
                || mat.startsWith("player:");
    }

    public net.minecraft.world.item.ItemStack getItem(int frame, TabPlayer p, Page page, int slot) {
        if (materials.isEmpty()) return air;

        Map<String,String> replacements = replacements(p,page,slot);

        List<String> lore = new ArrayList<>(lores.get(Utils.frame(frame,lores.size())));
        lore.forEach(l->lore.set(lore.indexOf(l),placeholders(l,p,replacements)));

        Map<String,String> enchants = new HashMap<>(this.enchants);
        enchants.forEach((enchant,lvl)->enchants.put(placeholders(enchant,p,replacements),placeholders(lvl,p,replacements)));

        List<String> flags = new ArrayList<>(this.flags);
        flags.forEach(flag->flags.set(flags.indexOf(flag),placeholders(flag,p,replacements)));

        Map<String,Map<String,String>> attributes = new HashMap<>(this.attributes);
        attributes.forEach((attribute,cfg)->{
            cfg.forEach((opt,value)->{
                cfg.put(placeholders(opt,p,replacements),placeholders(value,p,replacements));
            });
            attributes.put(placeholders(attribute,p,replacements),cfg);
        });

        return getItem(placeholders(materials.get(Utils.frame(frame,materials.size())),p,replacements),
                names.isEmpty() ? null : placeholders(names.get(Utils.frame(frame,names.size())),p,replacements),
                amounts.isEmpty() ? null : placeholders(amounts.get(Utils.frame(frame,amounts.size())),p,replacements),
                lore,
                enchants,
                flags,
                attributes,
                slot
        );
    }

    private final net.minecraft.world.item.ItemStack air = net.minecraft.world.item.ItemStack.b;

    public net.minecraft.world.item.ItemStack getItem(String mat, String name, String amount, List<String> lore, Map<String,String> enchants, List<String> flags, Map<String,Map<String,String>> attributes, int slot) {
        ItemStack item;
        String customModelData = "";
        String potionEffect = "";
        String color = "";

        if (isSkinMat(mat)) {
            PropertyMap skin = null;
            try {
                Skin s = ARMenu.get().getMenuManager().skins.getSkin(mat);
                skin = new PropertyMap();
                skin.put("textures",new Property("textures", s.getValue(), s.getSignature()));
            }
            catch (Exception ignored) {}
            if (skin == null)
                return air;
            item = getSkull(skin);
        } else if (mat.startsWith("item-storage:")) {
            item = ARMenu.get().getItemStorage().getItem(mat.substring(13));
            if (item == null)
                return air;
        } else if (mat.startsWith("oraxen:")) {
            if (!Bukkit.getServer().getPluginManager().isPluginEnabled("Oraxen"))
                return air;
            item = OraxenItems.getItemById(mat.substring(7)).build();
            if (item == null)
                return air;
        } else {
            String mat2 = mat.replace(" ", "_").toUpperCase();

            Matcher modelMatcher = customModelDataPattern.matcher(mat2);
            if (modelMatcher.find()) {
                customModelData = modelMatcher.group("data");
                mat2 = mat2.replace(modelMatcher.group(),"");
            }

            Matcher effectMatcher = potionEffectPattern.matcher(mat2);
            if (effectMatcher.find()) {
                potionEffect = effectMatcher.group("data");
                mat2 = mat2.replace(effectMatcher.group(),"");
            }
            Matcher colorMatcher = colorPattern.matcher(mat2);
            if (colorMatcher.find()) {
                color = colorMatcher.group("data");
                mat2 = mat2.replace(colorMatcher.group(),"");
            }

            Material m2 = Material.getMaterial(mat2);
            if (m2 == null) return air;
            item = new ItemStack(m2);
        }

        try {item.setAmount(Math.round(Float.parseFloat(amount)));}
        catch (Exception ignored) {}

        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        if (!customModelData.equals("")) {
            int modelData = Utils.parseInt(customModelData,-1);
            if (modelData != -1)
                item.getItemMeta().setCustomModelData(modelData);
        }

        if (!enchants.isEmpty()) enchants.forEach((enchant,lvl)->{
            Enchantment e = Enchantment.getByKey(NamespacedKey.minecraft(enchant.toLowerCase().replace(" ","_")));
            if (e == null) return;

            try {
                int l = Integer.parseInt(lvl);
                meta.addEnchant(e, l, true);
            } catch (Exception ignored) {}
        });

        if (!flags.isEmpty()) flags.forEach(flag->{
            try {
                ItemFlag f = ItemFlag.valueOf(flag.toUpperCase().replace(" ", "_"));
                meta.addItemFlags(f);
            } catch (Exception ignored) {}
        });

        if (!attributes.isEmpty()) attributes.forEach((attribute,cfg)->{
            try {
                Attribute att = Attribute.valueOf(attribute.toUpperCase().replace(" ", "_"));

                EquipmentSlot s = EquipmentSlot.valueOf(cfg.get("slot").toUpperCase().replace(" ","_"));
                int fAmt = Integer.parseInt(cfg.get("amount"));

                String type = cfg.get("type").toUpperCase().replace(" ","_");
                AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(type);
                if (meta.hasAttributeModifiers())
                    meta.getAttributeModifiers().clear();
                meta.addAttributeModifier(att, new AttributeModifier(UUID.randomUUID(), attribute,fAmt, operation, s));
            } catch (Exception ignored) {}
        });

        meta.getPersistentDataContainer().set(ARMenu.get().namespacedKey, PersistentDataType.STRING,name+"-"+slot);
        if (meta instanceof PotionMeta potion) {
            try {
                PotionType effect = PotionType.valueOf(potionEffect);
                potion.setBasePotionData(new PotionData(effect));
            } catch (Exception ignored) {}

            if (!color.equals("")) {
                String[] rgb = color.split(",");
                int r = Integer.parseInt(rgb[0]);
                int g = Integer.parseInt(rgb[1]);
                int b = Integer.parseInt(rgb[2]);
                potion.setColor(Color.fromBGR(r,g,b));
            }
        }

        item.setItemMeta(meta);

        net.minecraft.world.item.ItemStack nmsItem = Utils.asNMSCopy(item);
        if (!color.equals("")) {
            int color1 = Utils.parseInt(color, -1);
            if (color1 != -1)
                nmsItem.b("display").a("color",color1);
        }
        if (name != null) nmsItem.a(Utils.rgbComp(name));
        if (!lore.isEmpty()) {
            String lore1 = String.join("\n",lore);
            List<String> strs = List.of(lore1.split("\\n"));

            NBTTagList list = new NBTTagList();
            strs.forEach(s->{
                IChatBaseComponent comp = Utils.rgbComp(s);
                String json = IChatBaseComponent.ChatSerializer.a(comp);
                NBTTagString str = NBTTagString.a(json);
                list.add(str);
            });
            nmsItem.a("display").a("Lore",list);
        }
        return nmsItem;
    }

    protected String placeholders(String text, TabPlayer p, Map<String,String> replacements) {
        text = Utils.replacements(text,replacements);
        return Utils.parsePlaceholders(text,p);
    }

    protected Map<String,String> replacements(TabPlayer p, Page page, int slot) {
        Map<String,String> map = new HashMap<>();
        map.put("%slot%",slot+"");
        map.put("%page%", page.getName());
        List<String> args = ARMenu.get().getMenuManager().sessions.get(p).getArguments();
        for (String arg : args)
            map.put("%arg-"+args.indexOf(arg)+"%",arg);
        map.put("%args-amount%",args.size()+"");
        map.put("%args%",String.join(" ",args));
        return map;
    }

    public List<Map<Action,String>> getClickActions(ClickType clickType, TabPlayer p, int slot, Page page) {
        List<Map<Action,String>> list = new ArrayList<>();
        Map<String,Object> actions = (Map<String, Object>) config.get("actions");
        if (actions == null) return list;

        Map<String,String> replacements = replacements(p,page,slot);
        replacements.put("%click%",clickType+"");

        for (String type : actions.keySet()) {
            for (String type2 : type.split(",")) {
                ClickType click = ClickType.get(type2);
                if (click == null) {
                    ARMenu.get().getLogger().info("Click type "+type2+" does not exist! Skipped");
                    continue;
                }
                if (click != clickType) continue;
                page.getMenu().onEvent(p,"items."+name+".actions."+type,replacements);
            }
        }
        return list;
    }
}
