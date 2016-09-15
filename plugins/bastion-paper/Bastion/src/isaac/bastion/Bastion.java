package isaac.bastion;

import java.util.LinkedList;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import isaac.bastion.commands.BastionCommandManager;
import isaac.bastion.commands.ModeChangeCommand;
import isaac.bastion.commands.PlayersStates.Mode;
import isaac.bastion.listeners.BastionListener;
import isaac.bastion.listeners.CommandListener;
import isaac.bastion.listeners.ElytraListener;
import isaac.bastion.listeners.EnderPearlListener;
import isaac.bastion.manager.BastionBlockManager;
import isaac.bastion.manager.ConfigManager;
import isaac.bastion.storage.BastionBlockStorage;
import isaac.bastion.storage.Database;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public final class Bastion extends JavaPlugin {
	private static BastionListener listener; ///Main listener
	private static Bastion plugin; ///Holds the plugin
	private static BastionBlockManager bastionManager; ///Most of the direct interaction with Bastions
	private static ConfigManager config; ///Holds the configuration
	
	public void onEnable() 	{
		//set the static variables
		plugin = this;
		config = new ConfigManager();
		bastionManager = new BastionBlockManager();
		listener = new BastionListener();
		registerNameLayerPermissions();
		
		removeGhostBlocks();
		
		if(!this.isEnabled()) //check that the plugin was not disabled in setting up any of the static variables
			return;
		
		registerListeners();
		registerCommands();
	}
	
	private void registerListeners() {
		getServer().getPluginManager().registerEvents(listener, this);
		getServer().getPluginManager().registerEvents(new CommandListener(), this);
		getServer().getPluginManager().registerEvents(new EnderPearlListener(), this);
		getServer().getPluginManager().registerEvents(new ElytraListener(), this);
	}

	//Sets up the command managers
	private void registerCommands(){
		getCommand("Bastion").setExecutor(new BastionCommandManager());
		getCommand("bsi").setExecutor(new ModeChangeCommand(Mode.INFO));
		getCommand("bsd").setExecutor(new ModeChangeCommand(Mode.DELETE));
		getCommand("bso").setExecutor(new ModeChangeCommand(Mode.NORMAL));
		getCommand("bsb").setExecutor(new ModeChangeCommand(Mode.BASTION));
		getCommand("bsf").setExecutor(new ModeChangeCommand(Mode.OFF));
		getCommand("bsm").setExecutor(new ModeChangeCommand(Mode.MATURE));
	}

	public void onDisable() {
		if(bastionManager != null) {
			bastionManager.close(); //saves all Bastion Blocks
		}
	}

	public static BastionBlockManager getBastionManager() {
		return bastionManager;
	}
	public static Bastion getPlugin() {
		return plugin;
	}
	public static BastionListener getBastionBlockListener() {
		return listener;
	}
	public static ConfigManager getConfigManager() {
		return config;
	}
	
	public void removeGhostBlocks(){
		Database db = BastionBlockStorage.db;
		Bukkit.getLogger().log(Level.INFO, "Bastion is beginning ghost block check.");
		for (BastionBlock block: bastionManager.set) {
			if (block.getLocation().getBlock().getType() != block.getType().getMaterial().getItemType() &&
					block.getLocation().getBlock().getData() != block.getType().getMaterial().getData()) {
				Bukkit.getLogger().log(Level.INFO, "Bastion removed a block at: " + block.getLocation() + ". If it is still"
						+ " there, there is a problem...");
				block.delete(db);
			}
		}
		Bukkit.getLogger().log(Level.INFO, "Bastion has ended ghost block check.");
	}
	
	public void registerNameLayerPermissions() {
		LinkedList <PlayerType> memberAndAbove = new LinkedList<PlayerType>();
		memberAndAbove.add(PlayerType.MEMBERS);
		memberAndAbove.add(PlayerType.MODS);
		memberAndAbove.add(PlayerType.ADMINS);
		memberAndAbove.add(PlayerType.OWNER);
		LinkedList <PlayerType> modAndAbove = new LinkedList<PlayerType>();
		modAndAbove.add(PlayerType.MODS);
		modAndAbove.add(PlayerType.ADMINS);
		modAndAbove.add(PlayerType.OWNER);
		PermissionType.registerPermission("BASTION_PEARL", memberAndAbove);
		PermissionType.registerPermission("BASTION_PLACE", modAndAbove);
	}

}
