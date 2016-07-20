package net.minelink.ctplus;

import java.io.IOException;
import java.util.UUID;

import net.minelink.ctplus.compat.api.NpcNameGeneratorFactory;
import net.minelink.ctplus.compat.api.NpcPlayerHelper;
import net.minelink.ctplus.hook.Hook;
import net.minelink.ctplus.hook.HookManager;
import net.minelink.ctplus.hook.TownyHook;
import net.minelink.ctplus.hook.factions.FactionsPlugin;
import net.minelink.ctplus.listener.ForceFieldListener;
import net.minelink.ctplus.listener.InstakillListener;
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

import static org.bukkit.ChatColor.*;

public final class CombatTagPlus extends JavaPlugin {

    private final PlayerCache playerCache = new PlayerCache();

    private Settings settings;

    private HookManager hookManager;

    private TagManager tagManager;

    private NpcPlayerHelper npcPlayerHelper = NoOpNpcPlayerHelper.INSTANCE;

    private NpcManager npcManager;

    private FactionsPlugin factionsPlugin;

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
        NpcManager npcManager = this.npcManager;
        if (npcManager == null) {
            throw new IllegalStateException("NPCs aren't enabled!");
        } else {
            return npcManager;
        }
    }

    public FactionsPlugin getFactionsPlugin() {
        return factionsPlugin;
    }

    public boolean hasNpcs() {
        return npcManager != null;
    }

    @Override
    public void onEnable() {
        // Load settings
        saveDefaultConfig();

        settings = new Settings(this);
        if (settings.isOutdated()) {
            settings.update();
            getLogger().info("Configuration file has been updated.");
        }

        // Disable plugin if version compatibility check fails
        if (isVersionCompatible()) {
            getLogger().info("CombatTagPlus appears to support npcs for your version of CraftBukkit (" + ReflectionUtils.API_VERSION + ")!");
            // Helper class was found
            try {
                // Attempt to create a new helper
                npcPlayerHelper = (NpcPlayerHelper) PLAYER_HELPER_IMPL_CLASS.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                // Fail miserably
                throw new RuntimeException(e);
            }
        } else if (settings.areNPCsRequired()) {
            getLogger().severe("**VERSION ERROR**");
            getLogger().severe("Server API version detected: " + ReflectionUtils.API_VERSION);
            getLogger().severe("This version of CombatTagPlus doesn't support NPCs on your version of CraftBukkit!");
            getLogger().severe("The plugin is configured to use NPCs, and therefore won't work without it!");
            getLogger().severe("You may disable NPCs by setting 'instantly-kill' to true and 'always-spawn' to false");
            getLogger().severe("Disabling plugin.....");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } else {
            getLogger().warning("Server API version detected: " + ReflectionUtils.API_VERSION);
            getLogger().warning("This version of CombatTagPlus doesn't support NPCs on your version of CraftBukkit.");
            getLogger().warning("However, the plugin isn't configured to use NPCs, and therefore will work without it!");
        }

        // Initialize plugin state
        hookManager = new HookManager(this);
        npcManager = isVersionCompatible() ? new NpcManager(this) : null;
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
        if (hasNpcs()) {
            Bukkit.getPluginManager().registerEvents(new NpcListener(this), this);
        }
        Bukkit.getPluginManager().registerEvents(new InstakillListener(this), this);
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

    // Load NMS compatibility helper class
    private static final Class<?> PLAYER_HELPER_IMPL_CLASS = ReflectionUtils.getCompatClass("NpcPlayerHelperImpl");

    private boolean isVersionCompatible() {
        return PLAYER_HELPER_IMPL_CLASS != null;
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
            Hook hook = (Hook) Class.forName(className).newInstance();
            if (hook instanceof FactionsPlugin) {
                getLogger().info("Advanced factions integration available.");
                getLogger().info("CombatTagPlus should respect factions relationships for NPCs now!");
                factionsPlugin = (FactionsPlugin) hook;
            } else {
                getLogger().info("Advanced factions integration isn't available for your version of factions.");
            }
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
            if (!hasNpcs() && getSettings().areNPCsRequired()) {
                if (sender instanceof Player) {
                    sender.sendMessage(RED + "You reconfigured the plugin to use NPCs, but NPCS aren't supported on CraftBukkit " + ReflectionUtils.API_VERSION);
                    sender.sendMessage(RED + "Therefore the plugin won't work on this version of CraftBukkit and needs to be disabled!");
                    sender.sendMessage(RED + "You may disable NPCs by setting 'instantly-kill' to true and 'always-spawn' to false");
                }
                getLogger().severe(sender.getName() + " reconfigured the plugin to use NPCs!");
                getLogger().severe("However, NPCs aren't supprted on CraftBukkit " + ReflectionUtils.API_VERSION);
                getLogger().severe("Therefore the plugin won't work on this version of CraftBukkit and needs to be disabled!");
                getLogger().severe("You may disable NPCs by setting 'instantly-kill' to true and 'always-spawn' to false");
                getLogger().severe("Disabling plugin....");
                getServer().getPluginManager().disablePlugin(this);
                return true;
            } else {
                if (sender instanceof Player) {
                    sender.sendMessage(GREEN + getName() + " config reloaded.");
                }
                getLogger().info("Config reloaded by " + sender.getName());
            }
        } else if (cmd.getName().equals("combattagplus")) {
            if (!(sender instanceof Player)) return false;

            UUID uniqueId = ((Player) sender).getUniqueId();
            Tag tag = getTagManager().getTag(uniqueId);
            if (tag == null || tag.isExpired() || !getTagManager().isTagged(uniqueId)) {
                sender.sendMessage(getSettings().getCommandUntagMessage());
                return true;
            }

            String duration = DurationUtils.format(tag.getTagDuration());
            sender.sendMessage(getSettings().getCommandTagMessage().replace("{time}", duration));
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
