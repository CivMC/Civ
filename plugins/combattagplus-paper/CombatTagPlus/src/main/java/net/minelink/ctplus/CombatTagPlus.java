package net.minelink.ctplus;

import net.minelink.ctplus.compat.api.NpcPlayerHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.bukkit.ChatColor.*;

public final class CombatTagPlus extends JavaPlugin implements Listener {

    private Map<UUID, Player> players;

    private Settings settings;

    private TagManager tagManager;

    private NpcPlayerHelper npcPlayerHelper;

    private NpcManager npcManager;

    public Settings getSettings() {
        return settings;
    }

    public TagManager getTagManager() {
        return tagManager;
    }

    public NpcPlayerHelper getNpcPlayerHelper() {
        return npcPlayerHelper;
    }

    public NpcManager getNpcManager() {
        return npcManager;
    }

    public Player getPlayer(UUID playerId) {
        return players.get(playerId);
    }

    @Override
    public void onEnable() {
        // Disable plugin if version compatibility check fails
        if (!checkVersionCompatibility()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Load settings
        saveDefaultConfig();

        settings = new Settings(this);
        if (settings.isOutdated()) {
            getLogger().warning("**WARNING**");
            getLogger().warning("Your CombatTagPlus configuration file is outdated.");
            getLogger().warning("Backup your old file and then delete it to generate a new copy.");
        }

        // Initialize plugin state
        npcManager = new NpcManager(this);
        tagManager = new TagManager(this);

        // Build player cache from currently online players
        Map<UUID, Player> playerMap = new HashMap<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            playerMap.put(player.getUniqueId(), player);
        }

        players = playerMap;

        // Register event listeners
        Bukkit.getPluginManager().registerEvents(this, this);

        if (Bukkit.getPluginManager().isPluginEnabled("PlayerHeads")) {
            Bukkit.getPluginManager().registerEvents(new PlayerHeadsListener(this), this);
        }

        // Periodic task for purging unused data
        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                getTagManager().purgeExpired();
                BarUpdateTask.purgeFinished();
            }
        }, 3600, 3600);
    }

    @Override
    public void onDisable() {
        // Clean up player references
        // This also happens for individual players when they disconnect.
        if (players != null) {
            players.clear();
            players = null;
        }
    }

    private boolean checkVersionCompatibility() {
        // Load NMS compatibility helper class
        Class<?> dummyPlayerHelperClass = ReflectionUtils.getCompatClass("NpcPlayerHelperImpl");

        // Warn about incompatibility and return false indicating failure
        if (dummyPlayerHelperClass == null) {
            getLogger().severe("**VERSION ERROR**");
            getLogger().severe("Server API version detected: " + ReflectionUtils.API_VERSION);
            getLogger().severe("This version of CombatTagPlus is not compatible with your CraftBukkit.");
            return false;
        }

        // Helper class was found
        try {
            // Attempt to create a new helper
            npcPlayerHelper = (NpcPlayerHelper) dummyPlayerHelperClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            // Fail miserably
            throw new RuntimeException(e);
        }

        // Yay, we're compatible! (hopefully)
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equals("ctplusreload")) {
            reloadConfig();
            getSettings().load();
            sender.sendMessage(GREEN + getName() + " config reloaded.");
        } else if (cmd.getName().equals("combattagplus")) {
            if (!(sender instanceof Player)) return false;

            Tag tag = getTagManager().getTag(((Player) sender).getUniqueId());
            if (tag == null || tag.isExpired()) {
                sender.sendMessage(GREEN + "You are not in combat.");
                return true;
            }

            String duration = DurationUtils.format(tag.getTagDuration());
            sender.sendMessage(RED + duration + GRAY + " remaining on your combat timer.");
        }

        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void addPlayer(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Send a Player List of all the NPCs for client visibility reasons
        getNpcPlayerHelper().createPlayerList(player);

        // Add player to cache
        players.put(player.getUniqueId(), player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void removePlayer(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Remove all NPCs from the Player List for player
        getNpcPlayerHelper().removePlayerList(player);

        // Remove player from cache
        players.remove(player.getUniqueId());
    }

    @EventHandler
    public void spawnNpc(PlayerQuitEvent event) {
        // Do nothing if player is not combat tagged and NPCs only spawn if tagged
        Player player = event.getPlayer();
        if (!getTagManager().isTagged(player.getUniqueId()) && !getSettings().alwaysSpawn()) return;

        // Kill player if configuration states so
        if (getSettings().instantlyKill()) {
            player.setHealth(0);
            return;
        }

        // Do nothing if NPC already exists
        final Npc npc = getNpcManager().spawn(player);
        if (npc == null) return;

        // Despawn NPC after npc-despawn-time has elapsed in ticks
        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                getNpcManager().despawn(npc);
            }
        }, getSettings().getNpcDespawnTicks());
    }

    @EventHandler
    public void despawnNpc(PlayerJoinEvent event) {
        // Attempt to despawn NPC
        Npc npc = getNpcManager().getSpawnedNpc(event.getPlayer().getUniqueId());
        if (npc != null) {
            getNpcManager().despawn(npc);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void despawnNpc(PlayerDeathEvent event) {
        // Do nothing if player is not a NPC
        Player player = event.getEntity();
        if (!getNpcPlayerHelper().isNpc(player)) return;

        // Fetch NPC using the actual player identity
        UUID id = getNpcPlayerHelper().getIdentity(player).getId();
        final Npc npc = getNpcManager().getSpawnedNpc(id);
        if (npc == null) return;

        // NPC died, remove player's combat tag
        getTagManager().untag(id);

        // Despawn NPC on the next tick
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                getNpcManager().despawn(npc);
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void syncOffline(PlayerDeathEvent event) {
        // Do nothing if player is not a NPC
        final Player player = event.getEntity();
        if (!getNpcPlayerHelper().isNpc(player)) return;

        // NPC died, remove player's combat tag
        getTagManager().untag(player.getUniqueId());

        // Save NPC player data on next tick
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                getNpcPlayerHelper().syncOffline(player);
            }
        });
    }

    @EventHandler
    public void syncOffline(AsyncPlayerPreLoginEvent event) {
        // Do nothing if login is already disallowed
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        // Do nothing if there is no spawned NPC for this player
        final UUID playerId = event.getUniqueId();
        if (!getNpcManager().npcExists(playerId)) return;

        // Synchronously save the NPC's player data
        Future<?> future = Bukkit.getScheduler().callSyncMethod(this, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Npc npc = getNpcManager().getSpawnedNpc(playerId);
                if (npc == null) return null;

                getNpcPlayerHelper().syncOffline(npc.getEntity());
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
        if (!players.containsKey(npc.getIdentity().getId())) {
            getNpcPlayerHelper().syncOffline(npc.getEntity());
        }
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

        // Combat tag victim and player
        getTagManager().tag(victim, attacker);
    }

    private static final Set<PotionEffectType> harmfulEffects = new HashSet<>(Arrays.asList(
        PotionEffectType.BLINDNESS,
        PotionEffectType.CONFUSION,
        PotionEffectType.HARM,
        PotionEffectType.HUNGER,
        PotionEffectType.POISON,
        PotionEffectType.SLOW,
        PotionEffectType.SLOW_DIGGING,
        PotionEffectType.WEAKNESS,
        PotionEffectType.WITHER
    ));

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
            if (!getNpcPlayerHelper().isNpc(victim)) {
                getTagManager().tag(victim, attacker);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void untagPlayer(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Remove combat tag from player if not a NPC
        if (!getNpcPlayerHelper().isNpc(player)) {
            getTagManager().untag(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void sendTagMessage(PlayerCombatTagEvent event) {
        // DO nothing if tag message is blank
        String message = getSettings().getTagMessage();
        if (message == null || message.isEmpty()) return;

        // Send combat tag notification to victim
        Player victim = event.getVictim();
        if (victim != null && !getTagManager().isTagged(victim.getUniqueId())) {
            victim.sendMessage(message);
        }

        // Send combat tag notification to attacker
        Player attacker = event.getAttacker();
        if (attacker != null && !getTagManager().isTagged(attacker.getUniqueId())) {
            attacker.sendMessage(message);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void broadcastKill(PlayerDeathEvent event) {
        // Do nothing with kill message is blank
        String message = getSettings().getKillMessage();
        if (message == null || message.isEmpty()) return;

        Player player = event.getEntity();
        UUID playerId = player.getUniqueId();

        // Player is NPC, determine actual identity
        if (getNpcPlayerHelper().isNpc(player)) {
            playerId = getNpcPlayerHelper().getIdentity(player).getId();
        }

        // Do nothing if player isn't tagged
        Tag tag = getTagManager().getTag(playerId);
        if (tag == null) return;

        String victim = tag.getVictimName();
        String attacker = tag.getAttackerName();

        // Broadcast kill message
        if (victim != null && attacker != null) {
            message = message.replace("{victim}", victim).replace("{attacker}", attacker);
            Bukkit.broadcast(message, "ctplus.notify.kill");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void showBar(PlayerCombatTagEvent event) {
        // Update victim's combat bar
        Player victim = event.getVictim();
        if (victim != null) {
            BarUpdateTask.run(this, victim);
        }

        // Update attacker's combat bar
        Player attacker = event.getAttacker();
        if (attacker != null) {
            BarUpdateTask.run(this, attacker);
        }
    }

    @EventHandler
    public void showBar(PlayerJoinEvent event) {
        BarUpdateTask.run(this, event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void disableWorld(PlayerCombatTagEvent event) {
        // Doesn't matter who, just determine who got tagged
        Player player = event.getVictim();
        if (player == null) player = event.getAttacker();

        // Don't tag if player is in a disabled world
        if (getSettings().getDisabledWorlds().contains(player.getWorld().getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void disableCommand(PlayerCommandPreprocessEvent event) {
        // Do nothing if player has bypass permission
        Player player = event.getPlayer();
        if (player.hasPermission("ctplus.bypass.command")) return;

        // Do nothing if player isn't even combat tagged
        if (!getTagManager().isTagged(player.getUniqueId())) return;

        String message = event.getMessage().toLowerCase();

        // Is player using a denied command?
        for (String command : getSettings().getDisabledCommands()) {
            String c = "/" + command.toLowerCase();
            if (!message.equals(c) && !message.startsWith(c + " ")) continue;

            // Cancel command
            event.setCancelled(true);
            player.sendMessage(AQUA + c + RED + " is disabled in combat.");
        }
    }

}
