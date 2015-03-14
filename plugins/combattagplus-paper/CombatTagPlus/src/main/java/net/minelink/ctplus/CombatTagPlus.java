package net.minelink.ctplus;

import net.minelink.ctplus.compat.api.NpcNameGeneratorFactory;
import net.minelink.ctplus.compat.api.NpcPlayerHelper;
import net.minelink.ctplus.factions.api.FactionsHelper;
import net.minelink.ctplus.factions.api.FactionsHelperImpl;
import net.minelink.ctplus.listener.ForceFieldListener;
import net.minelink.ctplus.listener.NpcListener;
import net.minelink.ctplus.listener.PlayerHeadsListener;
import net.minelink.ctplus.listener.PlayerListener;
import net.minelink.ctplus.listener.TagListener;
import net.minelink.ctplus.task.BarUpdateTask;
import net.minelink.ctplus.task.SafeLogoutTask;
import net.minelink.ctplus.util.DurationUtils;
import net.minelink.ctplus.util.ReflectionUtils;
import net.minelink.ctplus.worldguard.api.WorldGuardHelper;
import net.minelink.ctplus.worldguard.api.WorldGuardHelperImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.ChatColor.*;

public final class CombatTagPlus extends JavaPlugin {

    private Map<UUID, Player> players;

    private Settings settings;

    private TagManager tagManager;

    private NpcPlayerHelper npcPlayerHelper;

    private NpcManager npcManager;

    private FactionsManager factionsManager;

    private WorldGuardManager worldGuardManager;

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

    public FactionsManager getFactionsManager() {
        return factionsManager;
    }

    public WorldGuardManager getWorldGuardManager() {
        return worldGuardManager;
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

        NpcNameGeneratorFactory.setNameGenerator(new NpcNameGeneratorImpl(this));

        integrateFactions();
        integrateWorldGuard();

        // Build player cache from currently online players
        Map<UUID, Player> playerMap = new HashMap<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            playerMap.put(player.getUniqueId(), player);
        }

        players = playerMap;

        // Register event listeners
        Bukkit.getPluginManager().registerEvents(new ForceFieldListener(this), this);
        Bukkit.getPluginManager().registerEvents(new NpcListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerHeadsListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new TagListener(this), this);

        if (Bukkit.getPluginManager().isPluginEnabled("PlayerHeads")) {
            Bukkit.getPluginManager().registerEvents(new PlayerHeadsListener(this), this);
        }

        // Periodic task for purging unused data
        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                getTagManager().purgeExpired();
                BarUpdateTask.purgeFinished();
                SafeLogoutTask.purgeFinished();
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

    private void integrateFactions() {
        // Use a dummy implementation if Factions is disabled
        if (!getSettings().useFactions()) {
            factionsManager = new FactionsManager(this, new FactionsHelperImpl());
            return;
        }

        // Determine if Factions is loaded
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Factions");
        if (plugin == null) {
            getLogger().info("Factions integration is disabled because it is not loaded.");

            // Use the dummy helper implementation if Factions isn't loaded
            factionsManager = new FactionsManager(this, new FactionsHelperImpl());
            return;
        }

        FactionsHelper helper = null;
        String[] v = plugin.getDescription().getVersion().split("\\.");
        String version = v[0] + "_" + v[1];

        // Determine which helper class implementation to use
        String className = "net.minelink.ctplus.factions.v" + version + ".FactionsHelperImpl";

        try {
            // Try to create a new helper instance
            helper = (FactionsHelper) Class.forName(className).newInstance();

            // Create the manager which is what the plugin will interact with
            factionsManager = new FactionsManager(this, helper);
        } catch (Exception e) {
            // Something went wrong, chances are it's a newer, incompatible WorldGuard
            getLogger().warning("**WARNING**");
            getLogger().warning("Failed to enable Factions integration due to errors.");
            getLogger().warning("This is most likely due to a newer Factions.");

            // Use the dummy helper implementation since WG isn't supported
            factionsManager = new FactionsManager(this, new FactionsHelperImpl());

            // Let's leave a stack trace in console for reporting
            e.printStackTrace();
        }
    }

    private void integrateWorldGuard() {
        // Use a dummy implementation if WG is disabled
        if (!getSettings().useWorldGuard()) {
            worldGuardManager = new WorldGuardManager(this, new WorldGuardHelperImpl());
            return;
        }

        // Determine if WorldGuard is loaded
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (plugin == null) {
            getLogger().info("WorldGuard integration is disabled because it is not loaded.");

            // Use the dummy helper implementation if WG isn't loaded
            worldGuardManager = new WorldGuardManager(this, new WorldGuardHelperImpl());
            return;
        }

        WorldGuardHelper helper = null;
        String v = plugin.getDescription().getVersion();

        // Determine which helper class implementation to use
        String className = "net.minelink.ctplus.worldguard.v" + (v.startsWith("5") ? 5 : 6) + ".WorldGuardHelperImpl";

        try {
            // Try to create a new helper instance
            helper = (WorldGuardHelper) Class.forName(className).newInstance();

            // Create the manager which is what the plugin will interact with
            worldGuardManager = new WorldGuardManager(this, helper);
        } catch (Exception e) {
            // Something went wrong, chances are it's a newer, incompatible WorldGuard
            getLogger().warning("**WARNING**");
            getLogger().warning("Failed to enable WorldGuard integration due to errors.");
            getLogger().warning("This is most likely due to a newer WorldGuard.");

            // Use the dummy helper implementation since WG isn't supported
            worldGuardManager = new WorldGuardManager(this, new WorldGuardHelperImpl());

            // Let's leave a stack trace in console for reporting
            e.printStackTrace();
        }
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
        } else if (cmd.getName().equals("ctpluslogout")) {
            if (!(sender instanceof Player)) return false;

            // Do nothing if player is already logging out
            Player player = (Player) sender;
            if (SafeLogoutTask.hasTask(player)) return false;

            // Attempt to start a new logout task
            SafeLogoutTask.run(this, player);
        }

        return true;
    }

    public boolean addPlayer(Player player) {
        return players.put(player.getUniqueId(), player) != null;
    }

    public boolean removePlayer(Player player) {
        return players.remove(player.getUniqueId()) != null;
    }

    public Player getPlayer(UUID playerId) {
        return players.get(playerId);
    }

    public boolean isPvpEnabledAt(Location location) {
        return getFactionsManager().isPvpEnabledAt(location) && getWorldGuardManager().isPvpEnabledAt(location);
    }

}
