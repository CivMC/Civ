package vg.civcraft.mc.citadel.playerstate;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class PatchState extends AbstractPlayerState {

	public PatchState(Player p, boolean bypass) {
		super(p, bypass);
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
			Utility.sendAndLog(player, ChatColor.RED, "This block is not reinforced");
			return;
		}
		if (!rein.hasPermission(player, Citadel.repairPerm)) {
			Utility.sendAndLog(player, ChatColor.RED,
					"You do not have permission to repair reinforcements on this group");
			return;
		}
		if (rein.getHealth() >= rein.getType().getHealth()) {
			if (rein.hasPermission(player, Citadel.infoPerm)) {
				Utility.sendAndLog(player, ChatColor.GOLD,
						"Reinforcement is already at " + InformationState.formatHealth(rein) + ChatColor.GOLD
								+ " health with " + ChatColor.AQUA + rein.getType().getName() + ChatColor.GOLD + " on "
								+ ChatColor.LIGHT_PURPLE + rein.getGroup().getName());
			} else {
				Utility.sendAndLog(player, ChatColor.GOLD, "Reinforcement is already at "
						+ InformationState.formatHealth(rein) + ChatColor.GOLD + " health");
			}
			return;
		}
		ItemMap playerMap = new ItemMap(player.getInventory());
		if (playerMap.getAmount(rein.getType().getItem()) <= 0) {
			Utility.sendAndLog(player, ChatColor.RED, "You don't have the item required to repair " + ChatColor.AQUA
					+ rein.getType().getName() + ChatColor.GOLD + " reinforcements");
			return;
		}
		if (!rein.rollForItemReturn()) {
			if (!Utility.consumeReinforcementItems(player, rein.getType())) {
				return;
			}
		}
		if (Citadel.getInstance().getConfigManager().logCreation()) {
			Citadel.getInstance().getLogger()
					.info(player.getName() + " recreated reinforcement with " + rein.getType().getName() + " for "
							+ e.getClickedBlock().getType().toString() + " at "
							+ e.getClickedBlock().getLocation().toString() + " via repair");
		}
		rein.setHealth(-1);
		ReinforcementLogic.createReinforcement(rein.getLocation().getBlock(), rein.getType(), rein.getGroup());
	}

}
