package net.civmc.kitpvp.kit;

public enum KitCustomItem {
    METEORIC_IRON_HELMET("meteoric_iron_helmet", 16),
    METEORIC_IRON_CHESTPLATE("meteoric_iron_chestplate", 18),
    METEORIC_IRON_LEGGINGS("meteoric_iron_leggings", 17),
    METEORIC_IRON_BOOTS("meteoric_iron_boots", 15),
    METEORIC_IRON_SWORD("meteoric_iron_sword", 16),
    METEORIC_IRON_SWORD_KNOCKBACK1("meteoric_iron_sword_knockback1", 16),
    METEORIC_IRON_SWORD_KNOCKBACK("meteoric_iron_sword_knockback", 16),
    METEORIC_IRON_AXE("meteoric_iron_axe", 12),
    METEORIC_IRON_PICKAXE("meteoric_iron_pickaxe", 12),
    BACKPACK("backpack", 100),
    ;

    private final String item;
    private final int cost;

    KitCustomItem(String item, int cost) {
        this.item = item;
        this.cost = cost;
    }

    public String getItem() {
        return item;
    }

    public int getCost() {
        return cost;
    }
}
