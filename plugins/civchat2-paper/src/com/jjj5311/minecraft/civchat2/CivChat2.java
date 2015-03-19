package com.jjj5311.minecraft.civchat2;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import com.jjj5311.minecraft.civchat2.utility.CivChat2Config;
import com.jjj5311.minecraft.civchat2.utility.CivChat2Log;

/**
 * @author jjj5311
 *
 */
public class CivChat2 extends JavaPlugin{
	
	private static CivChat2 instance;
	private static CivChat2Log log_;
	private static boolean groupsEnabled;
	private static CivChat2Config config_;
	private static boolean debug = false;
	private static CivChat2Manager chatMan;
	
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
		log_.debug("Debug Test");
		chatMan = new CivChat2Manager();
	}
	
	public void onDisable(){
		//onDisable stuff
	}
	
	public static CivChat2Manager getCivChat2Manager(){
		return chatMan;
	}

	public static boolean debugEnabled() {
		return config_.getDebug();
	}

	public static CivChat2Log getCivChat2Log() {
		return log_;
	}

	
}
