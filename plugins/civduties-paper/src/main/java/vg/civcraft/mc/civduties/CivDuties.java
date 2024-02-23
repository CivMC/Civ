package vg.civcraft.mc.civduties;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civduties.command.CivDutiesCommandHandler;
import vg.civcraft.mc.civduties.configuration.Command;
import vg.civcraft.mc.civduties.configuration.Command.Timing;
import vg.civcraft.mc.civduties.configuration.DutiesConfigManager;
import vg.civcraft.mc.civduties.configuration.Tier;
import vg.civcraft.mc.civduties.database.DatabaseManager;
import vg.civcraft.mc.civduties.external.CombatTagHandler;
import vg.civcraft.mc.civduties.external.VaultManager;
import vg.civcraft.mc.civduties.listeners.PlayerListener;
import vg.civcraft.mc.civmodcore.ACivMod;

public class CivDuties extends ACivMod {
	private static CivDuties pluginInstance;
	private DutiesConfigManager config;
	private DatabaseManager db;
	private ModeManager modeManager;
	private VaultManager vaultManager;
	private CivDutiesCommandHandler commandHandler;

	public CivDuties() {
		pluginInstance = this;
	}

	@Override
	public void onEnable() {
		super.onEnable();
		config = new DutiesConfigManager(this);
		config.parse();
		if (config.getDatabase() == null) {
			getLogger().severe("Invalid database credentials, shutting down");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		db = new DatabaseManager(config.getDatabase());
		vaultManager = new VaultManager();
		modeManager = new ModeManager();
		commandHandler = new CivDutiesCommandHandler(this);
		registerListeners();
	}

	@Override
	public void onDisable() {
		for(Player player : Bukkit.getOnlinePlayers()){
			if(modeManager.isInDuty(player)){
				Tier tier = config.getTier(db.getPlayerData(player.getUniqueId()).getTierName());
				for(Command command: tier.getCommands()){
					if(command.getTiming() == Timing.LOGOUT){
						command.execute(player);
					}
				}
				vaultManager.addPermissionsToPlayer(player, tier.getTemporaryPermissions());
				vaultManager.addPlayerToGroups(player, tier.getTemporaryGroups());
			}
		}
	}

	public static CivDuties getInstance() {
		return pluginInstance;
	}

	public DutiesConfigManager getConfigManager() {
		return config;
	}

	public DatabaseManager getDatabaseManager() {
		return db;
	}

	public ModeManager getModeManager() {
		return modeManager;
	}

	public VaultManager getVaultManager() {
		return vaultManager;
	}
	
	public boolean isVaultEnabled() {
		return Bukkit.getPluginManager().isPluginEnabled("Vault");
	}

	public boolean isCombatTagPlusEnabled() {
		return Bukkit.getPluginManager().isPluginEnabled("CombatTagPlus");
	}


	private void registerListeners() {
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		if (isCombatTagPlusEnabled()) {
			getServer().getPluginManager().registerEvents(new CombatTagHandler(), this);
		}
	}
}
