package spaceFountain.bastion;




import org.bukkit.plugin.java.JavaPlugin;

import spaceFountain.bastion.commands.BastionCommandManager;
import spaceFountain.bastion.commands.CommandListener;
import spaceFountain.bastion.commands.DeleteCommandManager;
import spaceFountain.bastion.commands.InfoCommandManager;
import spaceFountain.bastion.commands.NormalCommandManager;
import spaceFountain.bastion.manager.BastionBlockManager;
import spaceFountain.bastion.manager.ConfigManager;




public final class Bastion extends JavaPlugin
{
	private static BastionListener listener; ///Main listener
	private static Bastion plugin; ///Holds the plugin
	private static BastionBlockManager bastionManager; ///Most of the direct interaction with Bastions
	private static ConfigManager config; ///Holds the configuration
	
	public void onEnable()
	{
		plugin = this;
		config = new ConfigManager();
		bastionManager = new BastionBlockManager();
		listener = new BastionListener();
		
		getServer().getPluginManager().registerEvents(listener, this);
		getServer().getPluginManager().registerEvents(new CommandListener(), this);
		if(config.getEnderPearlsBlocked())
			getServer().getPluginManager().registerEvents(new EnderPearlListener(), this);
		registerCommands();
	}
	/**
	 * registerCommands()
	 * Sets up the command managers
	 */
	private void registerCommands(){
		getCommand("Bastion").setExecutor(new BastionCommandManager());
		getCommand("bsi").setExecutor(new InfoCommandManager());
		getCommand("bsd").setExecutor(new DeleteCommandManager());
		getCommand("bso").setExecutor(new NormalCommandManager());
	}

	public void onDisable()
	{
		bastionManager.close();		
	}

	public static BastionBlockManager getBastionManager()
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