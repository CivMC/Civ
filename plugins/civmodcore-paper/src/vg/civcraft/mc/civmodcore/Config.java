package vg.civcraft.mc.civmodcore;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.reflect.ClassPath;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import vg.civcraft.mc.civmodcore.annotations.CivConfig;
import vg.civcraft.mc.civmodcore.annotations.CivConfigs;
import vg.civcraft.mc.civmodcore.annotations.ConfigOption;
import vg.civcraft.mc.civmodcore.util.ClassUtility;

public class Config {
  //private static Config global_instance_ = null;

  // ================================================
  // Configuration defaults
  public boolean DebugLog = false;

  private FileConfiguration config_ = null;

  public void initialize() {
	  plugin_.reloadConfig();
      config_ = plugin_.getConfig();
  }

  public ConfigurationSection getStorage() {
    return config_;
  }

  private ACivMod plugin_ = null;

  public Config(ACivMod plugin) {

	    plugin_ = plugin;
	    initialize();
	    
	    for(Class clazz:ClassUtility.GetClassesForPlugin(plugin)){
	    	scanAnnotations(clazz);
	    }
	    setConfig();
  }
  private Map<String, ConfigOption> dynamicOptions_ = new TreeMap<String, ConfigOption>();

  private void addToConfig(CivConfig bug) {
    if (dynamicOptions_.containsKey(bug.name())) {
    	plugin_.info("Duplicate configuration option detected: " + bug.name());
      return;
    }
    dynamicOptions_.put(bug.name(), new ConfigOption(this, bug));
  }

  private void scanAnnotations(Class clazz) {
    try {
//   plugin_.info("loading annotations for: "+clazz.getName());
      for (Method method : clazz.getMethods()) {

//        plugin_.info("scanning: "+method.getName());
        CivConfig bug = method.getAnnotation(CivConfig.class);
        if (bug != null) {
//            plugin_.info("adding: "+bug.toString());
          addToConfig(bug);
          continue;
        }
        CivConfigs bugs = method.getAnnotation(CivConfigs.class);
        if (bugs != null) {
          for (CivConfig drone : bugs.value()) {
//            plugin_.info("adding: "+drone.toString());
            addToConfig(drone);
          }
          continue;
        }
      }
    } catch(NoClassDefFoundError e){
    	// We want to ignore any errors found from this because it could be a soft dependency that is not loaded for a plugin.
    } catch(Exception ex) {
    	plugin_.info(ex.toString());
    }
  }
//  private void scanAnnotations() {
//    try {
//    	plugin_.info("loading annotations");
//      for (Method method : plugin_.getClass().getMethods()) {
//
//      	plugin_.info("scanning: "+method.getName());
//        CivConfig bug = method.getAnnotation(CivConfig.class);
//        if (bug != null) {
//
//          plugin_.info("adding: "+bug.toString());
//          addToConfig(bug);
//          continue;
//        }
//        CivConfigs bugs = method.getAnnotation(CivConfigs.class);
//        if (bugs != null) {
//          for (CivConfig drone : bugs.value()) {
//
//            plugin_.info("adding: "+drone.toString());
//            addToConfig(drone);
//          }
//          continue;
//        }
//      }
//    } catch(Exception ex) {
//    	plugin_.info(ex.toString());
//    }
//  }

  public void load() {
  }

  public void reload() {
    plugin_.reloadConfig();
  }

  public void save() {
    plugin_.saveConfig();
  }

  public ConfigOption get(String optionName) {
    return dynamicOptions_.get(optionName);
  }

  public boolean set(String optionName, String value) {
    ConfigOption opt = dynamicOptions_.get(optionName);
    if (opt != null) {
      opt.setString(value);
      return true;
    }
    return false;
  }
  
  private void setConfig(){
		for (ConfigOption c : dynamicOptions_.values()){
			switch (c.getType()){
			case Bool:
				config_.set(c.getName(), c.getBool());
				break;
			case String:
				config_.set(c.getName(), c.getString());
				break;
			case Int:
				config_.set(c.getName(), c.getInt());
				break;
			case Double:
				config_.set(c.getName(), c.getDouble());
				break;
			}
		}
		plugin_.saveConfig();
	}
}