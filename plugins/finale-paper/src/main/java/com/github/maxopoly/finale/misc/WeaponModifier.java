package com.github.maxopoly.finale.misc;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

public class WeaponModifier {

    public static final int DAMAGE_NON_ADJUSTED = -1;
    public static final double ATTACK_SPEED_NON_ADJUSTED = -1.0D;

    private static final class WeaponConfig {

        private double damage;
        private double attackSpeed;
        private double bonusDamagePerNetheritePiece;
        private double armourDamageMultiplier;

        private WeaponConfig(double damage, double attackSpeed, double bonusDamagePerNetheritePiece, double armourDamageMultiplier) {
            this.damage = damage;
            this.attackSpeed = attackSpeed;
            this.bonusDamagePerNetheritePiece = bonusDamagePerNetheritePiece;
            this.armourDamageMultiplier = armourDamageMultiplier;
        }

        public double getAttackSpeed() {
            return attackSpeed;
        }

        public double getDamage() {
            return damage;
        }

        public double getBonusDamagePerNetheritePiece() {
            return bonusDamagePerNetheritePiece;
        }

        public double getArmourDamageMultiplier() {
            return armourDamageMultiplier;
        }
    }

    private final Map<Material, WeaponConfig> weapons;
    private final Map<String, WeaponConfig> customWeapons;

    public WeaponModifier() {
        this.weapons = new HashMap<>();
        this.customWeapons = new HashMap<>();
    }

    public void addWeapon(Material m, int damage, double attackSpeed, double bonusDamagePerNetheritePiece, double armourDamageMultiplier) {
        weapons.put(m, new WeaponConfig(damage, attackSpeed, bonusDamagePerNetheritePiece, armourDamageMultiplier));
    }


    public void addCustomWeapon(String s, int damage, double attackSpeed, double bonusDamagePerNetheritePiece, double armourDamageMultiplier) {
        customWeapons.put(s, new WeaponConfig(damage, attackSpeed, bonusDamagePerNetheritePiece, armourDamageMultiplier));
    }

    private WeaponConfig getWeaponConfig(ItemStack item) {
        if (item == null || item.getType().isEmpty()) {
            return null;
        }
        String key = CustomItem.getCustomItemKey(item);
        if (key != null && customWeapons.containsKey(key)) {
            return customWeapons.get(key);
        } else {
            return weapons.get(item.getType());
        }
    }


    public double getAttackSpeed(ItemStack m) {
        WeaponConfig config = getWeaponConfig(m);
        if (config == null) {
            return DAMAGE_NON_ADJUSTED;
        }
        return config.getAttackSpeed();
    }

    public double getDamage(ItemStack m) {
        WeaponConfig config = getWeaponConfig(m);
        if (config == null) {
            return ATTACK_SPEED_NON_ADJUSTED;
        }
        return config.getDamage();
    }

    public double getBonusDamagePerNetheritePiece(ItemStack m) {
        WeaponConfig config = getWeaponConfig(m);
        if (config == null) {
            return 0;
        }
        return config.getBonusDamagePerNetheritePiece();
    }

    public double getArmourDamageMultiplier(ItemStack m) {
        WeaponConfig config = getWeaponConfig(m);
        if (config == null) {
            return 1;
        }
        return config.getArmourDamageMultiplier();
    }
}
