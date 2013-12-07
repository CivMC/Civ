package isaac.bastion;


import isaac.bastion.manager.BastionManager;
import isaac.bastion.manager.ConfigManager;

import com.untamedears.citadel.entity.PlayerReinforcement;
import com.untamedears.citadel.events.CreateReinforcementEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public final class BastionListener
implements Listener
{
	private BastionManager bastionManager;
	private ConfigManager config;

	public BastionListener()
	{
		bastionManager = Bastion.getBastionManager();
		config=Bastion.getConfigManager();
	}
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		bastionManager.handleBlockPlace(event);
	}
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(event.isCancelled()){
			return;
		}
		if (event.getBlock().getType() == config.getBastionBlockMaterial()) {
			Bastion.getPlugin().getLogger().info("Block break "+event.getBlock().toString());
			bastionManager.removeBastion(event.getBlock().getLocation());
		}
	}
	@EventHandler
	public void onReinforcement(CreateReinforcementEvent event) {

		if (event.getBlock().getType() == config.getBastionBlockMaterial()) {
			bastionManager.addBastion(event.getBlock().getLocation(),(PlayerReinforcement) event.getReinforcement());
		}
	}
	public BastionManager getBastionManager(){
		return bastionManager;
	
	}
}