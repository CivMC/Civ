package com.github.maxopoly.finale.listeners;

import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

public class MeteoricIronSlownessListener implements Listener {

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler
    public void on(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player) || !(event.getDamager() instanceof LivingEntity damager)) {
            return;
        }

        int pieces = 0;
        for (ItemStack armour : player.getInventory().getArmorContents()) {
            String customItem = CustomItem.getCustomItemKey(armour);
            if (customItem != null || customItem.startsWith("meteoric_iron_")) {
                pieces++;
            }
        }
        if (pieces != 4) {
            return;
        }

        if (event.getDamageSource().getDamageType() != DamageType.PLAYER_ATTACK || event.getDamageSource().isIndirect()) {
            return;
        }

        damager.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 0, false));
    }
}
