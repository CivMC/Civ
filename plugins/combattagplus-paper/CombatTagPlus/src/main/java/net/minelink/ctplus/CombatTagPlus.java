package net.minelink.ctplus;

import net.minelink.ctplus.compat.api.NpcNameGeneratorFactory;
import net.minelink.ctplus.compat.api.NpcPlayerHelper;
import net.minelink.ctplus.hook.Hook;
import net.minelink.ctplus.hook.HookManager;
import net.minelink.ctplus.hook.TownyHook;
import net.minelink.ctplus.listener.ForceFieldListener;
import net.minelink.ctplus.listener.NpcListener;
import net.minelink.ctplus.listener.PlayerHeadsListener;
import net.minelink.ctplus.listener.PlayerListener;
import net.minelink.ctplus.listener.TagListener;
import net.minelink.ctplus.task.ForceFieldTask;
import net.minelink.ctplus.task.SafeLogoutTask;
import net.minelink.ctplus.task.TagUpdateTask;
import net.minelink.ctplus.util.BarUtils;
import net.minelink.ctplus.util.DurationUtils;
import net.minelink.ctplus.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;

import java.io.IOException;
import java.util.UUID;

import static org.bukkit.ChatColor.*;

public final class CombatTagPlus extends JavaPlugin {

    private final PlayerCache playerCache = new PlayerCache();

    private Settings settings;

    private HookManager hookManager;

    private TagManager tagManager;

    private NpcPlayerHelper npcPlayerHelper;

    private NpcManager npcManager;

    public PlayerCache getPlayerCache() {
        return playerCache;
    }

    public Settings getSettings() {
        return settings;
    }

    public HookManager getHookManager() {
        return hookManager;
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
            settings.update();
            getLogger().info("Configuration file has been updated.");
        }

        // Initialize plugin state
        hookManager = new HookManager(this);
        npcManager = new NpcManager(this);
        tagManager = new TagManager(this);

        NpcNameGeneratorFactory.setNameGenerator(new NpcNameGeneratorImpl(this));

        integrateFactions();
        integrateTowny();
        integrateWorldGuard();

        BarUtils.init();

        // Build player cache from currently online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            getPlayerCache().addPlayer(player);
        }

        // Register event listeners
        Bukkit.getPluginManager().registerEvents(new ForceFieldListener(this), this);
        Bukkit.getPluginManager().registerEvents(new NpcListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new TagListener(this), this);

        if (Bukkit.getPluginManager().isPluginEnabled("PlayerHeads")) {
            Bukkit.getPluginManager().registerEvents(new PlayerHeadsListener(this), this);
        }

        // Anti-SafeZone task
        ForceFieldTask.run(this);

        // Periodic task for purging unused data
        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                getTagManager().purgeExpired();
                TagUpdateTask.purgeFinished();
                SafeLogoutTask.purgeFinished();
            }
        }, 3600, 3600);

        // Start metrics
        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException ignore) {}
    }

    @Override
    public void onDisable() {
        TagUpdateTask.cancelTasks(this);
    }

    private boolean checkVersionCompatibility() {
        // Load NMS compatibility helper class
        Class<?> helperClass = ReflectionUtils.getCompatClass("NpcPlayerHelperImpl");

        // Warn about incompatibility and return false indicating failure
        if (helperClass == null) {
            getLogger().severe("**VERSION ERROR**");
            getLogger().severe("Server API version detected: " + ReflectionUtils.API_VERSION);
            getLogger().severe("This version of CombatTagPlus is not compatible with your CraftBukkit.");
            return false;
        }

        // Helper class was found
        try {
            // Attempt to create a new helper
            npcPlayerHelper = (NpcPlayerHelper) helperClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            // Fail miserably
            throw new RuntimeException(e);
        }

        // Yay, we're compatible! (hopefully)
        return true;
    }

    private void integrateFactions() {
        if (!getSettings().useFactions()) {
            return;
        }

        // Determine if Factions is loaded
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Factions");
        if (plugin == null) {
            return;
        }

        String[] v = plugin.getDescription().getVersion().split("\\.");
        String version = v[0] + "_" + v[1];

        // Special case for HCF. Use FactionsUUID 1.6 hook
        if (version.compareTo("1_6") < 0) {
            version = "1_6";
        } else if (version.compareTo("2_7") > 0) {
            version = "2_7";
        }

        // Determine which hook implementation to use
        String className = "net.minelink.ctplus.factions.v" + version + ".FactionsHook";

        try {
            // Create and add FactionsHook
            getHookManager().addHook((Hook) Class.forName(className).newInstance());
        } catch (Exception e) {
            // Something went wrong, chances are it's a newer, incompatible Factions
            getLogger().warning("**WARNING**");
            getLogger().warning("Failed to enable Factions integration due to errors.");
            getLogger().warning("This is most likely due to a newer Factions.");

            // Let's leave a stack trace in console for reporting
            e.printStackTrace();
        }
    }

    private void integrateTowny() {
        if (!getSettings().useTowny()) {
            return;
        }

        // Determine if Towny is loaded
        if (Bukkit.getPluginManager().isPluginEnabled("Towny")) {
            Hook hook = new TownyHook();
            getHookManager().addHook(hook);
        }
    }

    private void integrateWorldGuard() {
        if (!getSettings().useWorldGuard()) {
            return;
        }

        // Determine if WorldGuard is loaded
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (plugin == null) {
            return;
        }

        String v = plugin.getDescription().getVersion();

        // Determine which hook implementation to use
        String className = "net.minelink.ctplus.worldguard.v" + (v.startsWith("5") ? 5 : 6) + ".WorldGuardHook";

        try {
            // Create and add WorldGuardHook
            getHookManager().addHook((Hook) Class.forName(className).newInstance());
        } catch (Exception e) {
            // Something went wrong, chances are it's a newer, incompatible WorldGuard
            getLogger().warning("**WARNING**");
            getLogger().warning("Failed to enable WorldGuard integration due to errors.");
            getLogger().warning("This is most likely due to a newer WorldGuard.");

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

            UUID uniqueId = ((Player) sender).getUniqueId();
            Tag tag = getTagManager().getTag(uniqueId);
            if (tag == null || tag.isExpired() || !getTagManager().isTagged(uniqueId)) {
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

}
