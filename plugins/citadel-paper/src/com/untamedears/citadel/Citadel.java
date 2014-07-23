package com.untamedears.citadel;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.untamedears.citadel.access.AccessDelegate;
import com.untamedears.citadel.command.CommandHandler;
import com.untamedears.citadel.dao.CitadelCachingDao;
import com.untamedears.citadel.dao.CitadelDao;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.FactionMember;
import com.untamedears.citadel.entity.Moderator;
import com.untamedears.citadel.entity.PersonalGroup;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.NaturalReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;
import com.untamedears.citadel.entity.ReinforcementKey;
import com.untamedears.citadel.listener.BlockListener;
import com.untamedears.citadel.listener.ChunkListener;
import com.untamedears.citadel.listener.EntityListener;
import com.untamedears.citadel.listener.InventoryListener;
import com.untamedears.citadel.listener.PlayerListener;
import com.untamedears.citadel.listener.WorldListener;

/**
 * User: JonnyD
 * Date: 07/18/12
 * Time: 11:57 PM
 */
public class Citadel extends JavaPlugin {
    private static final Logger log = Logger.getLogger("Citadel");
    private static final CommandHandler commandHandler = new CommandHandler();
    private static final ReinforcementManager reinforcementManager = new ReinforcementManager();
    private static final GroupManager groupManager = new GroupManager();
    private static final PersonalGroupManager personalGroupManager = new PersonalGroupManager();
    private static final ConfigManager configManager = new ConfigManager();
    private static final Random randomGenerator = new Random();
    private static final AccountIdManager accountIdManager = new AccountIdManager();
    private static CitadelCachingDao dao;
    private static Citadel plugin;

    public enum VerboseMsg {
        InteractionAttempt,
        ReinDelegation,
        AdminReinBypass,
        ReinBypass,
        RedstoneOpen,
        GolemCreated,
        NullGroup,
        AdminReinLocked,
        ReinLocked,
        CropTrample,
        ReinOvergrowth,
        ReinCreated,
        ReinDmg,
        ReinDestroyed,
        Enabled,
        Disabled
    };
    public static final Map<VerboseMsg, String> VERBOSE_MESSAGES = new HashMap<VerboseMsg, String>();
    public static final Map<String, VerboseMsg> INSENSITIVE_VERBOSE_MESSAGES = new HashMap<String, VerboseMsg>();

