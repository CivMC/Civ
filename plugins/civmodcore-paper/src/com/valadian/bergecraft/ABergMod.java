package com.valadian.bergecraft;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.valadian.bergecraft.annotations.*;
import com.valadian.bergecraft.interfaces.ApiManager;

public abstract class ABergMod extends JavaPlugin implements Listener  {
	protected static String pluginName = "BergMod";
	public static void severe(String message) {
	    log_.severe("["+pluginName+"] " + message);
	}
	
	public static void warning(String message) {
	    log_.warning("["+pluginName+"] " + message);
	}
	public static void info(String message) {
	    log_.info("["+pluginName+"] " + message);
	}
	
	public static void debug(String message) {
	    if (Config.DebugLog) {
	      log_.info("["+pluginName+"] " + message);
	    }
	}

    public static Config config_ = null;
		  
    //private File configFile;
    private static final Logger log_ = Logger.getLogger(pluginName);
    private static ABergMod global_instance_ = null;
    //private static String mainDirectory = "plugins/"+pluginName;
    
    public ApiManager apis;    
    
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args) {
      if (//!(sender instanceof ConsoleCommandSender) ||
          !command.getName().equals(pluginName) ||
          args.length < 1) {
        return false;
      }
      String option = args[0];
      String value = null;
      String subvalue = null;
      boolean set = false;
      boolean subvalue_set = false;
      String msg = "";
      if (args.length > 1) {
        value = args[1];
        set = true;
      }
      if (args.length > 2) {
        subvalue = args[2];
        subvalue_set = true;
      }
      ConfigOption opt = config_.get(option);
      if (opt != null) {
        if (set) {
          opt.set(value);
        }
        msg = String.format("%s = %s", option, opt.getString());
      } else if (option.equals("debug")) {
        if (set) {
          Config.DebugLog = toBool(value);
        }
        msg = String.format("debug = %s", config_.DebugLog);
      } else if (option.equals("save")) {
        config_.save();
        msg = "Configuration saved";
      } else if (option.equals("reload")) {
        config_.reload();
        msg = "Configuration loaded";
      } else {
        msg = String.format("Unknown option %s", option);
      }
      sender.sendMessage(msg);
      return true;
    }
    // ================================================
    // General

    public void onLoad()
    {
      loadConfiguration();
      loadApis();
      info("Loaded");
    }
    private void loadConfiguration() {
        config_ = Config.initialize(this);
      }
    protected void loadApis() {
    	  //ApiManager.disablerApis.add(new CompatGimmickApi());
    }
    public void onEnable() {
      registerEvents();
      registerCommands();
      global_instance_ = this;
      info("Enabled");
    }
    private void registerEvents() {
      getServer().getPluginManager().registerEvents(this, this);
    }

    public void registerCommands() {
      ConsoleCommandSender console = getServer().getConsoleSender();
      console.addAttachment(this, pluginName+".console", true);
    }
    public boolean isInitiaized() {
      return global_instance_ != null;
    }

    public boolean toBool(String value) {
      if (value.equals("1") || value.equalsIgnoreCase("true")) {
        return true;
      }
      return false;
    }
}
