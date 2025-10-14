package com.untamedears.jukealert;

import com.untamedears.jukealert.commands.JACommandManager;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChainFactory;
import com.untamedears.jukealert.database.JukeAlertDAO;
import com.untamedears.jukealert.listener.LoggableActionListener;
import com.untamedears.jukealert.listener.SnitchLifeCycleListener;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.SnitchTypeManager;
import com.untamedears.jukealert.model.actions.LoggedActionFactory;
import com.untamedears.jukealert.util.JASettingsManager;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.api.ChunkMetaAPI;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.api.SingleBlockAPIView;

public class JukeAlert extends ACivMod {

    private static JukeAlert instance;

    public static JukeAlert getInstance() {
        return instance;
    }

    private JukeAlertDAO dao;
    private JAConfigManager configManager;
    private SnitchTypeManager snitchConfigManager;
    private SnitchManager snitchManager;
    private LoggedActionFactory loggedActionFactory;
    private JASettingsManager settingsManager;
    private SnitchCullManager cullManager;
    private TaskChainFactory taskChainFactory;
    private JACommandManager commandManager;

    public JAConfigManager getConfigManager() {
        return configManager;
    }

    public JASettingsManager getSettingsManager() {
        return settingsManager;
    }

    public LoggedActionFactory getLoggedActionFactory() {
        return loggedActionFactory;
    }

    public SnitchTypeManager getSnitchConfigManager() {
        return snitchConfigManager;
    }

    public JukeAlertDAO getDAO() {
        return dao;
    }

    public SnitchManager getSnitchManager() {
        return snitchManager;
    }

    public SnitchCullManager getSnitchCullManager() {
        return cullManager;
    }

    public TaskChainFactory getTaskChainFactory() {
        return this.taskChainFactory;
    }

    @Override
    public void onDisable() {
        snitchManager.shutdown();
        if (this.taskChainFactory != null) {
            this.taskChainFactory.shutdown(10, TimeUnit.SECONDS);
            this.taskChainFactory = null;
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        super.onEnable();
        snitchConfigManager = new SnitchTypeManager();
        cullManager = new SnitchCullManager();
        configManager = new JAConfigManager(this, snitchConfigManager);
        saveDefaultConfig();
        dao = new JukeAlertDAO(configManager.getDatabase(getConfig()));
        if (!dao.updateDatabase()) {
            getLogger().severe("Errors setting up database, shutting down");
            Bukkit.shutdown();
            return;
        }
        loggedActionFactory = new LoggedActionFactory();
        SingleBlockAPIView<Snitch> api = ChunkMetaAPI.registerSingleTrackingPlugin(this, dao);
        if (api == null) {
            getLogger().severe("Errors setting up chunk metadata API, shutting down");
            Bukkit.shutdown();
            return;
        }
        if (!configManager.parse()) {
            Bukkit.shutdown();
            return;
        }

        snitchManager = new SnitchManager(api);
        snitchManager.enable();

        settingsManager = new JASettingsManager();
        commandManager = new JACommandManager(this);
        registerJukeAlertEvents();
        JukeAlertPermissionHandler.setup();
        this.taskChainFactory = BukkitTaskChainFactory.create(this);
    }

    private void registerJukeAlertEvents() {
        PluginManager pm = getServer().getPluginManager();
        LoggableActionListener loggableActionListener = new LoggableActionListener(snitchManager);
        pm.registerEvents(loggableActionListener, this);
        loggableActionListener.setupScheduler(this);
        pm.registerEvents(new SnitchLifeCycleListener(snitchManager, snitchConfigManager, getLogger()), this);
    }
}
