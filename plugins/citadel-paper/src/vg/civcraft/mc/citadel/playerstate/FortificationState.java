package vg.civcraft.mc.citadel.playerstate;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class FortificationState extends IPlayerState {

	private ReinforcementType type;

	public FortificationState(Player p, boolean bypass, ReinforcementType type) {
		super(p, bypass);
		this.type = type;
	}

	@Override
	public void handleBlockPlace(BlockPlaceEvent e) {
		// check if reinforcement already exists
		Reinforcement rein = Citadel.getInstance().getReinforcementManager().getReinforcement(e.getBlock());
		if (rein != null) {
			// something like a slab, we just ignore this
			return;
		}
		Player player = e.getPlayer();
		if (!type.canBeReinforced(e.getBlock().getType())) {
			e.setCancelled(true);
			Utility.sendAndLog(player, ChatColor.RED, type.getName() + " can not reinforce " + e.getBlock().getType());
			return;
		}

		ItemMap playerItems = new ItemMap(player.getInventory());
		int available = playerItems.getAmount(type.getItem());
		if (available == 0) {
			// TODO leave mode
			e.setCancelled(true);
			Utility.sendAndLog(player, ChatColor.RED, "You have no items left to reinforce with " + type.getName());
			return;
		}
		ItemMap toRemove = new ItemMap(type.getItem());
		if (toRemove.removeSafelyFrom(player.getInventory())) {
			Utility.sendAndLog(player, ChatColor.RED,
					"Failed to remove items needed for " + type.getName() + " reinforcement from your inventory");
			// TODO exit mode
			return;
		}
	}

	@Override
	public void handleInteractBlock(PlayerInteractEvent e) {
	}

	@Override
	public void handleBreakBlock(BlockBreakEvent e) {
	}

}
