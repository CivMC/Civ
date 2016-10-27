package org.bukkit.plugin.java;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;

public class PluginLoaderProxy  {
	
	private static final File pluginsDirectory = new File("bin/test/server/plugins/");
	private static final File testPlugin = new File("TestPlugin.jar");
	
	private final PluginLoader pluginLoader;
	
	public PluginLoaderProxy(final PluginLoader pluginLoader) {
		this.pluginLoader = pluginLoader;
	}
	
	@SuppressWarnings("unchecked")
	public Plugin loadPlugin(String pluginName, String pluginVersion, String mainClass) throws InvalidPluginException {
		
		final PluginDescriptionFile description = new PluginDescriptionFile(pluginName, pluginVersion, mainClass);
        final PluginClassLoader loader;
        try {       	
        	
            loader = new PluginClassLoader((JavaPluginLoader) pluginLoader, null, description, pluginsDirectory, testPlugin);
        } catch (InvalidPluginException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new InvalidPluginException(ex);
        }

		try {
			final Field f = pluginLoader.getClass().getDeclaredField("loaders");
			f.setAccessible(true);
			List<PluginClassLoader> loaders = (List<PluginClassLoader>) f.get(pluginLoader);
			loaders.add(loader);
		} catch (Exception ex) {
            throw new InvalidPluginException(ex);
		}
		
		return loader.plugin;
	}
}
