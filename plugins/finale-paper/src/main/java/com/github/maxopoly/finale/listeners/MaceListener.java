package com.github.maxopoly.finale.listeners;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.UseCooldown;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class MaceListener implements Listener {

    private final int cooldown;
    private final double maxDamage;
    private final float windCooldown;

    public MaceListener(int cooldown, double maxDamage, float windCooldown) {
        this.cooldown = cooldown;
        this.maxDamage = maxDamage;
        this.windCooldown = windCooldown;
    }

    @EventHandler
    public void on(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damagerPlayer)) {
            return;
        }

        ItemStack item = damagerPlayer.getInventory().getItemInMainHand();
        if (item.getType() != Material.MACE) {
            return;
        }

        if (damagerPlayer.getCooldown(Material.MACE) > 0) {
            event.setCancelled(true);
            return;
        }

        event.setDamage(Math.min(event.getDamage(), maxDamage));

        damagerPlayer.setCooldown(Material.MACE, cooldown);
        item.damage(2, damagerPlayer);
    }

    @EventHandler
    public void on(PlayerLaunchProjectileEvent event) {
        if (!(event.getProjectile() instanceof WindCharge)) {
            return;
        }

        ItemStack item = event.getItemStack();
        item.setData(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(windCooldown).build());
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        // cooldowns reset on relog for some reason, so just automatically apply max cooldown
        // better would be to work out how long the cooldown should actually be
        event.getPlayer().setCooldown(Material.MACE, cooldown);
    }
}
