package net.minelink.ctplus.listener;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import net.minelink.ctplus.CombatTagPlus;
import net.minelink.ctplus.Npc;
import net.minelink.ctplus.event.CombatLogEvent;
import net.minelink.ctplus.event.NpcDespawnEvent;
import net.minelink.ctplus.event.NpcDespawnReason;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class NpcListener implements Listener {

    private final CombatTagPlus plugin;

    public NpcListener(CombatTagPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombatLog(CombatLogEvent event) {
        Player player = event.getPlayer();

        if (plugin.getSettings().instantlyKill()) return; // Let instakill handle it

        // Spawn a new NPC
        plugin.getNpcManager().spawn(player);
    }

    @EventHandler
    public void despawnNpc(PlayerJoinEvent event) {
        // Attempt to despawn NPC
        Npc npc = plugin.getNpcManager().getSpawnedNpc(event.getPlayer().getUniqueId());
        if (npc != null) {
            plugin.getNpcManager().despawn(npc);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void despawnNpc(PlayerDeathEvent event) {
        // Do nothing if player is not a NPC
        Player player = event.getEntity();
        if (!plugin.getNpcPlayerHelper().isNpc(player)) return;

        // Fetch NPC using the actual player identity
        UUID id = plugin.getNpcPlayerHelper().getIdentity(player).getId();
        final Npc npc = plugin.getNpcManager().getSpawnedNpc(id);
        if (npc == null) return;

        // NPC died, remove player's combat tag
        plugin.getTagManager().untag(id);

        // Despawn NPC on the next tick
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getNpcManager().despawn(npc, NpcDespawnReason.DEATH);
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onNPCDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return; // Check if the attacker is a player
        if (!(event.getEntity() instanceof Player) || !plugin.getNpcPlayerHelper().isNpc((Player) event.getEntity())) return; // Check if the defender is an entity
        Player attacker = (Player) event.getDamager();
        Player npc = (Player) event.getEntity();
        UUID npcPlayerId = plugin.getNpcPlayerHelper().getIdentity(npc).getId();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void updateDespawnTime(EntityDamageByEntityEvent event) {
        // Do nothing if we are not to update NPC despawn time on hit
        if (!plugin.getSettings().resetDespawnTimeOnHit()) return;

        // Do nothing if entity damaged is not a player
        if (!(event.getEntity() instanceof Player)) return;

        // Do nothing if player damaged is not a NPC
        Player player = (Player) event.getEntity();
        if (!plugin.getNpcPlayerHelper().isNpc(player)) return;

        // Update the NPC despawn time
        UUID npcId = plugin.getNpcPlayerHelper().getIdentity(player).getId();
        Npc npc = plugin.getNpcManager().getSpawnedNpc(npcId);
        if (plugin.getNpcManager().hasDespawnTask(npc)) {
            long despawnTime = System.currentTimeMillis() + plugin.getSettings().getNpcDespawnMillis();
            plugin.getNpcManager().getDespawnTask(npc).setTime(despawnTime);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void syncOffline(PlayerDeathEvent event) {
        // Do nothing if player is not a NPC
        final Player player = event.getEntity();
        if (!plugin.getNpcPlayerHelper().isNpc(player)) return;

        // NPC died, remove player's combat tag
        plugin.getTagManager().untag(player.getUniqueId());

        // Save NPC player data on next tick
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getNpcPlayerHelper().syncOffline(player);
            }
        });
    }

    @EventHandler
    public void syncOffline(AsyncPlayerPreLoginEvent event) {
        // Do nothing if login is already disallowed
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        // Do nothing if there is no spawned NPC for this player
        final UUID playerId = event.getUniqueId();
        if (!plugin.getNpcManager().npcExists(playerId)) return;

        // Synchronously save the NPC's player data
        Future<?> future = Bukkit.getScheduler().callSyncMethod(plugin, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Npc npc = plugin.getNpcManager().getSpawnedNpc(playerId);
                if (npc == null) return null;

                plugin.getNpcPlayerHelper().syncOffline(npc.getEntity());
                return null;
            }
        });

        // Wait for the save to complete
        // TODO: There must be a better way of doing this?
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void syncOffline(NpcDespawnEvent event) {
        Npc npc = event.getNpc();

        // Save player data when the NPC despawns
        Player player = plugin.getPlayerCache().getPlayer(npc.getIdentity().getId());
        if (player == null) {
            plugin.getNpcPlayerHelper().syncOffline(npc.getEntity());
            return;
        }

        // Copy NPC player data to online player
        Player npcPlayer = npc.getEntity();
        player.setMaximumAir(npcPlayer.getMaximumAir());
        player.setRemainingAir(npcPlayer.getRemainingAir());
        player.setHealthScale(npcPlayer.getHealthScale());
        player.setMaxHealth(getRealMaxHealth(npcPlayer));
        player.setHealth(npcPlayer.getHealth());
        player.setTotalExperience(npcPlayer.getTotalExperience());
        player.setFoodLevel(npcPlayer.getFoodLevel());
        player.setExhaustion(npcPlayer.getExhaustion());
        player.setSaturation(npcPlayer.getSaturation());
        player.setFireTicks(npcPlayer.getFireTicks());
        player.getInventory().setContents(npcPlayer.getInventory().getContents());
        player.getInventory().setArmorContents(npcPlayer.getInventory().getArmorContents());
        player.addPotionEffects(npcPlayer.getActivePotionEffects());
    }

    /*
     * This is to prevent players with the health boost potion effect getting increased max-health.
     * Player.getMaxHealth() returns the player's health including temporary boosts from potions.
     * If we apply this to the new player with Player.setMaxHealth(), the previously temporary boost becomes permanent.
     * Players can abuse this glitch repeatedly to get infinite amounts of max-health.
     * To fix this, we simply remove any health boosts granted by potions.
     */
    @SuppressWarnings("deprecation")
    private static double getRealMaxHealth(Player npcPlayer) {
        double health = npcPlayer.getMaxHealth();
        for (PotionEffect p : npcPlayer.getActivePotionEffects()) {
            if (p.getType().equals(PotionEffectType.HEALTH_BOOST)) {
                health -= (p.getAmplifier() + 1) * 4;
            }
        }
        return health;
    }
}
