package bastion.isaac;

import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public final class Bastion extends JavaPlugin
{
  private static BastionListener listener;
  private static Bastion plugin;

  public void onEnable()
  {
    listener = new BastionListener();
    getLogger().info("onEnable has been invoked!");
    getServer().getPluginManager().registerEvents(listener, this);
    plugin = this;
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