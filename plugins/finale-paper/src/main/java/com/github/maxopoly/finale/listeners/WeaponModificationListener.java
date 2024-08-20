package com.github.maxopoly.finale.listeners;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.misc.ArmourModifier;
import com.github.maxopoly.finale.misc.ItemUtil;
import com.github.maxopoly.finale.misc.WeaponModifier;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.UUID;

public class WeaponModificationListener implements Listener {

    private final NamespacedKey KEY_KNOCKBACK_RESISTANCE;
    private final NamespacedKey KEY_ARMOR_TOUGHNESS;
    private final NamespacedKey KEY_ARMOR;
    private final NamespacedKey KEY_ATTACK_SPEED;
    private final NamespacedKey KEY_ATTACK_DAMAGE;

    {
        KEY_KNOCKBACK_RESISTANCE = new NamespacedKey(Finale.getPlugin(), "knockback_resistance");
        KEY_ARMOR_TOUGHNESS = new NamespacedKey(Finale.getPlugin(), "armor_toughness");
        KEY_ARMOR = new NamespacedKey(Finale.getPlugin(), "armor");
        KEY_ATTACK_SPEED = new NamespacedKey(Finale.getPlugin(), "attack_speed");
        KEY_ATTACK_DAMAGE = new NamespacedKey(Finale.getPlugin(), "attack_damage");
    }

    @EventHandler
    public void weaponMod(InventoryClickEvent e) {
        ItemStack is = e.getCurrentItem();
        if (is == null) {
            return;
        }

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
            im.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE,
                new org.bukkit.attribute.AttributeModifier(KEY_KNOCKBACK_RESISTANCE,
                    knockbackResistance,
                    org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
                    is.getType().getEquipmentSlot().getGroup())
            );

            im.removeAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS);
            im.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS,
                new org.bukkit.attribute.AttributeModifier(KEY_ARMOR_TOUGHNESS,
                    toughness,
                    org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
                    is.getType().getEquipmentSlot().getGroup())
            );

            im.removeAttributeModifier(Attribute.GENERIC_ARMOR);
            im.addAttributeModifier(Attribute.GENERIC_ARMOR,
                new org.bukkit.attribute.AttributeModifier(KEY_ARMOR,
                    armour,
                    org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
                    is.getType().getEquipmentSlot().getGroup())
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
        e.getCurrentItem().setItemMeta(im);
    }

}
