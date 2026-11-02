package vg.civcraft.mc.civchat2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.civchat2.broadcaster.BungeeServerBroadcaster;
import vg.civcraft.mc.civchat2.broadcaster.BungeeServerListener;
import vg.civcraft.mc.civchat2.broadcaster.NoopServerBroadcaster;
import vg.civcraft.mc.civchat2.broadcaster.ServerBroadcaster;
import vg.civcraft.mc.civchat2.commands.CivChatCommandManager;
import vg.civcraft.mc.civchat2.database.CivChatDAO;
import vg.civcraft.mc.civchat2.listeners.CivChat2Listener;
import vg.civcraft.mc.civchat2.listeners.KillListener;
import vg.civcraft.mc.civchat2.listeners.NewPlayerListener;
import vg.civcraft.mc.civchat2.listeners.PearlDeathTracker;
import vg.civcraft.mc.civchat2.prefix.StarManager;
import vg.civcraft.mc.civchat2.utility.CivChat2Config;
import vg.civcraft.mc.civchat2.utility.CivChat2FileLogger;
import vg.civcraft.mc.civchat2.utility.CivChat2Log;
import vg.civcraft.mc.civchat2.utility.CivChat2SettingsManager;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.permission.PermissionType;

/**
 * @author jjj5311
 */
public class CivChat2 extends ACivMod {

    private static CivChat2 instance;

    private CivChat2Log log;
    private CivChat2Config config;
    private CivChat2Manager chatMan;
    private CivChat2SettingsManager settingsManager;
    private CivChat2FileLogger fileLog;
    private CivChatDAO databaseManager;
    private CivChatCommandManager commandManager;
    private ServerBroadcaster broadcaster;

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
        saveDefaultConfig();
        reloadConfig();
        config = new CivChat2Config(getConfig());
        log = new CivChat2Log();
        log.initializeLogger(instance);
        fileLog = new CivChat2FileLogger();
        databaseManager = new CivChatDAO();
        settingsManager = new CivChat2SettingsManager();

        if (config.getServerBroadcastChat()) {
            broadcaster = new BungeeServerBroadcaster();
            this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeServerListener());
        } else {
            broadcaster = new NoopServerBroadcaster();
        }

        StarManager starManager = new StarManager(getConfig().getBoolean("chat.playtimeStars"));

        chatMan = new CivChat2Manager(instance, broadcaster, starManager);
        log.debug("Debug Enabled");
        commandManager = new CivChatCommandManager(this);
        registerNameLayerPermissions();
        registerCivChatEvents();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new CivChatPlaceholders(starManager).register();
        }
    }

    @Override
    public void onDisable() {
    }

    public CivChat2Manager getCivChat2Manager() {
        return chatMan;
    }

    public ServerBroadcaster getBroadcaster() {
        return broadcaster;
    }

    public boolean debugEnabled() {
        return config.getDebug();
    }
    public CivChat2Log getCivChat2Log() {
        return log;
    }



    private void registerCivChatEvents() {
        getServer().getPluginManager().registerEvents(new CivChat2Listener(chatMan), this);
        PearlDeathTracker pearlDeathTracker = null;
        if (Bukkit.getPluginManager().isPluginEnabled("ExilePearl")) {
            pearlDeathTracker = new PearlDeathTracker();
            registerPearlEventListener(pearlDeathTracker);
        }
        getServer().getPluginManager().registerEvents(new KillListener(config, databaseManager, settingsManager, pearlDeathTracker), this);
        if (config.isJoinGlobalGroupByDefault()) {
            getServer().getPluginManager().registerEvents(new NewPlayerListener(), this);
        }
    }

    @SuppressWarnings("unchecked")
    private void registerPearlEventListener(PearlDeathTracker tracker) {
        try {
            Plugin exilePearl = Bukkit.getPluginManager().getPlugin("ExilePearl");
            ClassLoader loader = exilePearl.getClass().getClassLoader();
            Class<? extends Event> pearlEventClass = (Class<? extends Event>)
                Class.forName("com.devotedmc.ExilePearl.event.PlayerPearledEvent", true, loader);
            Class<?> exilePearlInterface = Class.forName("com.devotedmc.ExilePearl.ExilePearl", true, loader);
            java.lang.reflect.Method getPearlMethod = pearlEventClass.getMethod("getPearl");
            java.lang.reflect.Method getPlayerIdMethod = exilePearlInterface.getMethod("getPlayerId");
            getServer().getPluginManager().registerEvent(
                pearlEventClass,
                new Listener() {},
                EventPriority.MONITOR,
                (listener, event) -> {
                    try {
                        Object pearl = getPearlMethod.invoke(event);
                        UUID playerId = (UUID) getPlayerIdMethod.invoke(pearl);
                        tracker.addRecentlyPearled(playerId);
                    } catch (Exception ignored) {
                    }
                },
                this,
                true
            );
            getLogger().info("Registered PlayerPearledEvent listener for pearl death messages");
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            getLogger().warning("ExilePearl is enabled but required class/method not found: " + e.getMessage());
        }
    }

    public void registerNameLayerPermissions() {
        List<PlayerType> memberAndAbove = Arrays.asList(PlayerType.MEMBERS, PlayerType.MODS, PlayerType.ADMINS, PlayerType.OWNER);
        PermissionType.registerPermission("READ_CHAT", new ArrayList<>(memberAndAbove),
            "Allows receiving messages sent in the group chat");
        PermissionType.registerPermission("WRITE_CHAT", new ArrayList<>(memberAndAbove),
            "Allows sending messages to the group chat");
    }

    private void registerCompletions() {

    }

    public static CivChat2 getInstance() {
        return instance;
    }

    public CivChat2SettingsManager getCivChat2SettingsManager() {
        return settingsManager;
    }

    public CivChat2Config getPluginConfig() {
        return config;
    }

    public CivChat2FileLogger getCivChat2FileLogger() {
        return fileLog;
    }

    public CivChatDAO getDatabaseManager() {
        return this.databaseManager;
    }
}
