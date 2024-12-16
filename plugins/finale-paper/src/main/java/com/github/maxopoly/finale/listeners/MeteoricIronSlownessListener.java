package com.github.maxopoly.finale.listeners;

import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

public class MeteoricIronSlownessListener implements Listener {

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler
    public void onSword(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player) || !(event.getDamager() instanceof Player damager)) {
            return;
        }

        ItemStack hand = damager.getInventory().getItemInMainHand();
        String customItem = CustomItem.getCustomItemKey(hand);
        if (!"meteoric_iron_sword".equals(customItem)) {
            return;
        }
        if (event.getDamageSource().getDamageType() != DamageType.PLAYER_ATTACK || event.getDamageSource().isIndirect()) {
            return;
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 0, false));
    }


    @SuppressWarnings("UnstableApiUsage")
    @EventHandler
    public void onArmour(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        int pieces = 0;
        for (ItemStack armour : player.getInventory().getArmorContents()) {
            String customItem = CustomItem.getCustomItemKey(armour);
            if (customItem != null && customItem.startsWith("meteoric_iron_")) {
                pieces++;
            }
        }
        if (pieces != 4) {
            return;
        }

        if (event.getDamageSource().getDamageType() != DamageType.FALL) {
            return;
        }

        event.setDamage(0);
    }
}
