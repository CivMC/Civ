package vg.civcraft.mc.civchat2;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civchat2.command.CivChat2CommandHandler;
import vg.civcraft.mc.civchat2.listeners.CivChat2Listener;
import vg.civcraft.mc.civchat2.utility.CivChat2Config;
import vg.civcraft.mc.civchat2.utility.CivChat2Log;
import vg.civcraft.mc.civchat2.zipper.CivChat2FileLogger;

/**
 * @author jjj5311
 *
 */
public class CivChat2 extends JavaPlugin{
	
	private static CivChat2 instance;
	private static CivChat2Log log_;
	private static boolean groupsEnabled;
	private static CivChat2Config config_;
	private static CivChat2Manager chatMan;
	private CivChat2Listener chatListener;
	private static CivChat2CommandHandler handle;
	private static CivChat2FileLogger fileLog;
	private static boolean debug = false;
	
	public void onEnable(){
		//onEnable stuff
		instance = this;
		config_ = new CivChat2Config();
		config_.setConfigOptions(getConfig());
		if(!new File(this.getDataFolder() + "config.yml").exists()){
			//config.yml does not exist save the default
			this.saveDefaultConfig();
		}
		debug = config_.getDebug();
		log_ = new CivChat2Log();
		log_.initializeLogger(instance);
		
		groupsEnabled = config_.getGroupsEnabled();
		log_.info("groupsEnabled is set to: " + groupsEnabled);
		log_.debug("Debug Enabled");
		handle = new CivChat2CommandHandler();
		handle.registerCommands();
		fileLog = new CivChat2FileLogger();
		fileLog.Init();
		chatMan = new CivChat2Manager(instance);
		chatMan.test();
		chatListener = new CivChat2Listener(chatMan);
		registerEvents();
	}
	
	public void onDisable(){
		//onDisable stuff
		fileLog.serverShutdown();
	}
	
	public static CivChat2Manager getCivChat2Manager(){
		debugmessage("Returning CivChat2Manager");
		return chatMan;
	}

	public static boolean debugEnabled() {
		return config_.getDebug();
	}
	
	public static void debugmessage(String msg){
		log_.debug(msg);
	}

	public static CivChat2Log getCivChat2Log() {
		return log_;
	}
	
	public void registerEvents(){
		getServer().getPluginManager().registerEvents(chatListener, instance);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		return handle.execute(sender, cmd, args);
	}

	public static CivChat2CommandHandler getCivChat2CommandHandler() {
		return handle;
	}

	public static void warningMessage(String errorMsg) {
		log_.warning(errorMsg);		
	}
	
	public static void infoMessage(String infoMsg){
		log_.info(infoMsg);
	}

	public static CivChat2 getInstance() {
		return instance;
	}

	public static void severeMessage(String severeMsg) {
		log_.severe(severeMsg);
	}

	public static CivChat2Config getPluginConfig() {
		return config_;
	}

	public static CivChat2FileLogger getCivChat2FileLogger() {
		return fileLog;
	}

	
}
