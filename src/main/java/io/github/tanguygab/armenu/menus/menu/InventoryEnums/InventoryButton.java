package io.github.tanguygab.armenu.menus.menu.InventoryEnums;

import java.util.List;
import java.util.Locale;

public enum InventoryButton {

    ENCHANT_1(InventoryType.ENCHANTING_TABLE,0),
    ENCHANT_2(InventoryType.ENCHANTING_TABLE,1),
    ENCHANT_3(InventoryType.ENCHANTING_TABLE,2),

    LECTERN_PREVIOUS(InventoryType.LECTERN,1),
    LECTERN_NEXT(InventoryType.LECTERN,2),
    LECTERN_TAKE_BOOK(InventoryType.LECTERN,3),

    STONECUTTER_RECIPE(InventoryType.STONECUTTER,4),

    LOOM_RECIPE(InventoryType.LOOM,4);

    private final InventoryType type;
    private final int id;
    private final List<String> names;

    InventoryButton(InventoryType type, int id, String... names) {
        this.type = type;
        this.id = id;
        this.names = List.of(names);
    }

    public int getId() {
        return id;
    }

    public static InventoryButton get(InventoryType type, int id) {
        for (InventoryButton button : values()) {
            if (button.type != type) continue;
            if (type == InventoryType.STONECUTTER || type == InventoryType.LOOM) return button;
            if (button.id == id) return button;
        }
        return null;
    }

    public static InventoryButton get(String name) {
        name = name.toLowerCase().replace(" ","_");
        for (InventoryButton button : values()) {
            if (button.names.contains(name))
                return button;
        }
        return null;
    }

    @Override
    public String toString() {
        return super.toString().replace("_"," ").toLowerCase();
    }
}
