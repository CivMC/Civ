package isaac.bastion;

import java.util.LinkedList;

import isaac.bastion.commands.BastionCommandManager;
import isaac.bastion.commands.ModeChangeCommand;
import isaac.bastion.commands.PlayersStates.Mode;
import isaac.bastion.listeners.BastionBreakListener;
import isaac.bastion.listeners.BastionDamageListener;
import isaac.bastion.listeners.BastionInteractListener;
import isaac.bastion.listeners.ElytraListener;
import isaac.bastion.manager.BastionBlockManager;
import isaac.bastion.storage.BastionBlockStorage;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public final class Bastion extends ACivMod {
	private static Bastion plugin;
	private static BastionBlockStorage storage;
	private static BastionBlockManager manager;
	
	public void onEnable() 	{
		plugin = this;
		saveDefaultConfig();
		reloadConfig();
		BastionType.loadBastionTypes(getConfig().getConfigurationSection("bastions"));
		setupDatabase();
		registerNameLayerPermissions();
		manager = new BastionBlockManager();
		
		if(!this.isEnabled()) //check that the plugin was not disabled in setting up any of the static variables
			return;
		
		registerListeners();
		setupCommands();
	}
	
	public void onDisable() {
		storage.close();
	}
	
	public String getPluginName() {
		return "Bastion";
	}
	
	private void registerListeners() {
		getServer().getPluginManager().registerEvents(new BastionDamageListener(), this);
		getServer().getPluginManager().registerEvents(new BastionInteractListener(), this);
		getServer().getPluginManager().registerEvents(new ElytraListener(), this);
		getServer().getPluginManager().registerEvents(new BastionBreakListener(storage), this);
	}

	private void setupDatabase() {
		ManagedDatasource db = (ManagedDatasource) getConfig().get("mysql");
		try {
			db.getConnection().close();
		} catch(Exception e) {
			warning("Could not connect to database, stopping bastion", e);
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		storage = new BastionBlockStorage(db, getLogger());
		storage.registerMigrations();
		storage.loadBastions();
	}
	
	//Sets up the command managers
	private void setupCommands(){
		getCommand("Bastion").setExecutor(new BastionCommandManager());
		getCommand("bsi").setExecutor(new ModeChangeCommand(Mode.INFO));
		getCommand("bsd").setExecutor(new ModeChangeCommand(Mode.DELETE));
		getCommand("bso").setExecutor(new ModeChangeCommand(Mode.NORMAL));
		getCommand("bsb").setExecutor(new ModeChangeCommand(Mode.BASTION));
		getCommand("bsf").setExecutor(new ModeChangeCommand(Mode.OFF));
		getCommand("bsm").setExecutor(new ModeChangeCommand(Mode.MATURE));
	}

	public static Bastion getPlugin() {
		return plugin;
	}
	
	public static BastionBlockManager getBastionManager() {
		return manager;
	}
	
	public static BastionBlockStorage getBastionStorage() {
		return storage;
	}
	
	private void registerNameLayerPermissions() {
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