package com.github.maxopoly.finale.listeners;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.misc.ArmourModifier;
import com.github.maxopoly.finale.misc.ItemUtil;
import com.github.maxopoly.finale.misc.WeaponModifier;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.UUID;

public class WeaponModificationListener implements Listener {

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

            im.removeAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS);
            im.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS,
                new org.bukkit.attribute.AttributeModifier(UUID.randomUUID(),
                    "generic.knockbackResistance",
                    knockbackResistance,
                    org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
                    is.getType().getEquipmentSlot())
            );

            im.removeAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS);
            im.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS,
                new org.bukkit.attribute.AttributeModifier(UUID.randomUUID(),
                    "generic.armorToughness",
                    toughness,
                    org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
                    is.getType().getEquipmentSlot())
            );

            im.removeAttributeModifier(Attribute.GENERIC_ARMOR);
            im.addAttributeModifier(Attribute.GENERIC_ARMOR,
                new org.bukkit.attribute.AttributeModifier(UUID.randomUUID(),
                    "generic.armor",
                    armour,
                    org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
                    is.getType().getEquipmentSlot())
            );
        }

        WeaponModifier weaponMod = Finale.getPlugin().getManager().getWeaponModifer();

        double adjustedDamage = weaponMod.getDamage(is.getType());
        double adjustedAttackSpeed = weaponMod.getAttackSpeed(is.getType());

        if (adjustedAttackSpeed != -1.0 || adjustedDamage != -1) {
            im.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);
            im.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,
                new org.bukkit.attribute.AttributeModifier(UUID.randomUUID(),
                    "generic.attackSpeed",
                    adjustedAttackSpeed,
                    org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlot.HAND)
            );

            im.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);
            im.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,
                new org.bukkit.attribute.AttributeModifier(UUID.randomUUID(),
                    "generic.attackDamage",
                    adjustedDamage,
                    org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlot.HAND)
            );
        }
        e.getCurrentItem().setItemMeta(im);
    }

}
