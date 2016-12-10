package net.minelink.ctplus.listener;

import com.google.common.collect.ImmutableSet;
import net.minelink.ctplus.CombatTagPlus;
import net.minelink.ctplus.Tag;
import net.minelink.ctplus.TagManager;
import net.minelink.ctplus.event.PlayerCombatTagEvent;
import net.minelink.ctplus.task.SafeLogoutTask;
import net.minelink.ctplus.task.TagUpdateTask;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Creature;
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
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;

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
        Player victim = determineVictim(victimEntity);
        if (victim == null) return;

        // Do not tag the victim if they are in creative mode
        if (victim.getGameMode() == GameMode.CREATIVE && plugin.getSettings().disableCreativeTags()) {
            victim = null;
        }

        LivingEntity attacker = determineAttacker(attackerEntity, victim);
        if (attacker == null) return;
        Player attackingPlayer = attacker instanceof Player ? (Player) attacker : null;

        // Do nothing if damage is self-inflicted
        if (Objects.equals(victim, attacker) && plugin.getSettings().disableSelfTagging()) return;

        // Do not tag the attacker if they are in creative mode
        if (attackingPlayer != null && attackingPlayer.getGameMode() == GameMode.CREATIVE && plugin.getSettings().disableCreativeTags()) {
            return;
        }

        // Combat tag victim and player
        plugin.getTagManager().tag(victim, attackingPlayer);
    }

    @Nullable
    private Player determineVictim(Entity victimEntity) {
        // Find victim
        if (victimEntity instanceof Tameable) {
            AnimalTamer owner = ((Tameable) victimEntity).getOwner();
            if (!(owner instanceof Player)) return null;

            // Victim is a player's pet
            return (Player) owner;
        } else if (victimEntity instanceof Player) {
            // Victim is a player
            return (Player) victimEntity;
        } else {
            // Victim is not a player
            return null;
        }
    }

    @Nullable
    private LivingEntity determineAttacker(Entity attackerEntity, Player victim) {
        // Find attacker
        if (attackerEntity instanceof Creature && plugin.getSettings().mobTagging()) {
            return (LivingEntity) attackerEntity;
        } else if (attackerEntity instanceof Projectile) {
            Projectile p = (Projectile) attackerEntity;
            Entity source;
            if (p.getShooter() instanceof Entity) {
                source = (Entity) p.getShooter();
            } else {
                return null;
            }

            // Ignore self inflicted ender pearl damage
            if (p.getType() == EntityType.ENDER_PEARL && Objects.equals(victim, source)) return null;

            return determineAttacker(source, victim);
        } else if (attackerEntity instanceof Tameable) {
            AnimalTamer owner = ((Tameable) attackerEntity).getOwner();
            if (owner instanceof Player) {
                // Attacker is a player's pet
                return (Player) owner;
            }
        } else if (attackerEntity instanceof Player) {
            // Attacker is a player
            return (Player) attackerEntity;
        }
        return null;
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

            // Do nothing if the affected player is the thrower
            Player victim = (Player) entity;
            if (victim == attacker) continue;

            if (!plugin.getNpcPlayerHelper().isNpc(victim)) {
                plugin.getTagManager().tag(victim, attacker);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void tagPlayer(ProjectileLaunchEvent event) {
        // Do nothing if option is disabled
        if (plugin.getSettings().resetTagOnPearl()) return;

        // Do nothing if the launched projectile is not an ender pearl
        Projectile entity = event.getEntity();
        if (entity.getType() != EntityType.ENDER_PEARL) return;

        // Do nothing if projectile source is not a player
        if (!(entity.getShooter() instanceof Player)) return;

        // Do nothign if player has permission to bypass tagging
        Player player = (Player) entity.getShooter();
        if (player.hasPermission("ctplus.bypass.tag")) return;

        // Do nothing if player is not tagged
        Tag tag = plugin.getTagManager().getTag(player.getUniqueId());
        if (tag == null) return;

        // Reset the tag duration
        TagManager.Flag flag;
        if (player.getUniqueId().equals(tag.getAttackerId())) {
            flag = TagManager.Flag.TAG_ATTACKER;
        } else if (player.getUniqueId().equals(tag.getVictimId())) {
            flag = TagManager.Flag.TAG_VICTIM;
        } else return;

        Player victim = null;
        if (tag.getVictimId() != null) {
            victim = Bukkit.getPlayer(tag.getVictimId());
        }

        Player attacker = null;
        if (tag.getAttackerId() != null) {
            attacker = Bukkit.getPlayer(tag.getAttackerId());
        }

        plugin.getTagManager().tag(victim, attacker, EnumSet.of(flag));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void untagPlayer(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Remove combat tag from player if not a NPC
        if (!plugin.getNpcPlayerHelper().isNpc(player)) {
            plugin.getTagManager().untag(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void untagPlayer(PlayerKickEvent event) {
        // Do nothing if user has not specified to kill on kick
        if (!plugin.getSettings().untagOnKick()) return;

        // Do nothing if blacklist contains this kick message
        if (plugin.getSettings().getUntagOnKickBlacklist().contains(event.getReason())) return;

        // Remove the players tag
        Player player = event.getPlayer();
        plugin.getTagManager().untag(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void sendTagMessage(PlayerCombatTagEvent event) {
        Player attacker = event.getAttacker();
        Player victim = event.getVictim();

        // Send combat tag notification to victim
        if (victim != null && !plugin.getTagManager().isTagged(victim.getUniqueId())
                && !plugin.getSettings().onlyTagAttacker()) {
            if (attacker != null) {
                String message = plugin.getSettings().getTagMessage();
                if (!message.isEmpty()) {
                    victim.sendMessage(message.replace("{opponent}", attacker.getName()));
                }
            } else {
                String message = plugin.getSettings().getTagUnknownMessage();
                if (!message.isEmpty()) {
                    victim.sendMessage(message);
                }
            }
        }

        // Send combat tag notification to attacker
        if (attacker != null && !plugin.getTagManager().isTagged(attacker.getUniqueId())) {
            if (victim != null) {
                String message = plugin.getSettings().getTagMessage();
                if (!message.isEmpty()) {
                    attacker.sendMessage(message.replace("{opponent}", victim.getName()));
                }
            } else {
                String message = plugin.getSettings().getTagUnknownMessage();
                if (!message.isEmpty()) {
                    attacker.sendMessage(message);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void updateTag(PlayerCombatTagEvent event) {
        // Update victim's combat bar
        Player victim = event.getVictim();
        if (victim != null) {
            TagUpdateTask.run(plugin, victim);
        }

        // Update attacker's combat bar
        Player attacker = event.getAttacker();
        if (attacker != null) {
            TagUpdateTask.run(plugin, attacker);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void cancelLogout(PlayerCombatTagEvent event) {
        // Cancel safe logout attempt if player was just combat tagged
        Player player = event.getPlayer();
        if (!SafeLogoutTask.cancel(player)) return;

        // Inform player
        if (!plugin.getSettings().getLogoutCancelledMessage().isEmpty()) {
            player.sendMessage(plugin.getSettings().getLogoutCancelledMessage());
        }
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
        if (!plugin.getHookManager().isPvpEnabledAt(event.getPlayer().getLocation())) {
            event.setCancelled(true);
        }
    }

}
