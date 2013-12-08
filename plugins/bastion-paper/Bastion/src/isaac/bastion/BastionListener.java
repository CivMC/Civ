package isaac.bastion;


import com.untamedears.citadel.entity.PlayerReinforcement;
import com.untamedears.citadel.events.CreateReinforcementEvent;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public final class BastionListener
implements Listener
{
	private BastionManager bastionManager;

	public BastionListener()
	{
		bastionManager = Bastion.getBastionManager();
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
		if (event.getBlock().getType() == Material.GOLD_BLOCK) {
			Bastion.getPlugin().getLogger().info("Block break "+event.getBlock().toString());
			bastionManager.removeBastion(event.getBlock().getLocation());
		}
	}
	@EventHandler
	public void onReinforcement(CreateReinforcementEvent event) {

		if (event.getBlock().getType() == Material.GOLD_BLOCK) {
			bastionManager.addBastion(event.getBlock().getLocation(),(PlayerReinforcement) event.getReinforcement());
		}
	}
	public BastionManager getBastionManager(){
		return bastionManager;
	
	}
}