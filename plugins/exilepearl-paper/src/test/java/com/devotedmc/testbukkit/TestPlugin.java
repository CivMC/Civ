package com.devotedmc.testbukkit;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Charsets;

public class TestPlugin<T extends JavaPlugin> {

	private final Class<T> clazz;
	private final PluginDescriptionFile description;
	private YamlConfiguration config = new YamlConfiguration();
	private JavaPlugin instance = null;

	public TestPlugin(final Class<T> clazz) throws Exception {
		this.clazz = clazz;

		// Make sure the class is loaded
		getClass().getClassLoader().loadClass(clazz.getName());

		try {
			description = new PluginDescriptionFile(getResource("plugin.yml"));
		} catch (Exception ex) {
			throw new  InvalidDescriptionException(ex);
		}

        final InputStream configStream = getResource("config.yml");
        if (configStream == null) {
            return;
        }
        config = YamlConfiguration.loadConfiguration(new InputStreamReader(configStream, Charsets.UTF_8));
	}

	/**
	 * Gets the plugin class
	 * @return The plugin class
	 */
	public Class<? extends JavaPlugin> getPluginClass() {
		return clazz;
	}

	/**
	 * Gets the plugin name
	 * @return The plugin name
	 */
	public String getName() {
		return description.getName();
	}

	/**
	 * Gets the plugin description
	 * @return The plugin description
	 */
	public PluginDescriptionFile getDescription() {
		return  description;
	}

	/**
	 * Gets the Yaml config
	 * @return The Yaml config
	 */
	public YamlConfiguration getConfig() {
		return config;
	}


	/**
	 * Sets the Yaml config
	 * @param config The new config
	 */
	public void setConfig(YamlConfiguration config) {
		this.config = config;
	}

	/**
	 * Sets a configuration value
	 * @param path The config item path
	 * @param value The object value
	 */
	public TestPlugin<T> appendConfig(String path, Object value) {
		config.set(path, value);
		return this;
	}


	@SuppressWarnings("unchecked")
	public T getInstance() {
		return (T)instance;
	}

	public void setInstance(JavaPlugin instance) {
		this.instance = instance;
	}

	/**
	 * Gets a resource
	 * @param filename The resource name
	 * @return The input stream
	 */
    private InputStream getResource(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }

        try {    	
            URL url = null;
            String[] names = clazz.getCanonicalName().split("\\.");
            String name1 = names[names.length - 1].toLowerCase();
            String name2 = names[names.length - 2].toLowerCase();
            
            // Bit of a hack, but find the version that corresponds to this plugin.
            // With bukkit, each plugin has it's own classloader, but all the test plugins
            // use the same system classloader here
            Enumeration<URL> urls = clazz.getClassLoader().getResources(filename);
            while(urls.hasMoreElements()) {
            	URL next = urls.nextElement();
            	if (next.getPath().toLowerCase().contains(name1)) {
            		url = next;
            		break;
            	}
            }
            
            if (url == null) {
            	urls = clazz.getClassLoader().getResources(filename);
                while(urls.hasMoreElements()) {
                	URL next = urls.nextElement();
                	if (next.getPath().toLowerCase().contains(name2)) {
                		url = next;
                		break;
                	}
                }
            }

            if (url == null) {
                return null;
            }

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException ex) {
            return null;
        }
    }

}
