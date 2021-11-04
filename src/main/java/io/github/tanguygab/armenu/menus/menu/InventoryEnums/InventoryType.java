package io.github.tanguygab.armenu.menus.menu.InventoryEnums;

import net.minecraft.world.inventory.Containers;
import java.util.List;

public enum InventoryType {
    
    NORMAL_9(Containers.a,9,"9"),
    NORMAL_18(Containers.b,18, "18"),
    NORMAL_27(Containers.c,27, "27"),
    NORMAL_36(Containers.d,36, "36"),
    NORMAL_45(Containers.e,45, "45"),
    NORMAL_54(Containers.f,54,"54"),
    DISPENSER(Containers.g,9,"dispenser"),
    ANVIL(Containers.h,2, "anvil"),
    BEACON(Containers.i,1, "beacon"),
    BLAST_FURNACE(Containers.j,3, "blast_furnace", "blast"),
    BREWING_STAND(Containers.k,5, "brewing_stand","brewing"),
    CRAFTING_TABLE(Containers.l,10,"crafting_table","craft","crafting"),
    ENCHANTING_TABLE(Containers.m,2, "enchantment_table","enchant","enchantment"),
    FURNACE(Containers.n,3, "furnace"),
    GRINDSTONE(Containers.o,3, "grindstone","grind"),
    HOPPER(Containers.p,5, "hopper"),
    LECTERN(Containers.q,0, "lectern"),
    LOOM(Containers.r,4, "loom"),
    MERCHANT(Containers.s,3, "merchant", "villager", "trade"),
    SHULKER_BOX(Containers.t,27, "shulker_box", "shulker"),
    SMITHING_TABLE(Containers.u,3, "smithing_table", "smithing"),
    SMOKER(Containers.v,3, "smoker"),
    CARTOGRAPHY(Containers.w,3, "cartography_table", "cartography"),
    STONECUTTER(Containers.x,2, "stonecutter", "cutter");

    public final Containers<?> container;
    public final int size;
    public final List<String> name;

    InventoryType(Containers<?> container, int size, String... name) {
        this.name = List.of(name);
        this.size = size;
        this.container = container;
    }

    public int getSize() {
        return size;
    }

    public static InventoryType get(String name) {
        name = name.toLowerCase().replace(" ","_");
        for (InventoryType type : values()) {
            if (type.name.contains(name))
                return type;
        }
        return null;
    }
}
