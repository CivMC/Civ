package isaac.bastion;

import com.untamedears.citadel.entity.PlayerReinforcement;
import com.untamedears.citadel.events.CreateReinforcementEvent;

import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public final class BastionListener
implements Listener
{
	public Logger test;
	private BastionManager manager;

	public BastionListener()
	{
		manager = new BastionManager();
	}
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		manager.handleBlockPlace(event);
	}
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(event.isCancelled())
			return;
		if (event.getBlock().getType() == Material.GOLD_BLOCK) {
			manager.removeBastion(event.getBlock().getLocation());
		}
	}
	@EventHandler
	public void onReinforcement(CreateReinforcementEvent event) {

		if (event.getBlock().getType() == Material.GOLD_BLOCK) {
			PlayerReinforcement reinforcement = (PlayerReinforcement) event.getReinforcement();
			manager.addBastion(event.getBlock().getLocation(), reinforcement.getDurability(), reinforcement.getOwner());
		}
	}
}