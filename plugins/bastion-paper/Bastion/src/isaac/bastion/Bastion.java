package isaac.bastion;




import isaac.bastion.commands.BastionCommandManager;
import isaac.bastion.commands.CommandListener;
import isaac.bastion.commands.DeleteCommandManager;
import isaac.bastion.commands.InfoCommandManager;
import isaac.bastion.commands.NormalCommandManager;
import isaac.bastion.manager.BastionBlockManager;
import isaac.bastion.manager.ConfigManager;

import org.bukkit.plugin.java.JavaPlugin;




public final class Bastion extends JavaPlugin
{
	private static BastionListener listener;
	private static Bastion plugin;
	private static BastionBlockManager bastionManager;
	private static ConfigManager config;

	public void onEnable()
	{
		plugin = this;
		config = new ConfigManager();
		bastionManager = new BastionBlockManager();
		listener = new BastionListener();
		
		getServer().getPluginManager().registerEvents(listener, this);
		getServer().getPluginManager().registerEvents(new CommandListener(), this);
		registerCommands();
	}
	private void registerCommands(){
		getCommand("Bastion").setExecutor(new BastionCommandManager());
		getCommand("bsi").setExecutor(new InfoCommandManager());
		getCommand("bsd").setExecutor(new DeleteCommandManager());
		getCommand("bso").setExecutor(new NormalCommandManager());
	}

	public void onDisable()
	{
		bastionManager.close();
		getLogger().info("onDisable has been invoked!");
		
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