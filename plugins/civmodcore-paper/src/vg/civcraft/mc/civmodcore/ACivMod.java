package vg.civcraft.mc.civmodcore;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civmodcore.annotations.*;
import vg.civcraft.mc.civmodcore.command.CommandHandler;
import vg.civcraft.mc.civmodcore.interfaces.ApiManager;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventoryListener;
import vg.civcraft.mc.civmodcore.util.ClassUtility;

public abstract class ACivMod extends JavaPlugin implements Listener{
	
	protected CommandHandler handle;
	
	protected abstract String getPluginName();
	
	public void severe(String message) {
	    log_.severe("["+getPluginName()+"] " + message);
	}
	
	public void warning(String message) {
	    log_.warning("["+getPluginName()+"] " + message);
	}
	public void info(String message) {
	    log_.info("["+getPluginName()+"] " + message);
	}
	
	public void debug(String message) {
	    if (config_.DebugLog) {
	      log_.info("["+getPluginName()+"] " + message);
	    }
	}

	public Config GetConfig(){
		if(config_==null){
			log_.info("Config not initialized. Most likely due to overriding onLoad and not calling super.onLoad()");
		}
		return config_;
	}
    private Config config_ = null;
    public ClassLoader classLoader = null;
    private final Logger log_ = Logger.getLogger(getPluginName());
    
    public ApiManager apis;    
    
    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args) {
      if (//!(sender instanceof ConsoleCommandSender) ||
          !command.getName().equals(getPluginName().toLowerCase()) ||
          args.length < 1) {
    	  return handle == null ? false : handle.execute(sender, command, args);
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
        	config_.DebugLog = toBool(value);
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

    @Override
    public void onLoad()
    {
      classLoader = getClassLoader();
      loadConfiguration();
      info("Configuration Loaded");
    }
    private void loadConfiguration() {
        config_ = new Config(this);
        System.out.println("loaded config for: "+getPluginName() + "Config: "+ (config_!=null));
    }
    @Override
    public void onEnable() {
      registerEvents();
      registerCommands();
      //global_instance_ = this;
      info("Main Plugin Events and Config Command registered");
    }
    private void registerEvents() {
    	getServer().getPluginManager().registerEvents(this, this);
    	getServer().getPluginManager().registerEvents(new ClickableInventoryListener(), this);
    }
    public void registerCommands() {
      ConsoleCommandSender console = getServer().getConsoleSender();
      console.addAttachment(this, getPluginName().toLowerCase()+".console", true);
    }
//    public boolean isInitiaized() {
//      return global_instance_ != null;
//    }

    public boolean toBool(String value) {
      if (value.equals("1") || value.equalsIgnoreCase("true")) {
        return true;
      }
      return false;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args){
    	return handle == null ? null : handle.complete(sender, cmd, args);
    }
    public CommandHandler getCommandHandler(){
    	return handle;
    }
    protected void setCommandHandler(CommandHandler handle){
    	this.handle = handle;
    }
}
