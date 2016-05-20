package vg.civcraft.mc.namelayer.bungee;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class NameLayerBungee extends Plugin {
	
	private File configFile;
	private static NameLayerBungee plugin;
	private ConfigurationProvider configManager;
	private Configuration config;
	private ProxyServer proxy;
	private static DataBaseManager db;
	
	public void onEnable() {
		proxy = getProxy();
		plugin = this;
		configFile = new File(this.getDataFolder(), "config.yml");
		loadConfiguration();
		new ConfigManager(config);
		Runnable run = new Runnable() {

			@Override
			public void run() {
				db = new DataBaseManager();
				BungeeListener listener = new BungeeListener(db);
				proxy.getPluginManager().registerListener(plugin, listener);
			}
			
		};
		runAsync(run);
	}

	public void loadConfiguration() {
		if (!getDataFolder().exists())
			getDataFolder().mkdir();
		configManager = ConfigurationProvider.getProvider(YamlConfiguration.class);
		if (!configFile.exists()) {
			try {
                configFile.createNewFile();
                try (InputStream is = getResourceAsStream("bungee" + File.separator + "config.yml");
                     OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to create configuration file", e);
            }
        }
		try {
			config = configManager.load(configFile);
		} catch (IOException e) {
			config = new Configuration();
			saveConfiguration();
		}
	}
	
	public void saveConfiguration() {
		try {
			configManager.save(config, configFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static NameLayerBungee getInstance() {
		return plugin;
	}
	
	public void runAsync(Runnable run){
		proxy.getScheduler().runAsync(plugin, run);
	}
	
	public static String getPlayerName(UUID uuid) {
		return db.getCurrentName(uuid);
	}
	
	public static UUID getUUIDFromPlayerName(String name) {
		return db.getUUID(name);
	}
}
