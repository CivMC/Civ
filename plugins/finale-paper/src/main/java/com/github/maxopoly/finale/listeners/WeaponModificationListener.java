package com.github.maxopoly.finale.listeners;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.misc.ArmourModifier;
import com.github.maxopoly.finale.misc.ItemUtil;
import com.github.maxopoly.finale.misc.TippedArrowModifier;
import com.github.maxopoly.finale.misc.WeaponModifier;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

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

            im.removeAttributeModifier(Attribute.KNOCKBACK_RESISTANCE);
            EquipmentSlotGroup group = is.getType().getEquipmentSlot().getGroup();
            if (knockbackResistance > 0) {
                im.addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE,
                    new org.bukkit.attribute.AttributeModifier(new NamespacedKey(Finale.getPlugin(), "knockback_resistance" + group),
                        knockbackResistance,
                        org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
                        group)
                );
            }

            im.removeAttributeModifier(Attribute.ARMOR_TOUGHNESS);
            im.addAttributeModifier(Attribute.ARMOR_TOUGHNESS,
                new org.bukkit.attribute.AttributeModifier(new NamespacedKey(Finale.getPlugin(), "armor_toughness_" + group),
                    toughness,
                    org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
                    group)
            );

            im.removeAttributeModifier(Attribute.ARMOR);
            im.addAttributeModifier(Attribute.ARMOR,
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
            im.removeAttributeModifier(Attribute.ATTACK_SPEED);
            im.addAttributeModifier(Attribute.ATTACK_SPEED,
                new org.bukkit.attribute.AttributeModifier(KEY_ATTACK_SPEED,
                    adjustedAttackSpeed,
                    org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.MAINHAND)
            );

            im.removeAttributeModifier(Attribute.ATTACK_DAMAGE);
            im.addAttributeModifier(Attribute.ATTACK_DAMAGE,
                new org.bukkit.attribute.AttributeModifier(KEY_ATTACK_DAMAGE,
                    adjustedDamage,
                    org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.MAINHAND)
            );
        }

        is.setItemMeta(im);

        if (is.getType() == Material.TIPPED_ARROW) {
            ItemMeta itemMeta = is.getItemMeta();
            PotionMeta potionMeta = (PotionMeta) itemMeta;
            potionMeta = potionMeta.clone();
            PotionType potionType = potionMeta.getBasePotionType();
            if (potionType == null) {
                return;
            }

            List<PotionEffect> effects = potionType.getPotionEffects();

            TippedArrowModifier tippedArrowModifier = Finale.getPlugin().getManager().getTippedArrowModifier();
            TippedArrowModifier.TippedArrowConfig tippedArrowConfig = tippedArrowModifier.getTippedArrowConfig(potionType);
            if (tippedArrowConfig == null) {
                return;
            }

            int duration = tippedArrowConfig.getDuration();
            potionMeta.setBasePotionType(null);
            potionMeta.clearCustomEffects();
            potionMeta.setColor(tippedArrowConfig.getColor());

            for (PotionEffect effect : effects) {
                potionMeta.addCustomEffect(effect.withDuration(duration * 8), true);
            }

            potionMeta.itemName(Component.text(tippedArrowConfig.getName()));

            is.setItemMeta(potionMeta);
        }
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
