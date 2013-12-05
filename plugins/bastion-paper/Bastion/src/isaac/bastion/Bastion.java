package isaac.bastion;


import org.bukkit.plugin.java.JavaPlugin;


public final class Bastion extends JavaPlugin
{
	private static BastionListener listener;
	private static Bastion plugin;
	private static BastionManager bastionManager;

	public void onEnable()
	{
		plugin = this;
		bastionManager = new BastionManager();
		listener = new BastionListener();
		getLogger().info("onEnable has been invoked!");
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
}