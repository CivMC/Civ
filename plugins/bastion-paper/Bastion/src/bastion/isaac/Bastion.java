package bastion.isaac;


import org.bukkit.plugin.java.JavaPlugin;


public final class Bastion extends JavaPlugin
{
	private static BastionListener listener;
	private static Bastion plugin;

	public void onEnable()
	{
		plugin = this;
		listener = new BastionListener();
		getLogger().info("onEnable has been invoked!");
		getServer().getPluginManager().registerEvents(listener, this);
	}

	public void onDisable()
	{
		getLogger().info("onDisable has been invoked!");
	}

	public static Bastion getPlugin()
	{
		return plugin;
	}
}