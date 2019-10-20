package com.devotedmc.ExilePearl.command;

import org.bukkit.entity.Player;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.broadcast.NLGroupBroadcastListener;

import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class CmdPearlBroadcast extends PearlCommand {

	public CmdPearlBroadcast(ExilePearlApi pearlApi) {
		super(pearlApi);
		this.aliases.add("broadcast");

		if (plugin.isNameLayerEnabled()) {
			this.commandArgs.add(required("group/player", autoTab("group_or_player", "No matching group/player found.")));
			this.setHelpShort("Broadcasts your pearl location to a group or another player.");
		} else {
			this.commandArgs.add(requiredPlayer("player"));
			this.setHelpShort("Broadcasts your pearl location to another player.");
		}

		this.senderMustBePlayer = true;
	}

	@Override
	public void perform() {
		ExilePearl pearl = plugin.getPearl(player().getUniqueId());

		if (pearl == null) {
			msg(Lang.pearlNotExiled);
			return;
		}

		// First check for a group
		if (plugin.isNameLayerEnabled()) {
			GroupManager gm = NameAPI.getGroupManager();
			// First look for a matching group
			Group g = GroupManager.getGroup(argAsString(0));

			if (g != null) {
				if (!gm.hasAccess(g, player().getUniqueId(), PermissionType.getPermission("WRITE_CHAT"))) {
					msg(Lang.groupNoChatPermission);
				}else{
					//If they are already broadcasting to group then remove the listener for that group
					if (pearl.isBroadcastingTo(g)) {
						pearl.removeBroadcastListener(new NLGroupBroadcastListener(g));
						msg(Lang.groupStoppedBcasting, g.getName());
						return;
					}

					// Ok the group exists and the player has permission. Create the listener
					pearl.addBroadcastListener(new NLGroupBroadcastListener(g));
					msg(Lang.groupNowBcasting, g.getName());
					return;
				}
			}
		}

		// No group found, try to find a player
		Player player = plugin.getPlayer(argAsString(0));
		if (player == null) {
			msg(Lang.pearlNoPlayer);
			return;
		}

		if (player.equals(player())) {
			msg(Lang.pearlCantBcastSelf);
			return;
		}

		if (pearl.isBroadcastingTo(player.getUniqueId())) {
			msg(Lang.pearlAlreadyBcasting);
			return;
		}

		plugin.getPearlManager().addBroadcastRequest(player, pearl);

		msg(player, Lang.pearlBcastRequest, player().getName());
		msg(Lang.pearlBcastRequestSent);
	}
}
