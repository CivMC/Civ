package vg.civcraft.mc.civchat2;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civchat2.command.CivChat2CommandHandler;
import vg.civcraft.mc.civchat2.listeners.CivChat2Listener;
import vg.civcraft.mc.civchat2.listeners.MercuryMessageListener;
import vg.civcraft.mc.civchat2.utility.CivChat2Config;
import vg.civcraft.mc.civchat2.utility.CivChat2Executor;
import vg.civcraft.mc.civchat2.utility.CivChat2Log;
import vg.civcraft.mc.civchat2.zipper.CivChat2FileLogger;
import vg.civcraft.mc.civchat2.CivChat2Manager;

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
	private CivChat2FileLogger fileLog;
	private boolean isMercuryEnabled = false;
	private static CivChat2Executor executor;
	
	public void onEnable(){
		//onEnable stuff
		StringBuilder sb = new StringBuilder();
		instance = this;
		config_ = new CivChat2Config();
		config_.setConfigOptions(getConfig());
		if(!new File(sb.append(this.getDataFolder()).append("config.yml").toString()).exists()){
			//config.yml does not exist save the default
			this.saveDefaultConfig();
		}
		sb.delete(0, sb.length());
		config_.getDebug();
		log_ = new CivChat2Log();
		log_.initializeLogger(instance);
		fileLog = new CivChat2FileLogger();
		chatMan = new CivChat2Manager(instance);
		fileLog.Init();
		groupsEnabled = config_.getGroupsEnabled();
		log_.info(sb.append("groupsEnabled is set to: ")
					.append(groupsEnabled)
					.toString());
		sb.delete(0, sb.length());
		log_.debug("Debug Enabled");
		handle = new CivChat2CommandHandler();
		handle.registerCommands();
		
		chatListener = new CivChat2Listener(chatMan);
		executor = new CivChat2Executor(instance);
		registerEvents();
		
		getCommand("say").setExecutor(instance.executor);
	}
	
	public void onDisable(){
		//onDisable stuff
		fileLog.serverShutdown();
	}
	
	public CivChat2Manager getCivChat2Manager(){
		return CivChat2.chatMan;
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
		setMercuryEnabled(getServer().getPluginManager().isPluginEnabled("Mercury"));
		if (isMercuryEnabled()){
			this.getServer().getPluginManager().registerEvents(new MercuryMessageListener(this), this);
		}
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

	public CivChat2Config getPluginConfig() {
		return config_;
	}

	public CivChat2FileLogger getCivChat2FileLogger() {
		return fileLog;
	}

	public boolean isMercuryEnabled() {
		return isMercuryEnabled;
	}

	public void setMercuryEnabled(boolean isMercuryEnabled) {
		this.isMercuryEnabled = isMercuryEnabled;
	}

	
}
