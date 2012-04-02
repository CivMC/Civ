package com.untamedears.citadel;

import com.untamedears.citadel.command.*;
import com.untamedears.citadel.dao.CitadelDao;
import com.untamedears.citadel.entity.*;
import com.untamedears.citadel.listener.BlockListener;
import com.untamedears.citadel.listener.EntityListener;
import com.untamedears.citadel.listener.PlayerListener;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

public class Citadel extends JavaPlugin {
    private static Citadel instance;
    public static Citadel getInstance() {
        return instance;
    }
    
    public CitadelDao dao;
    public Logger log;
    public int flashLength;
    private boolean verboseLogging;
    public double redstoneDistance;
    public long autoModeReset;

    public void onEnable() {
        Citadel.instance = this;

        reloadConfig();
        log = this.getLogger();

        getConfig().options().copyDefaults(true);
        flashLength = getConfig().getInt("general.flashLength");
        autoModeReset = getConfig().getInt("general.autoModeReset");
        verboseLogging = getConfig().getBoolean("general.verboseLogging");
        redstoneDistance = getConfig().getDouble("general.redstoneDistance");
        for (Object obj : getConfig().getList("materials")) {
            LinkedHashMap map = (LinkedHashMap) obj;
            ReinforcementMaterial.put(new ReinforcementMaterial(map));
        }
        for (String name : getConfig().getStringList("additionalSecurable")) {
            Material material = Material.matchMaterial(name);
            if (material == null) {
                log.warning("Invalid additionalSecurable material " + name);
            } else {
                Reinforcement.SECURABLE.add(material.getId());
            }
        }
        for (String name : getConfig().getStringList("nonReinforceable")) {
            Material material = Material.matchMaterial(name);
            if (material == null) {
                log.warning("Invalid nonReinforceable material " + name);
            } else {
                Reinforcement.NON_REINFORCEABLE.add(material.getId());
            }
        }
        saveConfig();
        
        dao = new CitadelDao(this);

        //getCommand("cthelp").setExecutor(new Help(this));
        getCommand("ctmaterials").setExecutor(new ListMaterials(this));
        getCommand("ctsecurable").setExecutor(new ListMaterials(this));
        getCommand("ctnonreinforceable").setExecutor(new ListMaterials(this));
        getCommand("ctreinforce").setExecutor(new ChangePlacementMode(this));
        getCommand("ctfortify").setExecutor(new ChangePlacementMode(this));
        getCommand("ctinfo").setExecutor(new ChangePlacementMode(this));
        getCommand("ctoff").setExecutor(new ChangePlacementMode(this));
        getCommand("ctpublic").setExecutor(new ChangePlacementMode(this));
        getCommand("ctprivate").setExecutor(new ChangePlacementMode(this));
        getCommand("ctgroup").setExecutor(new ChangePlacementMode(this));
        getCommand("ctbypass").setExecutor(new ToggleBypass(this));
        getCommand("ctlist").setExecutor(new ListMembers(this));
        getCommand("ctallow").setExecutor(new ModifyFaction(this));
        getCommand("ctdisallow").setExecutor(new ModifyFaction(this));

        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityListener(this), this);

        log.info("Citadel is now enabled. ( •_•)>⌐■-■ ( ⌐■_■)");
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(Faction.class);
        classes.add(FactionMember.class);
        classes.add(Reinforcement.class);
        classes.add(ReinforcementKey.class);
        return classes;
    }

    public void onDisable() {
        Citadel.instance = null;
        
        log.info("Citadel is now disabled. (╯ •_•)╯ 彡┻━┻");
    }
    
    public void logVerbose(String messageFormat, Object... args) {
        if (verboseLogging)
            log.info(String.format(messageFormat, args));
    }
}