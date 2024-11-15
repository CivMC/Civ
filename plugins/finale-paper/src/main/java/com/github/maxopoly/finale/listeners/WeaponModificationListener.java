package com.github.maxopoly.finale.listeners;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.combat.CombatUtil;
import com.github.maxopoly.finale.misc.ArmourModifier;
import com.github.maxopoly.finale.misc.ItemUtil;
import com.github.maxopoly.finale.misc.WeaponModifier;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.UUID;

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

        ItemMeta im = is.getItemMeta();

        ArmourModifier armourMod = Finale.getPlugin().getManager().getArmourModifier();

        double toughness = armourMod.getToughness(is);
        double armour = armourMod.getArmour(is);
        double knockbackResistance = armourMod.getKnockbackResistance(is);

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

        double adjustedDamage = weaponMod.getDamage(is);
        double adjustedAttackSpeed = weaponMod.getAttackSpeed(is);

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

    @EventHandler
    public void damageEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim) || !(e.getDamager() instanceof Player attacker)) {
            return;
        }
        WeaponModifier weaponMod = Finale.getPlugin().getManager().getWeaponModifer();
        ItemStack item = attacker.getInventory().getItemInMainHand();

        int pieces = 0;
        for (ItemStack armour : victim.getInventory().getArmorContents()) {
            if (armour == null) {
                continue;
            }
            Material type = armour.getType();
            switch (type) {
                case NETHERITE_BOOTS, NETHERITE_HELMET, NETHERITE_CHESTPLATE, NETHERITE_LEGGINGS -> pieces++;
            }
        }

        e.setDamage(e.getDamage() + pieces * weaponMod.getBonusDamagePerNetheritePiece(item));
    }
}
