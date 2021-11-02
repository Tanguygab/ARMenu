package io.github.tanguygab.armenu.menus.menu;

import net.minecraft.world.inventory.Containers;
import java.util.List;

public enum InventoryType {
    
    NORMAL_9(Containers.a,"9"),
    NORMAL_18(Containers.b, "18"),
    NORMAL_27(Containers.c, "27"),
    NORMAL_36(Containers.d, "36"),
    NORMAL_45(Containers.e, "45"),
    NORMAL_54(Containers.f,"54"),
    DISPENSER(Containers.g,"dispenser"),
    ANVIL(Containers.h, "anvil"),
    BEACON(Containers.i, "beacon"),
    BLAST_FURNACE(Containers.j, "blast_furnace", "blast"),
    BREWING_STAND(Containers.k, "brewing_stand","brewing"),
    CRAFTING_TABLE(Containers.l,"crafting_table","craft","crafting"),
    ENCHANTING_TABLE(Containers.m, "enchantment_table","enchant","enchantment"),
    FURNACE(Containers.n, "furnace"),
    GRINDSTONE(Containers.o, "grindstone","grind"),
    HOPPER(Containers.p, "hopper"),
    LECTERN(Containers.q, "lectern"),
    LOOM(Containers.r, "loom"),
    MERCHANT(Containers.s, "merchant", "villager", "trade"),
    SHULKER_BOX(Containers.t, "shulker_box", "shulker"),
    SMITHING_TABLE(Containers.u, "smithing_table", "smithing"),
    SMOKER(Containers.v, "smoker"),
    CARTOGRAPHY(Containers.w, "cartography_table", "cartography"),
    STONECUTTER(Containers.x, "stonecutter", "cutter");

    public final Containers<?> container;
    public final List<String> name;

    InventoryType(Containers<?> container, String... name) {
        this.name = List.of(name);
        this.container = container;
    }

    public static InventoryType get(String name) {
        name = name.replace(" ", "_");
        for (InventoryType type : values()) {
            if (type.name.contains(name))
                return type;
        }
        return null;
    }

}
