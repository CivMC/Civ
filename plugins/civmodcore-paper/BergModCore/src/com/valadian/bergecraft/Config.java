package com.valadian.bergecraft;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Splitter;
import com.valadian.bergecraft.annotations.Bergification;
import com.valadian.bergecraft.annotations.Bergifications;
import com.valadian.bergecraft.annotations.ConfigOption;

public class Config {
  private static Config global_instance_ = null;

  // ================================================
  // Configuration defaults
  public static boolean DebugLog = false;

  private static FileConfiguration config_ = null;

  public static Config initialize(ABergMod plugin) {
    if (global_instance_ == null) {
      plugin.reloadConfig();
      config_ = plugin.getConfig();
      config_.options().copyDefaults(true);
      global_instance_ = new Config(plugin);
      global_instance_.load();
    }
    return global_instance_;
  }

  public static ConfigurationSection getStorage() {
    return config_;
  }

  private ABergMod plugin_ = null;
  private Set<Integer> remove_item_drops_ = null;

  public Config(ABergMod plugin) {
    plugin_ = plugin;
    scanAnnotations();
  }

  private Map<String, ConfigOption> dynamicOptions_ = new TreeMap<String, ConfigOption>();

  private void addToConfig(Bergification bug) {
    if (dynamicOptions_.containsKey(bug.opt())) {
    	ABergMod.info("Duplicate configuration option detected: " + bug.opt());
      return;
    }
    dynamicOptions_.put(bug.opt(), new ConfigOption(bug));
  }

  private void scanAnnotations() {
    try {
      for (Method method : ABergMod.class.getMethods()) {
        Bergification bug = method.getAnnotation(Bergification.class);
        if (bug != null) {
          addToConfig(bug);
          continue;
        }
        Bergifications bugs = method.getAnnotation(Bergifications.class);
        if (bugs != null) {
          for (Bergification drone : bugs.value()) {
            addToConfig(drone);
          }
          continue;
        }
      }
    } catch(Exception ex) {
    	ABergMod.info(ex.toString());
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
