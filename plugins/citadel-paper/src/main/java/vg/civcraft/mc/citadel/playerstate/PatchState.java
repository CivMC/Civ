package vg.civcraft.mc.citadel.playerstate;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import vg.civcraft.mc.citadel.CitadelPermissionHandler;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.command.PatchMode;
import vg.civcraft.mc.citadel.events.ReinforcementRepairEvent;
import vg.civcraft.mc.citadel.listener.ModeListener;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class PatchState extends AbstractPlayerState {

	public PatchState(Player p) {
		super(p);
	}

	@Override
	public String getName() {
		return "Patch mode";
	}

	@Override
	public void handleBlockPlace(BlockPlaceEvent e) {

	}

	@Override
	public void handleInteractBlock(PlayerInteractEvent e) {
		Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(e.getClickedBlock());
		Player player = e.getPlayer();
		if (rein == null) {
			CitadelUtility.sendAndLog(player, ChatColor.RED, "This block is not reinforced");
			return;
		}
		if (!rein.hasPermission(player, CitadelPermissionHandler.getRepair())) {
			CitadelUtility.sendAndLog(player, ChatColor.RED,
					"You do not have permission to repair reinforcements on this group");
			return;
		}
		if (rein.getHealth() >= rein.getType().getHealth()) {
			if (rein.hasPermission(player, CitadelPermissionHandler.getRepair())) {
				CitadelUtility.sendAndLog(player, ChatColor.GOLD,
						"Reinforcement is already at " + ModeListener.formatHealth(rein) + ChatColor.GOLD
								+ " health with " + ChatColor.AQUA + rein.getType().getName() + ChatColor.GOLD + " on "
								+ ChatColor.LIGHT_PURPLE + rein.getGroup().getName());
			} else {
				CitadelUtility.sendAndLog(player, ChatColor.GOLD, "Reinforcement is already at "
						+ ModeListener.formatHealth(rein) + ChatColor.GOLD + " health");
			}
			return;
		}
		ItemMap playerMap = new ItemMap(player.getInventory());
		if (playerMap.getAmount(rein.getType().getItem()) <= 0) {
			CitadelUtility.sendAndLog(player, ChatColor.RED, "You don't have the item required to repair " + ChatColor.AQUA
					+ rein.getType().getName() + ChatColor.GOLD + " reinforcements");
			return;
		}
		ReinforcementRepairEvent repairEvent = new ReinforcementRepairEvent(e.getPlayer(), rein);
		Bukkit.getPluginManager().callEvent(repairEvent);
		if (repairEvent.isCancelled()) {
			return;
		}
		if (!rein.rollForItemReturn()) {
			if (!CitadelUtility.consumeReinforcementItems(player, rein.getType(), false)) {
				return;
			}
		}
		rein.setHealth(rein.getType().getHealth());
		rein.resetCreationTime();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof PatchMode;
	}

	@Override
	public String getOverlayText() {
		return ChatColor.GREEN + "CTP";
	}

}
