package com.github.maxopoly.finale.misc;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

public class ArmourModifier {

    public static class ArmourConfig {

        private double toughness;
        private double armour;
        private double knockbackResistance;
        private int extraDurabilityHits;

        public ArmourConfig(double toughness, double armour, double knockbackResistance) {
            this(toughness, armour, knockbackResistance, 0);
        }

        public ArmourConfig(double toughness, double armour, double knockbackResistance, int extraDurabilityHits) {
            this.toughness = toughness;
            this.armour = armour;
            this.knockbackResistance = knockbackResistance;
            this.extraDurabilityHits = extraDurabilityHits;
        }

        public double getToughness() {
            return toughness;
        }

        public double getArmour() {
            return armour;
        }

        public double getKnockbackResistance() {
            return knockbackResistance;
        }

        public int getExtraDurabilityHits() {
            return extraDurabilityHits;
        }

        @Override
        public String toString() {
            return "Armour [toughness=" + toughness + ", armour=" + armour + ", kb_resistance=" + knockbackResistance + "]";
        }

    }

    private ExtraDurabilityTracker extraDurabilityTracker;
    private Map<Material, ArmourConfig> armour;
    private Map<String, ArmourConfig> customArmour;

    public ArmourModifier() {
        this.extraDurabilityTracker = new ExtraDurabilityTracker(this);
        this.armour = new HashMap<>();
        this.customArmour = new HashMap<>();
    }

    public void addArmour(Material m, double toughness, double armour, double knockbackResistance, int extraDurabilityHits) {
        this.armour.put(m, new ArmourConfig(toughness, armour, knockbackResistance, extraDurabilityHits));
    }

    public void addCustomArmour(String k, double toughness, double armour, double knockbackResistance, int extraDurabilityHits) {
        this.customArmour.put(k, new ArmourConfig(toughness, armour, knockbackResistance, extraDurabilityHits));
    }

    private ArmourConfig getArmourConfig(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return null;
        }
        String key = CustomItem.getCustomItemKey(item);
        if (key != null && customArmour.containsKey(key)) {
            return customArmour.get(key);
        } else {
            return armour.get(item.getType());
        }
    }

    public double getToughness(ItemStack m) {
        ArmourConfig config = getArmourConfig(m);
        if (config == null) {
            return -1;
        }
        return config.getToughness();
    }

    public double getArmour(ItemStack m) {
        ArmourConfig config = getArmourConfig(m);
        if (config == null) {
            return -1;
        }
        return config.getArmour();
    }

    public double getKnockbackResistance(ItemStack m) {
        ArmourConfig config = getArmourConfig(m);
        if (config == null) {
            return -1;
        }
        return config.getKnockbackResistance();
    }

    public int getExtraDurabilityHits(ItemStack m) {
        ArmourConfig config = getArmourConfig(m);
        if (config == null) {
            return -1;
        }

        return config.getExtraDurabilityHits();
    }

    public ExtraDurabilityTracker getExtraDurabilityTracker() {
        return extraDurabilityTracker;
    }
}
