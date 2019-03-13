package vg.civcraft.mc.citadel.playerstate;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.citadel.events.ReinforcementChangeTypeEvent;
import vg.civcraft.mc.citadel.events.ReinforcementGroupChangeEvent;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class ReinforcingState extends IPlayerState {

	private Group group;

	public ReinforcingState(Player p, boolean bypass, Group group) {
		super(p, bypass);
		this.group = group;
	}

	@Override
	public void handleBlockPlace(BlockPlaceEvent e) {
	}

	@Override
	public void handleInteractBlock(PlayerInteractEvent e) {
		// always cancel
		e.setCancelled(true);
		// does group still exist?
		if (!group.isValid()) {
			Utility.sendAndLog(e.getPlayer(), ChatColor.RED,
					"The group " + group.getName() + " seems to have been deleted in the mean time");
			Citadel.getInstance().getStateManager().setState(e.getPlayer(), null);
			return;
		}
		Player player = e.getPlayer();
		// does the player have an item?
		if (e.getItem() == null) {
			Utility.sendAndLog(player, ChatColor.RED, "You have nothing in your hand to reinforce with");
			return;
		}
		ReinforcementType type = Citadel.getInstance().getReinforcementTypeManager().getByItemStack(e.getItem());
		// is it a valid item to reinforce with
		if (type == null) {
			Utility.sendAndLog(player, ChatColor.RED, "You can not reinforce with this item");
			return;
		}
		// can the item reinforce the clicked block
		if (!type.canBeReinforced(e.getClickedBlock().getType())) {
			Utility.sendAndLog(player, ChatColor.RED,
					type.getName() + " can not reinforce " + e.getClickedBlock().getType());
			return;
		}
		// does the player have permission to reinforce on that group
		if (!NameAPI.getGroupManager().hasAccess(group, e.getPlayer().getUniqueId(),
				PermissionType.getPermission(Citadel.reinforcePerm))) {
			Utility.sendAndLog(e.getPlayer(), ChatColor.RED,
					"You seem to have lost permission to reinforce on " + group.getName());
			Citadel.getInstance().getStateManager().setState(e.getPlayer(), null);
			return;
		}
		Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(e.getClickedBlock());
		// if reinforcement exists, check if player has permission to edit it
		if (rein != null) {
			if (!rein.hasPermission(e.getPlayer(), Citadel.bypassPerm)) {
				Utility.sendAndLog(e.getPlayer(), ChatColor.RED,
						"You do not have permission to bypass reinforcements on " + group.getName());
				return;
			}
		}
		// check inventory for reinforcement item
		ItemMap toConsume = new ItemMap(type.getItem());
		if (!toConsume.isContainedIn(player.getInventory())) {
			Utility.sendAndLog(e.getPlayer(), ChatColor.RED, "No reinforcing item found in your inventory?");
			return;
		}
		// consume item from inventory
		if (!toConsume.removeSafelyFrom(player.getInventory())) {
			Utility.sendAndLog(e.getPlayer(), ChatColor.RED, "Failed to remove reinforcement item from your inventory");
			return;
		}

		if (rein == null) {
			// just create new reinforcement
			ReinforcementLogic.createReinforcement(e.getClickedBlock(), type, group);
		} else {
			// replace existing one
			boolean changedGroup = false;
			if (group.getGroupId() != rein.getGroup().getGroupId()) {
				// switch group
				ReinforcementGroupChangeEvent rgce = new ReinforcementGroupChangeEvent(player, rein, group);
				Bukkit.getPluginManager().callEvent(rgce);
				if (!rgce.isCancelled()) {
					rein.setGroup(group);
					changedGroup = true;
				}
				// informing the user is up to whoever cancelled it
			}
			if (type != rein.getType()) {
				// switch type
				ReinforcementChangeTypeEvent rcte = new ReinforcementChangeTypeEvent(player, rein, type);
				Bukkit.getPluginManager().callEvent(rcte);
				if (!rcte.isCancelled()) {
					rein.setType(type);
					Utility.sendAndLog(player, ChatColor.GREEN,
							"Updated reinforcement to " + rein.getType().getName() + " on " + group.getName());
				} else if (changedGroup) {
					Utility.sendAndLog(player, ChatColor.GREEN, "Updated group to " + group.getName());
				}
			}
		}
	}
	
	public Group getGroup() {
		return group;
	}

}
