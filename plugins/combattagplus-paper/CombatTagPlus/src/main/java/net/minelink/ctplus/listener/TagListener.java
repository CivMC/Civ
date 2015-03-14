package net.minelink.ctplus.listener;

import com.google.common.collect.ImmutableSet;
import net.minelink.ctplus.CombatTagPlus;
import net.minelink.ctplus.event.PlayerCombatTagEvent;
import net.minelink.ctplus.task.BarUpdateTask;
import net.minelink.ctplus.task.SafeLogoutTask;
import org.bukkit.GameMode;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Set;

import static org.bukkit.ChatColor.*;

public final class TagListener implements Listener {

    private static final Set<PotionEffectType> harmfulEffects = ImmutableSet.of(
        PotionEffectType.BLINDNESS,
        PotionEffectType.CONFUSION,
        PotionEffectType.HARM,
        PotionEffectType.HUNGER,
        PotionEffectType.POISON,
        PotionEffectType.SLOW,
        PotionEffectType.SLOW_DIGGING,
        PotionEffectType.WEAKNESS,
        PotionEffectType.WITHER
    );

    private final CombatTagPlus plugin;

    public TagListener(CombatTagPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void tagPlayer(EntityDamageByEntityEvent event) {
        Entity victimEntity = event.getEntity();
        Entity attackerEntity = event.getDamager();
        Player victim, attacker;

        // Find victim
        if (victimEntity instanceof Tameable) {
            AnimalTamer owner = ((Tameable) victimEntity).getOwner();
            if (!(owner instanceof Player)) return;

            // Victim is a player's pet
            victim = (Player) owner;
        } else if (victimEntity instanceof Player) {
            // Victim is a player
            victim = (Player) victimEntity;
        } else {
            // Victim is not a player
            return;
        }

        // Find attacker
        if (attackerEntity instanceof Projectile) {
            Projectile p = (Projectile) attackerEntity;
            ProjectileSource source = p.getShooter();
            if (!(source instanceof Player)) return;

            // Ignore self inflicted ender pearl damage
            if (p.getType() == EntityType.ENDER_PEARL && victim == source) return;

            // Attacker is a projectile
            attacker = (Player) source;
        } else if (attackerEntity instanceof Tameable) {
            AnimalTamer owner = ((Tameable) attackerEntity).getOwner();
            if (!(owner instanceof Player)) return;

            // Attacker is a player's pet
            attacker = (Player) owner;
        } else if (attackerEntity instanceof Player) {

            // Attacker is a player
            attacker = (Player) attackerEntity;
        } else {
            // Attacker is not a player
            return;
        }

        // Do nothing if either player is in creative and config disables creative tags
        if ((attacker.getGameMode() == GameMode.CREATIVE || victim.getGameMode() == GameMode.CREATIVE) &&
                plugin.getSettings().disableCreativeTags()) {
            return;
        }

        // Combat tag victim and player
        plugin.getTagManager().tag(victim, attacker);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void tagPlayer(PotionSplashEvent event) {
        // Do nothing if thrower is not a player
        ProjectileSource source = event.getEntity().getShooter();
        if (!(source instanceof Player)) return;

        Player attacker = (Player) source;
        boolean isHarmful = false;

        // Try to determine if the potion is harmful
        for (PotionEffect effect : event.getPotion().getEffects()) {
            if (harmfulEffects.contains(effect.getType())) {
                isHarmful = true;
                break;
            }
        }

        // Do nothing potion isn't harmful
        if (!isHarmful) return;

        // Tag the attacker and any affected players
        for (LivingEntity entity : event.getAffectedEntities()) {
            if (!(entity instanceof Player)) continue;

            Player victim = (Player) entity;
            if (!plugin.getNpcPlayerHelper().isNpc(victim)) {
                plugin.getTagManager().tag(victim, attacker);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void untagPlayer(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Remove combat tag from player if not a NPC
        if (!plugin.getNpcPlayerHelper().isNpc(player)) {
            plugin.getTagManager().untag(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void sendTagMessage(PlayerCombatTagEvent event) {
        // DO nothing if tag message is blank
        String message = plugin.getSettings().getTagMessage();
        if (message == null || message.isEmpty()) return;

        // Send combat tag notification to victim
        Player victim = event.getVictim();
        if (victim != null && !plugin.getTagManager().isTagged(victim.getUniqueId())) {
            victim.sendMessage(message);
        }

        // Send combat tag notification to attacker
        Player attacker = event.getAttacker();
        if (attacker != null && !plugin.getTagManager().isTagged(attacker.getUniqueId())) {
            attacker.sendMessage(message);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void showBar(PlayerCombatTagEvent event) {
        // Update victim's combat bar
        Player victim = event.getVictim();
        if (victim != null) {
            BarUpdateTask.run(plugin, victim);
        }

        // Update attacker's combat bar
        Player attacker = event.getAttacker();
        if (attacker != null) {
            BarUpdateTask.run(plugin, attacker);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void cancelLogout(PlayerCombatTagEvent event) {
        // Cancel safe logout attempt if player was just combat tagged
        Player player = event.getPlayer();
        if (!SafeLogoutTask.cancel(player)) return;

        // Inform player
        player.sendMessage(RED + "Logout cancelled due to being combat tagged.");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void disableInWorld(PlayerCombatTagEvent event) {
        // Don't tag if player is in a disabled world
        String world = event.getPlayer().getWorld().getName();
        if (plugin.getSettings().getDisabledWorlds().contains(world)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void disableInSafeRegion(PlayerCombatTagEvent event) {
        // Don't tag if player is in a protected region
        if (!plugin.isPvpEnabledAt(event.getPlayer().getLocation())) {
            event.setCancelled(true);
        }
    }

}
