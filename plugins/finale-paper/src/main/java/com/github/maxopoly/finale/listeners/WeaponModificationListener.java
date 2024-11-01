package com.github.maxopoly.finale.listeners;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.misc.ArmourModifier;
import com.github.maxopoly.finale.misc.ItemUtil;
import com.github.maxopoly.finale.misc.TippedArrowModifier;
import com.github.maxopoly.finale.misc.WeaponModifier;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.Map;

public class WeaponModificationListener implements Listener {

    private final NamespacedKey KEY_ATTACK_SPEED;
    private final NamespacedKey KEY_ATTACK_DAMAGE;

    {
        KEY_ATTACK_SPEED = new NamespacedKey(Finale.getPlugin(), "attack_speed");
        KEY_ATTACK_DAMAGE = new NamespacedKey(Finale.getPlugin(), "attack_damage");
    }

    @EventHandler
    public void weaponMod(InventoryClickEvent e) {
        ItemStack is = e.getCurrentItem();
        if (is == null) {
            return;
        }

        this.update(is);
    }

    public void update(ItemStack is) {
        ItemMeta im = is.getItemMeta();

        ArmourModifier armourMod = Finale.getPlugin().getManager().getArmourModifier();

        double toughness = armourMod.getToughness(is.getType());
        double armour = armourMod.getArmour(is.getType());
        double knockbackResistance = armourMod.getKnockbackResistance(is.getType());

        if (toughness != -1 || armour != -1 || knockbackResistance != -1) {
            if (toughness == -1) {
                toughness = ItemUtil.getDefaultArmourToughness(is);
            }
            if (armour == -1) {
                armour = ItemUtil.getDefaultArmour(is);
            }
            if (knockbackResistance == -1) {
                knockbackResistance = ItemUtil.getDefaultKnockbackResistance(is);
            }

            im.removeAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
            EquipmentSlotGroup group = is.getType().getEquipmentSlot().getGroup();
            if (knockbackResistance > 0) {
                im.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE,
                    new org.bukkit.attribute.AttributeModifier(new NamespacedKey(Finale.getPlugin(), "knockback_resistance" + group),
                        knockbackResistance,
                        org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
                        group)
                );
            }

            im.removeAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS);
            im.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS,
                new org.bukkit.attribute.AttributeModifier(new NamespacedKey(Finale.getPlugin(), "armor_toughness_" + group),
                    toughness,
                    org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
                    group)
            );

            im.removeAttributeModifier(Attribute.GENERIC_ARMOR);
            im.addAttributeModifier(Attribute.GENERIC_ARMOR,
                new org.bukkit.attribute.AttributeModifier(new NamespacedKey(Finale.getPlugin(), "armor_" + group),
                    armour,
                    org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
                    group)
            );
        }

        WeaponModifier weaponMod = Finale.getPlugin().getManager().getWeaponModifer();

        double adjustedDamage = weaponMod.getDamage(is.getType());
        double adjustedAttackSpeed = weaponMod.getAttackSpeed(is.getType());

        if (adjustedAttackSpeed != -1.0 || adjustedDamage != -1) {
            im.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);
            im.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,
                new org.bukkit.attribute.AttributeModifier(KEY_ATTACK_SPEED,
                    adjustedAttackSpeed,
                    org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.MAINHAND)
            );

            im.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
            im.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,
                new org.bukkit.attribute.AttributeModifier(KEY_ATTACK_DAMAGE,
                    adjustedDamage,
                    org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.MAINHAND)
            );
        }

        if (is.getType() == Material.TIPPED_ARROW) {
            ItemMeta itemMeta = is.getItemMeta();
            PotionMeta potionMeta = (PotionMeta) itemMeta;
            potionMeta = potionMeta.clone();
            PotionData basePotionData = potionMeta.getBasePotionData();
            PotionType potionType = basePotionData.getType();

            TippedArrowModifier tippedArrowModifier = Finale.getPlugin().getManager().getTippedArrowModifier();
            TippedArrowModifier.TippedArrowConfig tippedArrowConfig = tippedArrowModifier.getTippedArrowConfig(potionType);
            if (tippedArrowConfig == null) {
                return;
            }

            TippedArrowModifier.PotionCategory potionCategory;
            if (basePotionData.isExtended()) {
                potionCategory = TippedArrowModifier.PotionCategory.EXTENDED;
            } else if (basePotionData.isUpgraded()) {
                potionCategory = TippedArrowModifier.PotionCategory.AMPLIFIED;
            } else {
                potionCategory = TippedArrowModifier.PotionCategory.NORMAL;
            }

            Map<TippedArrowModifier.PotionCategory, Integer> durations = tippedArrowConfig.getDurations();
            System.out.println("durations: " + durations);
            Integer duration = durations.get(potionCategory);
            System.out.println("duration: " + duration);
            if (duration != null) {
                potionMeta.setBasePotionType(null);
                potionMeta.clearCustomEffects();
                potionMeta.setColor(tippedArrowConfig.getColor());

                PotionEffect newEffect = new PotionEffect(potionType.getEffectType(), duration, basePotionData.isUpgraded() ? 1 : 0);
                potionMeta.addCustomEffect(newEffect, true);

                if (potionType == PotionType.TURTLE_MASTER) {
                    PotionEffect resEffect = new PotionEffect(PotionEffectType.RESISTANCE, duration, basePotionData.isUpgraded() ? 1 : 0);
                    potionMeta.addCustomEffect(resEffect, true);
                }

                potionMeta.displayName(Component.text(tippedArrowConfig.getName()));

                is.setItemMeta(potionMeta);
            }
        }

        is.setItemMeta(im);
    }

}
