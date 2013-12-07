package isaac.bastion;


import java.util.logging.Logger;

import isaac.bastion.manager.BastionManager;
import isaac.bastion.manager.ConfigManager;

import org.bukkit.plugin.java.JavaPlugin;



public final class Bastion extends JavaPlugin
{
	private static BastionListener listener;
	private static Bastion plugin;
	private static BastionManager bastionManager;
	private static ConfigManager config;

	public void onEnable()
	{
		plugin = this;
		config = new ConfigManager();
		bastionManager = new BastionManager();
		listener = new BastionListener();
		getLogger().info("Port is "+config.getPort()+" Material is "+config.getBastionBlockMaterial());
		
		getServer().getPluginManager().registerEvents(listener, this);
	}

	public void onDisable()
	{
		bastionManager.save();
		getLogger().info("onDisable has been invoked!");
		
	}

	public static BastionManager getBastionManager()
	{
		return bastionManager;
	}
	public static Bastion getPlugin()
	{
		return plugin;
	}
	public static BastionListener getListenerr()
	{
		return listener;
	}
	public static ConfigManager getConfigManager(){
		return config;
	}
}