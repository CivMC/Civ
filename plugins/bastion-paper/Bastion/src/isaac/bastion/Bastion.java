package isaac.bastion;



import java.io.InputStream;
import java.util.Scanner;

import isaac.bastion.manager.BastionBlockManager;
import isaac.bastion.manager.ConfigManager;


import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
		getLogger().info("Port is "+config.getPort()+" Material is "+config.getBastionBlockMaterial());
		
		getServer().getPluginManager().registerEvents(listener, this);
	}

	public void onDisable()
	{
		bastionManager.close();
		getLogger().info("onDisable has been invoked!");
		
	}
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(label.equalsIgnoreCase("Bastion")){
			if(args.length>0){
				if(args[0].equalsIgnoreCase("License")){
					InputStream input = getClass().getResourceAsStream("/License.txt");
					sender.sendMessage(convertStreamToString(input));
					return true;
				}
			}
		}
		return false; 
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
	public static String convertStreamToString(InputStream is) {
	    Scanner s = new Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
}