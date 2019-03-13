package vg.civcraft.mc.citadel.playerstate;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class FortificationState extends IPlayerState {

	private ReinforcementType type;
	private Group group;

	public FortificationState(Player p, boolean bypass, ReinforcementType type, Group group) {
		super(p, bypass);
		this.type = type;
		this.group = group;
	}

	@Override
	public void handleBlockPlace(BlockPlaceEvent e) {
		//check if group still exists
		if (!group.isValid()) {
			Utility.sendAndLog(e.getPlayer(), ChatColor.RED, "The group " + group.getName() + " seems to have been deleted in the mean time");
			Citadel.getInstance().getStateManager().setState(e.getPlayer(), null);
			return;
		}
		//check if player still has permission
		if (!NameAPI.getGroupManager().hasAccess(group, e.getPlayer().getUniqueId(), PermissionType.getPermission(Citadel.reinforcePerm))) {
			Utility.sendAndLog(e.getPlayer(), ChatColor.RED, "You seem to have lost permission to reinforce on " + group.getName());
			Citadel.getInstance().getStateManager().setState(e.getPlayer(), null);
			return;
		}
		// check if reinforcement already exists
		Reinforcement rein = Citadel.getInstance().getReinforcementManager().getReinforcement(e.getBlock());
		if (rein != null) {
			// something like a slab, we just ignore this
			return;
		}
		Player player = e.getPlayer();
		//check if reinforcement can reinforce that block
		if (!type.canBeReinforced(e.getBlock().getType())) {
			e.setCancelled(true);
			Utility.sendAndLog(player, ChatColor.RED, type.getName() + " can not reinforce " + e.getBlock().getType());
			return;
		}
		ItemMap playerItems = new ItemMap(player.getInventory());
		//check inventory
		int available = playerItems.getAmount(type.getItem());
		if (available == 0) {
			Citadel.getInstance().getStateManager().setState(player, null);
			e.setCancelled(true);
			Utility.sendAndLog(player, ChatColor.RED, "You have no items left to reinforce with " + type.getName());
			return;
		}
		//remove from inventory
		ItemMap toRemove = new ItemMap(type.getItem());
		if (toRemove.removeSafelyFrom(player.getInventory())) {
			Utility.sendAndLog(player, ChatColor.RED,
					"Failed to remove items needed for " + type.getName() + " reinforcement from your inventory");
			Citadel.getInstance().getStateManager().setState(player, null);
			return;
		}
		//create reinforcement
		ReinforcementLogic.createReinforcement(e.getBlock(), type, group);
	}

	@Override
	public void handleInteractBlock(PlayerInteractEvent e) {
	}
	
	public Group getGroup() {
		return group;
	}
	
	public ReinforcementType getType() {
		return type;
	}
}