    static {
        VERBOSE_MESSAGES.put(VerboseMsg.InteractionAttempt, "Attempted interaction with block at %s");
        VERBOSE_MESSAGES.put(VerboseMsg.ReinDelegation, "Delegated to block at %s");
        VERBOSE_MESSAGES.put(VerboseMsg.AdminReinBypass, "[Admin] %s bypassed reinforcement at %s");
        VERBOSE_MESSAGES.put(VerboseMsg.ReinBypass, "%s bypassed reinforcement at %s");
        VERBOSE_MESSAGES.put(VerboseMsg.RedstoneOpen, "Prevented redstone from opening reinforcement at %s");
        VERBOSE_MESSAGES.put(VerboseMsg.GolemCreated, "Reinforcement removed due to golem creation at %s");
        VERBOSE_MESSAGES.put(VerboseMsg.NullGroup, "Null group %s(%s)");
        VERBOSE_MESSAGES.put(VerboseMsg.AdminReinLocked, "[Admin] %s accessed locked reinforcement at %s");
        VERBOSE_MESSAGES.put(VerboseMsg.ReinLocked, "%s failed to access locked reinforcement at %s");
        VERBOSE_MESSAGES.put(VerboseMsg.CropTrample, "Prevented reinforced crop trample at %s");
        VERBOSE_MESSAGES.put(VerboseMsg.ReinOvergrowth, "Prevented growth over reinforcement at %s");
        VERBOSE_MESSAGES.put(VerboseMsg.ReinCreated, "PlRein:%s:%d@%s,%d,%d,%d");
        VERBOSE_MESSAGES.put(VerboseMsg.ReinDmg, "Reinforcement damaged at %s");
        VERBOSE_MESSAGES.put(VerboseMsg.ReinDestroyed, "Reinforcement destroyed at %s");
        VERBOSE_MESSAGES.put(VerboseMsg.Enabled, "Citadel is now enabled.");
        VERBOSE_MESSAGES.put(VerboseMsg.Disabled, "Citadel is now disabled.");

        for (VerboseMsg msg : VerboseMsg.values()) {
            INSENSITIVE_VERBOSE_MESSAGES.put(msg.name().toLowerCase(), msg);
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        return commandHandler.dispatch(sender, label, args);
    }

    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        configManager.load();
        dao = new CitadelCachingDao(this);
        dao.updateDatabase();
        setUpStorage();
        commandHandler.registerCommands();
        // Events must register after dao is available
        registerEvents();
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
            System.out.println("failed");
        }
        ConsoleCommandSender console = getServer().getConsoleSender();
        console.addAttachment(this, "citadel.admin", true);
        console.addAttachment(this, "citadel.console", true);
        Citadel.verbose(VerboseMsg.Enabled);
    }

    public void onDisable() {
        //There should be some interface that CitadelCachingDao can implement that does this automatically on disable:
        //I don't want to do this as close() or finalize() because I want to make sure the database connection is still alive.
        if(dao instanceof CitadelCachingDao) {
            dao.shutDown();
        }
        Citadel.verbose(VerboseMsg.Disabled);
    }
    
    public void setUpStorage(){
        groupManager.initialize(dao);
        accountIdManager.initialize(dao);
        personalGroupManager.initialize(dao);
        
        ReinforcementStorage reinforcementStorage = new ReinforcementStorage(dao);
        reinforcementManager.setStorage(reinforcementStorage);
    }
    
    public void registerEvents(){
        try {
            PluginManager pm = getServer().getPluginManager();
            pm.registerEvents(new BlockListener(), this);
            pm.registerEvents(new ChunkListener(this.dao), this);
            pm.registerEvents(new PlayerListener(), this);
            pm.registerEvents(new EntityListener(), this);
            pm.registerEvents(new InventoryListener(), this);
            pm.registerEvents(new WorldListener(), this);
            pm.registerEvents(accountIdManager, this);
        }
        catch(Exception e)
        {
          printStackTrace(e);
        }
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(Faction.class);
        classes.add(PlayerReinforcement.class);
        classes.add(ReinforcementKey.class);
        classes.add(FactionMember.class);
        classes.add(PersonalGroup.class);
        classes.add(Moderator.class);
        return classes;
    }

    public static boolean verboseEnabled(VerboseMsg id) {
        return configManager.getVerboseLogging()
            && configManager.isVerboseSettingEnabled(id);
    }

    public static String verboseFmt(VerboseMsg id, Object... args) {
        String fmt = VERBOSE_MESSAGES.get(id);
        if (fmt == null) {
            return "Invalid verbose setting: " + id;
        }
        return String.format(fmt, args);
    }

    public static void verbose(VerboseMsg id) {
        if (Citadel.verboseEnabled(id)) {
            String msg = VERBOSE_MESSAGES.get(id);
            if (msg == null) {
                Citadel.info("Invalid verbose setting: " + id);
            }
            Citadel.info(msg);
        }
    }

    public static void verbose(VerboseMsg id, Object... args) {
        if (Citadel.verboseEnabled(id)) {
            Citadel.info(Citadel.verboseFmt(id, args));
        }
    }

    public static void info(String message){
        log.info("[Citadel] " + message);
    }

    public static void severe(String message){
        log.severe("[Citadel] " + message);
    }
    
    public static void warning(String message){
        log.warning("[Citadel] " + message);
    }
    
    public static GroupManager getGroupManager(){
        return groupManager;
    }
    
    public static PersonalGroupManager getPersonalGroupManager(){
        return personalGroupManager;
    }
    
    public static ReinforcementManager getReinforcementManager(){
        return reinforcementManager;
    }
    
    public static ConfigManager getConfigManager(){
        return configManager;
    }

    public static AccountIdManager getAccountIdManager() {
        return accountIdManager;
    }
    
    public static Citadel getPlugin(){
        return plugin;
    }

    public static Random getRandom(){
        return randomGenerator;
    }
    
    public static void printStackTrace(Throwable t)
    {
      severe("");
      severe("Internal error!");
      severe("Include the following into your bug report:");
      severe("          ======= SNIP HERE =======");
      
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      t.printStackTrace(pw);
      for(String l: sw.toString().replace("\r", "").split("\n"))
        severe(l);
      pw.close();
      try
      {
        sw.close();
      }
      catch(IOException e)
      {
      }
      
      severe("          ======= SNIP HERE =======");
      severe("");
    }
    
    public static CitadelDao getDao() {
        return (CitadelDao)dao;
    }

    public static Server getStaticServer() {
        return plugin.getServer();
    }
}
