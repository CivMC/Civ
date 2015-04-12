package vg.civcraft.mc.namelayer.config;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.config.annotations.NameConfig;
import vg.civcraft.mc.namelayer.config.annotations.NameConfigs;

/**
 * These classes found in this package are heavily influenced by those found in
 * https://github.com/Civcraft/Humbug
 * 
 * Erocs wrote all the classes and I am modifying them in a way that benefits 
 * NameLayer's API. So while Erocs did not directly write the classes found here
 * his code is the inspiration to what is found.
 * 
 * @author rourke750, Erocs
 *
 * ConfigManager is the class used to handle config annotations.
 */

public class NameConfigManager {

	public Map<JavaPlugin, Map<String, NameConfigNode>> conf = new HashMap<JavaPlugin, Map<String, NameConfigNode>>();
	
	/**
	 * Register your class that hosts annotations with this method.  It will scan your class and input your variables.
	 */
	public void registerListener(JavaPlugin plugin, NameConfigListener l){
		scanMethods(plugin, l);
		setConfig(plugin);
	}
	
	private void setConfig(JavaPlugin plugin){
		FileConfiguration config = plugin.getConfig();
		for (NameConfigNode c : conf.get(plugin).values()){
			switch (c.getType()){
			case Bool:
				config.set(c.getName(), c.getBool());
				break;
			case String:
				config.set(c.getName(), c.getString());
				break;
			case Int:
				config.set(c.getName(), c.getInt());
				break;
			case Double:
				config.set(c.getName(), c.getDouble());
				break;
			}
		}
		plugin.saveConfig();
	}
	
	private void scanMethods(JavaPlugin plugin, NameConfigListener l){
		try {
			for (Method method: l.getClass().getMethods()){
				NameConfig c = method.getAnnotation(NameConfig.class);
				if (c != null){
					addToConfig(plugin, c);
					continue;
				}
				
				NameConfigs cs = method.getAnnotation(NameConfigs.class);
				if (cs != null){
					for (NameConfig cc: cs.values()){
						addToConfig(plugin, cc);
					}
					continue;
				}
			}
		}catch(Exception ex) {
			NameLayerPlugin.log(Level.INFO, ex.toString());
		}
	}
	
	private void addToConfig(JavaPlugin plugin, NameConfig config){
		Map<String, NameConfigNode> c = conf.get(plugin);
		if (c.containsKey(config.name()))
			NameLayerPlugin.log(Level.INFO, "Duplicate entry found, overriding config entry.");
		c.put(config.name(), new NameConfigNode(plugin, config));
	}
	
	/**
	 * Returns the ConfigNode used to get your config options.
	 * @return Returns the NameConfigNode.
	 */
	public NameConfigNode get(JavaPlugin plugin, String name){
		return conf.get(plugin).get(name);
	}
}
