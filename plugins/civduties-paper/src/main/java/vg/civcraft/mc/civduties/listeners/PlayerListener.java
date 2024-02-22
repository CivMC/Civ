package vg.civcraft.mc.civduties.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import vg.civcraft.mc.civduties.CivDuties;
import vg.civcraft.mc.civduties.ModeManager;
import vg.civcraft.mc.civduties.configuration.Command;
import vg.civcraft.mc.civduties.configuration.Command.Timing;
import vg.civcraft.mc.civduties.configuration.DutiesConfigManager;
import vg.civcraft.mc.civduties.configuration.Tier;
import vg.civcraft.mc.civduties.database.DatabaseManager;
import vg.civcraft.mc.civduties.external.VaultManager;

public class PlayerListener implements Listener {
	private ModeManager modeManager;
	private DatabaseManager db;
	private DutiesConfigManager config;
	private VaultManager vaultManager;
	
	public PlayerListener() {
		this.modeManager = CivDuties.getInstance().getModeManager();
		this.db = CivDuties.getInstance().getDatabaseManager();
		this.config = CivDuties.getInstance().getConfigManager();
		this.vaultManager = CivDuties.getInstance().getVaultManager();
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void playerQuitEvent(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if(modeManager.isInDuty(player)){
			Tier tier = config.getTier(db.getPlayerData(player.getUniqueId()).getTierName());
			vaultManager.removePermissionsFromPlayer(player, tier.getTemporaryPermissions());
			vaultManager.removePlayerFromGroups(player, tier.getTemporaryGroups());
			for(Command command: tier.getCommands()){
				if(command.getTiming() == Timing.LOGOUT){
					command.execute(player);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerJoinEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer(); 
		if(modeManager.isInDuty(player)){
			Tier tier = config.getTier(db.getPlayerData(player.getUniqueId()).getTierName());
			for(Command command: tier.getCommands()){
				if(command.getTiming() == Timing.LOGIN){
					command.execute(player);
				}
			}
			vaultManager.addPermissionsToPlayer(player, tier.getTemporaryPermissions());
			vaultManager.addPlayerToGroups(player, tier.getTemporaryGroups());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity() instanceof Player && modeManager.isInDuty(((Player) event.getEntity()))) {
			Player player = (Player) event.getEntity();
			Tier tier = config.getTier(db.getPlayerData(player.getUniqueId()).getTierName());
			if(tier.isDeathDrops()){
				event.getDrops().clear();
				event.setDroppedExp(0);
			}
		}
	}
}
