package org.bukkit.plugin.java;

import static org.mockito.Mockito.*;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;

public class TestPluginLoader {
	
	private final Server server;
	final JavaPluginLoader pluginLoader;
	private File dataFolder = new File("test");
	private File file = new File("test");
	
	@SuppressWarnings("deprecation")
	public TestPluginLoader(final Server server) {
		this.server = server;
		this.pluginLoader = new JavaPluginLoader(this.server);
	}

	
	@SuppressWarnings("unchecked")
	public void loadTestPlugin(String pluginName, String pluginVersion, String mainClass) throws Exception {		
		PluginDescriptionFile pdf = new PluginDescriptionFile(pluginName, pluginVersion, mainClass);
		PluginClassLoader loader = new PluginClassLoader(pluginLoader, pluginLoader.getClass().getClassLoader(), pdf, dataFolder, file);

		Field f = pluginLoader.getClass().getDeclaredField("loaders");
		f.setAccessible(true);
		List<PluginClassLoader> loaders = (List<PluginClassLoader>) f.get(pluginLoader);
		loaders.add(loader);
	}
}
