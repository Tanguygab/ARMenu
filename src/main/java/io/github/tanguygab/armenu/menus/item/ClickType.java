package io.github.tanguygab.armenu.menus.item;

import net.minecraft.world.inventory.InventoryClickType;

import java.util.List;

public enum ClickType {

    LEFT_CLICK(InventoryClickType.a,0,-1,"left","left_click"),
    RIGHT_CLICK(InventoryClickType.a,1,-1,"right","right_click"),
    LEFt_CLICK_OUT(InventoryClickType.a,2,-999,"left_out","right_click_out"),
    RIGHT_CLICK_OUT(InventoryClickType.a,3,-999,"right_out","right_click_out"),

    SHIFT_lEFT_CLICK(InventoryClickType.b,1,-1,"shift_left","shift_left_click"),
    SHIFT_RIGHT_CLICK(InventoryClickType.b,2,-1,"shift_rigt","shift_right_click"),

    NUMPAD_1(InventoryClickType.c,0,-1,"num_1","numpad_1"),
    NUMPAD_2(InventoryClickType.c,1,-1,"num_2","numpad_2"),
    NUMPAD_3(InventoryClickType.c,2,-1,"num_3","numpad_3"),
    NUMPAD_4(InventoryClickType.c,3,-1,"num_4","numpad_4"),
    NUMPAD_5(InventoryClickType.c,4,-1,"num_5","numpad_5"),
    NUMPAD_6(InventoryClickType.c,5,-1,"num_6","numpad_6"),
    NUMPAD_7(InventoryClickType.c,6,-1,"num_7","numpad_7"),
    NUMPAD_8(InventoryClickType.c,7,-1,"num_8","numpad_8"),
    NUMPAD_9(InventoryClickType.c,8,-1,"num_9","numpad_9"),
    OFFHAND(InventoryClickType.c,40,-1,"offhand","swap"),

    MIDDLE_CLICK(InventoryClickType.d,2,-1,"middle","middle_click"),

    DROP_KEY(InventoryClickType.e,0,-1,"drop","drop_key"),
    CONTROL_DROP_KEY(InventoryClickType.e,1,-1,"ctrl_drop","control_drop_key"),
    
    START_LEFT_DRAG(InventoryClickType.f,0,-999,"start_left_drag"),
    START_RIGHT_DRAG(InventoryClickType.f,4,-999,"start_right_drag"),
    ADD_LEFT_DRAG(InventoryClickType.f,1,-1,"add_left_drag"),
    ADD_RIGHT_DRAG(InventoryClickType.f,5,-1,"add_right_drag"),
    END_LEFT_DRAG(InventoryClickType.f,2,-999,"end_left_drag"),
    END_RIGHT_DRAG(InventoryClickType.f,6,-999,"end_right_drag"),

    DOUBLE_CLICK(InventoryClickType.g,0,-1,"double","double_click");

    public InventoryClickType mode;
    public int button;
    public int slot;
    public List<String> names;

    ClickType(InventoryClickType mode, int button, int slot, String... name) {
        this.mode = mode;
        this.button = button;
        this.slot = slot;
        this.names = List.of(name);
    }

    public static ClickType get(String name) {
        for (ClickType type : values()) {
            if (type.getNames().contains(name))
                return type;
        }
        return null;
    }

    public InventoryClickType getMode() {
        return mode;
    }

    public int getButton() {
        return button;
    }

    public int getSlot() {
        return slot;
    }

    public List<String> getNames() {
        return names;
    }
}
