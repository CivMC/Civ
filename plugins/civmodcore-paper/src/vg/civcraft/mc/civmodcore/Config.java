package vg.civcraft.mc.civmodcore;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.minecraft.util.com.google.common.reflect.ClassPath;
import net.minecraft.util.com.google.common.reflect.ClassPath.ClassInfo;
import net.minecraft.util.com.google.common.collect.ImmutableSet;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Splitter;

import vg.civcraft.mc.civmodcore.annotations.CivConfig;
import vg.civcraft.mc.civmodcore.annotations.CivConfigs;
import vg.civcraft.mc.civmodcore.annotations.ConfigOption;

public class Config {
  //private static Config global_instance_ = null;

  // ================================================
  // Configuration defaults
  public boolean DebugLog = false;

  private FileConfiguration config_ = null;

  public void initialize() {
//    if (global_instance_ == null) {
    	plugin_.reloadConfig();
      config_ = plugin_.getConfig();
      config_.options().copyDefaults(true);
//      global_instance_ = new Config(plugin);
      load();
//    }
//    return global_instance_;
  }

  public ConfigurationSection getStorage() {
    return config_;
  }

  private ACivMod plugin_ = null;
  private Set<Integer> remove_item_drops_ = null;

  public Config(ACivMod plugin) {

	    plugin_ = plugin;
	    initialize();
	    
	    try {
	    	Package pack= plugin_.getClass().getPackage();
	    	if(pack==null){
	    		plugin_.info("Package name REQUIRED for annotation loading");
	    	}
	    	else{

		    	String packageName = pack.getName();		        
		        ClassLoader classloader = plugin_.classLoader;
		        ClassPath path = ClassPath.from(classloader);
		        
		        for (ClassPath.ClassInfo info : path.getTopLevelClassesRecursive(packageName)) {
		            Class clazz = Class.forName(info.getName(), true, classloader);
		            scanAnnotations(clazz);
		        }
	    	}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}    
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
    } catch(Exception ex) {
    	plugin_.info(ex.toString());
    }
  }
  private void scanAnnotations() {
    try {
    	plugin_.info("loading annotations");
      for (Method method : plugin_.getClass().getMethods()) {

      	plugin_.info("scanning: "+method.getName());
        CivConfig bug = method.getAnnotation(CivConfig.class);
        if (bug != null) {

          plugin_.info("adding: "+bug.toString());
          addToConfig(bug);
          continue;
        }
        CivConfigs bugs = method.getAnnotation(CivConfigs.class);
        if (bugs != null) {
          for (CivConfig drone : bugs.value()) {

            plugin_.info("adding: "+drone.toString());
            addToConfig(drone);
          }
          continue;
        }
      }
    } catch(Exception ex) {
    	plugin_.info(ex.toString());
    }
  }

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
}
