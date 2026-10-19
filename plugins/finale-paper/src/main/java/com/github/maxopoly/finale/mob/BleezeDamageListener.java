package com.github.maxopoly.finale.mob;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class BleezeDamageListener implements Listener {

    private static final String BLEEZE = "Bleeze";
    private static final String BLEEZE_MINION = "BleezeMinion";

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDamage(final EntityDamageEvent event) {
        if (!isBleeze(event.getEntity())) {
            return;
        }

        if (event.getDamageSource().getDamageType() == DamageType.DROWN) {
            event.setCancelled(true);
        }
    }

    private boolean isBleeze(final Entity entity) {
        for (final ActiveMob mob : MythicBukkit.inst().getMobManager().getActiveMobs()) {
            if (!mob.getEntity().getBukkitEntity().getUniqueId().equals(entity.getUniqueId())) {
                continue;
            }
            final String internalName = mob.getType().getInternalName();
            return BLEEZE.equals(internalName) || BLEEZE_MINION.equals(internalName);
        }
        return false;
    }
}
