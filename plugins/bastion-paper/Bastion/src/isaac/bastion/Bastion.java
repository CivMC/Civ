package isaac.bastion;




import isaac.bastion.commands.BastionCommandManager;
import isaac.bastion.commands.ModeChangeCommand;
import isaac.bastion.commands.PlayersStates.Mode;
import isaac.bastion.listeners.BastionListener;
import isaac.bastion.listeners.CommandListener;
import isaac.bastion.listeners.EnderPearlListener;
import isaac.bastion.manager.BastionBlockManager;
import isaac.bastion.manager.ConfigManager;

import org.bukkit.plugin.java.JavaPlugin;




public final class Bastion extends JavaPlugin
{
	private static BastionListener listener; ///Main listener
	private static Bastion plugin; ///Holds the plugin
	private static BastionBlockManager bastionManager; ///Most of the direct interaction with Bastions
	private static ConfigManager config; ///Holds the configuration
	
	public void onEnable()
	{
		//set the static variables
		plugin = this;
		config = new ConfigManager();
		bastionManager = new BastionBlockManager();
		listener = new BastionListener();
		
		if(!this.isEnabled()) //check that the plugin was not disabled in setting up any of the static variables
			return;
		
		registerListeners();
		registerCommands();
	}
	
	//What the name says
	private void registerListeners(){
		getServer().getPluginManager().registerEvents(listener, this);
		getServer().getPluginManager().registerEvents(new CommandListener(), this);
		if(config.getEnderPearlsBlocked()) //currently everything to do with blocking pearls is part of EnderPearlListener. Needs changed
			getServer().getPluginManager().registerEvents(new EnderPearlListener(), this);
	}
	
	

	//Sets up the command managers
	private void registerCommands(){
		getCommand("Bastion").setExecutor(new BastionCommandManager());
		getCommand("bsi").setExecutor(new ModeChangeCommand(Mode.INFO));
		getCommand("bsd").setExecutor(new ModeChangeCommand(Mode.DELETE));
		getCommand("bso").setExecutor(new ModeChangeCommand(Mode.NORMAL));
		getCommand("bsn").setExecutor(new ModeChangeCommand(Mode.DISABLED));
	}

	public void onDisable()
	{
		if(bastionManager==null)
			return;
		bastionManager.close();//saves all Bastion Blocks
	}

	public static BastionBlockManager getBastionManager()
	{
		return bastionManager;
	}
	public static Bastion getPlugin()
	{
		return plugin;
	}
	public static BastionListener getBastionBlockListener()
	{
		return listener;
	}
	public static ConfigManager getConfigManager(){
		return config;
	}

}