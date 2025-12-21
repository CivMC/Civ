package com.github.maxopoly.finale.listeners;

import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public class RebalanceTntMinecartListener implements Listener {
    private float radius;

    @EventHandler
    public void on(EntityCombustByEntityEvent event) {
        if (!(event.getEntity() instanceof ExplosiveMinecart minecart)) {
            return;
        }
        if (!(event.getCombuster() instanceof Arrow arrow) || arrow.getFireTicks() == 0) {
            return;
        }

        double rescaledLength = arrow.getVelocity().length() / 3;
        if (arrow.isShotFromCrossbow()) {
            rescaledLength *= (3 / 3.1);
        }
        ExplosionPrimeEvent newEvent = new ExplosionPrimeEvent(
            minecart,
            (float) ((minecart.getYield() * (3.3 / 4) + minecart.getExplosionSpeedFactor() * ThreadLocalRandom.current().nextDouble() * 0.6 * rescaledLength)),
            minecart.isIncendiary()
        );
        if (!newEvent.callEvent()) {
            minecart.setFuseTicks(-1);
            return;
        }
        this.radius = newEvent.getRadius();
        try {
            minecart.getLocation().getWorld().createExplosion(minecart, newEvent.getRadius(), newEvent.getFire(), true);
            minecart.remove();
        } finally {
            this.radius = 0;
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onTnt(EntityDamageEvent event) {
        if (!(event.getDamageSource().getDirectEntity() instanceof ExplosiveMinecart minecart)) {
            return;
        }
        if (radius == 0) {
            return;
        }
        float f = radius * 2.0F;
        double unscaledDamage = switch (event.getEntity().getWorld().getDifficulty()) {
            case PEACEFUL -> throw new IllegalStateException();
            case EASY -> event.getDamage() < 2 ? event.getDamage() : (event.getDamage() - 1) / 0.5;
            case NORMAL -> event.getDamage();
            case HARD -> event.getDamage() / 1.5;
        };
        // exposure is the one unknown variable we have, so figure it out by reversing the formula lmao

        // From ExplosionDamageCalculator.getEntityDamageAmount
        double distance = minecart.getLocation().distance(event.getEntity().getLocation());

        double exposure = (0.5 * (Math.sqrt(4 * 2 * ((unscaledDamage - 1) / 7 / f) + 1) - 1)) / (1 - distance / f);

        // Round up to next 0.5
        distance = Math.round(distance * 2) / 2.0;

        double d = distance / f;
        double impact = (1.0 - d) * exposure;
        double damage = ((impact * impact + impact) / 2.0 * 7.0 * f + 1.0);
        damage = switch (event.getEntity().getWorld().getDifficulty()) {
            case PEACEFUL -> throw new IllegalStateException();
            case EASY -> Math.min(damage / 2 + 1, damage);
            case NORMAL -> damage;
            case HARD -> damage * 1.5;
        };
        event.setDamage(damage);
    }

    @EventHandler
    public void on(ExplosionPrimeEvent event) {
        if (event.getEntity().isDead()) {
            event.setCancelled(true);
        }
    }
}
