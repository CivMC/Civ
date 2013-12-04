package bastion.isaac;

import com.untamedears.citadel.entity.PlayerReinforcement;
import com.untamedears.citadel.events.CreateReinforcementEvent;

import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
		Bastion.getPlugin().getLogger().info("block placed");
		manager.handleBlockPlace(event);
	}
	@EventHandler
	public void onReinforcement(CreateReinforcementEvent event) {
		Bastion.getPlugin().getLogger().info("block reinforced");

		if (event.getBlock().getType() == Material.GOLD_BLOCK) {
			PlayerReinforcement reinforcement = (PlayerReinforcement) event.getReinforcement();
			manager.addBastion(event.getBlock().getLocation(), reinforcement.getDurability(), reinforcement.getOwner());
		}
	}
}