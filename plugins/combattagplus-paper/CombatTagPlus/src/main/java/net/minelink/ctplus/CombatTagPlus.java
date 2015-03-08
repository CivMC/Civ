package net.minelink.ctplus;

import net.minelink.ctplus.compat.api.NpcPlayerHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.bukkit.ChatColor.*;

public final class CombatTagPlus extends JavaPlugin implements Listener {

    private final Map<UUID, Player> players = new HashMap<>();

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
        if (!checkVersionCompatibility()) return;

        saveDefaultConfig();

        settings = new Settings(this);
        if (settings.isOutdated()) {
            getLogger().warning("**WARNING**");
            getLogger().warning("Your CombatTagPlus configuration file is outdated.");
            getLogger().warning("Backup your old file and then delete it to generate a new copy.");
        }

        npcManager = new NpcManager(this);
        tagManager = new TagManager(this);

        Bukkit.getPluginManager().registerEvents(this, this);

        if (Bukkit.getPluginManager().isPluginEnabled("PlayerHeads")) {
            Bukkit.getPluginManager().registerEvents(new PlayerHeadsListener(this), this);
        }

        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                getTagManager().purgeExpired();
                BarUpdateTask.purgeFinished();
            }
        }, 3600, 3600);
    }

    private boolean checkVersionCompatibility() {
        Class<?> dummyPlayerHelperClass = ReflectionUtils.getCompatClass("NpcPlayerHelperImpl");

        if (dummyPlayerHelperClass == null) {
            getLogger().severe("**VERSION ERROR**");
            getLogger().severe("Server API version detected: " + ReflectionUtils.API_VERSION);
            getLogger().severe("This version of CombatTagPlus is not compatible with your CraftBukkit.");
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }

        try {
            npcPlayerHelper = (NpcPlayerHelper) dummyPlayerHelperClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

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
        getNpcPlayerHelper().createPlayerList(player);

        players.put(player.getUniqueId(), player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void removePlayer(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        getNpcPlayerHelper().removePlayerList(player);

        players.remove(player.getUniqueId());
    }

    @EventHandler
    public void spawnNpc(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!getTagManager().isTagged(player.getUniqueId())) return;

        final Npc npc = getNpcManager().spawn(player);
        if (npc == null) return;

        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                getNpcManager().despawn(npc);
            }
        }, getSettings().getNpcDespawnTicks());
    }

    @EventHandler
    public void despawnNpc(PlayerJoinEvent event) {
        Npc npc = getNpcManager().getSpawnedNpc(event.getPlayer().getUniqueId());
        if (npc != null) {
            getNpcManager().despawn(npc);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void despawnNpc(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!getNpcPlayerHelper().isNpc(player)) return;


        UUID id = getNpcPlayerHelper().getIdentity(player).getId();
        final Npc npc = getNpcManager().getSpawnedNpc(id);
        if (npc == null) return;

        getTagManager().untag(id);

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                getNpcManager().despawn(npc);
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void syncOffline(PlayerDeathEvent event) {
        final Player player = event.getEntity();
        if (!getNpcPlayerHelper().isNpc(player)) return;

        getTagManager().untag(player.getUniqueId());

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                getNpcPlayerHelper().syncOffline(player);
            }
        });
    }

    @EventHandler
    public void syncOffline(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        final UUID playerId = event.getUniqueId();
        if (!getNpcManager().npcExists(playerId)) return;

        Future<?> future = Bukkit.getScheduler().callSyncMethod(this, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Npc npc = getNpcManager().getSpawnedNpc(playerId);
                if (npc == null) return null;

                getNpcPlayerHelper().syncOffline(npc.getEntity());
                return null;
            }
        });

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void syncOffline(NpcDespawnEvent event) {
        Npc npc = event.getNpc();

        if (!players.containsKey(npc.getIdentity().getId())) {
            getNpcPlayerHelper().syncOffline(npc.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void tagPlayer(EntityDamageByEntityEvent event) {
        Entity victimEntity = event.getEntity();
        Entity attackerEntity = event.getDamager();
        Player victim, attacker;

        if (victimEntity instanceof Tameable) {
            AnimalTamer owner = ((Tameable) victimEntity).getOwner();
            if (!(owner instanceof Player)) return;

            victim = (Player) owner;
        } else if (victimEntity instanceof Player) {
            victim = (Player) victimEntity;
        } else return;

        if (attackerEntity instanceof Projectile) {
            ProjectileSource source = ((Projectile) attackerEntity).getShooter();
            if (!(source instanceof Player)) return;

            attacker = (Player) source;
        } else if (attackerEntity instanceof Tameable) {
            AnimalTamer owner = ((Tameable) attackerEntity).getOwner();
            if (!(owner instanceof Player)) return;

            attacker = (Player) owner;
        } else if (attackerEntity instanceof Player) {
            attacker = (Player) attackerEntity;
        } else return;

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
        ProjectileSource source = event.getEntity().getShooter();
        if (!(source instanceof Player)) return;

        Player attacker = (Player) source;
        boolean isHarmful = false;

        for (PotionEffect effect : event.getPotion().getEffects()) {
            if (harmfulEffects.contains(effect.getType())) {
                isHarmful = true;
                break;
            }
        }

        if (!isHarmful) return;

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

        if (!getNpcPlayerHelper().isNpc(player)) {
            getTagManager().untag(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void sendTagMessage(PlayerCombatTagEvent event) {
        String message = getSettings().getTagMessage();
        if (message == null || message.isEmpty()) return;

        Player victim = event.getVictim();
        if (victim != null && !getTagManager().isTagged(victim.getUniqueId())) {
            victim.sendMessage(message);
        }

        Player attacker = event.getAttacker();
        if (attacker != null && !getTagManager().isTagged(attacker.getUniqueId())) {
            attacker.sendMessage(message);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void broadcastKill(PlayerDeathEvent event) {
        String message = getSettings().getKillMessage();
        if (message == null || message.isEmpty()) return;

        Player player = event.getEntity();
        UUID playerId = player.getUniqueId();
        if (getNpcPlayerHelper().isNpc(player)) {
            playerId = getNpcPlayerHelper().getIdentity(player).getId();
        }

        Tag tag = getTagManager().getTag(playerId);
        if (tag == null) return;

        String victim = tag.getVictimName();
        String attacker = tag.getAttackerName();

        if (victim != null && attacker != null) {
            message = message.replace("{victim}", victim).replace("{attacker}", attacker);
            Bukkit.broadcast(message, "ctplus.notify.kill");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void showBar(PlayerCombatTagEvent event) {
        Player victim = event.getVictim();
        if (victim != null) {
            BarUpdateTask.run(this, victim);
        }

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
        Player player = event.getVictim();
        if (player == null) player = event.getAttacker();

        if (getSettings().getDisabledWorlds().contains(player.getWorld().getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void disableCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("ctplus.bypass.command")) return;
        if (!getTagManager().isTagged(player.getUniqueId())) return;

        String message = event.getMessage().toLowerCase();

        for (String command : getSettings().getDisabledCommands()) {
            String c = "/" + command.toLowerCase();
            if (!message.equals(c) && !message.startsWith(c + " ")) continue;

            event.setCancelled(true);
            player.sendMessage(AQUA + c + RED + " is disabled in combat.");
        }
    }

}
