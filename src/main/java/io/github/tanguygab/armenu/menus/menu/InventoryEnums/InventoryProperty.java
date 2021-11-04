package io.github.tanguygab.armenu.menus.menu.InventoryEnums;

import java.util.List;

public enum InventoryProperty {

    FIRE_ICON(List.of(InventoryType.FURNACE,InventoryType.BLAST_FURNACE,InventoryType.SMOKER),0,"fire_icon","fire"),
    MAXIMUM_FUEL_BURN_TIME(List.of(InventoryType.FURNACE,InventoryType.BLAST_FURNACE,InventoryType.SMOKER),1,"max_fuel_burn_time","burn_time"),
    PROGRESS(List.of(InventoryType.FURNACE,InventoryType.BLAST_FURNACE,InventoryType.SMOKER),2,"progress"),
    MAXIMUM_PROGRESS(List.of(InventoryType.FURNACE,InventoryType.BLAST_FURNACE,InventoryType.SMOKER),3,"max_progress"),

    ENCHANT_LVL_REQUIREMENT_1(InventoryType.ENCHANTING_TABLE,0,"enchant_lvl_requirement_1","enchant_lvl_required_1"),
    ENCHANT_LVL_REQUIREMENT_2(InventoryType.ENCHANTING_TABLE,1,"enchant_lvl_requirement_2","enchant_lvl_required_2"),
    ENCHANT_LVL_REQUIREMENT_3(InventoryType.ENCHANTING_TABLE,2,"enchant_lvl_requirement_3","enchant_lvl_required_3"),
    ENCHANT_SEED(InventoryType.ENCHANTING_TABLE,3,"enchant_seed"),
    ENCHANT_ID_1(InventoryType.ENCHANTING_TABLE,4,"enchant_id_1","enchant_1"),
    ENCHANT_ID_2(InventoryType.ENCHANTING_TABLE,5,"enchant_id_2","enchant_2"),
    ENCHANT_ID_3(InventoryType.ENCHANTING_TABLE,6,"enchant_id_3","enchant_3"),
    ENCHANT_LEVEL_1(InventoryType.ENCHANTING_TABLE,7,"enchant_lvl_1"),
    ENCHANT_LEVEL_2(InventoryType.ENCHANTING_TABLE,8,"enchant_lvl_2"),
    ENCHANT_LEVEL_3(InventoryType.ENCHANTING_TABLE,9,"enchant_lvl_3"),

    POWER_LEVEL(InventoryType.BEACON,0,"beacon_power_level","beacon_power","beacon_level"),
    POTION_EFFECT_1(InventoryType.BEACON,1,"potion_effect_1","effect_1"),
    POTION_EFFECT_2(InventoryType.BEACON,2,"potion_effect_2","effect_2"),

    REPAIR_COST(InventoryType.ANVIL,0,"repair_cost"),

    BREW_TIME(InventoryType.BREWING_STAND,0,"brew_time"),
    FUEL_TIME(InventoryType.BREWING_STAND,1,"brewing_fuel_time"),

    SELECTED_RECIPE(InventoryType.STONECUTTER,0,"selected_recipe","recipe"),

    SELECTED_PATTERN(InventoryType.LOOM,0,"selected_pattern","pattern"),

    PAGE_NUMBER(InventoryType.LECTERN,0,"page_number","page");

    private final List<InventoryType> types;
    private final int property;
    private final List<String> names;

    InventoryProperty(List<InventoryType> types, int property, String... names) {
        this.types = types;
        this.property = property;
        this.names = List.of(names);
    }

    InventoryProperty(InventoryType type, int property, String... names) {
        types = List.of(type);
        this.property = property;
        this.names = List.of(names);
    }

    public int getProperty() {
        return property;
    }

    public List<InventoryType> getTypes() {
        return types;
    }

    public static InventoryProperty get(InventoryType type, int property) {
        for (InventoryProperty prop : values()) {
            if (!prop.types.contains(type)) continue;
            if (prop.property == property) return prop;
        }
        return null;
    }

    public static InventoryProperty get(String name) {
        name = name.toLowerCase().replace(" ","_");
        for (InventoryProperty prop : values()) {
            if (prop.names.contains(name))
                return prop;
        }
        return null;
    }
}
