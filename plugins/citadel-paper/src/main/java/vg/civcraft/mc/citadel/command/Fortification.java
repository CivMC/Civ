package vg.civcraft.mc.citadel.command;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.citadel.playerstate.AbstractPlayerState;
import vg.civcraft.mc.citadel.playerstate.FortificationState;
import vg.civcraft.mc.citadel.playerstate.PlayerStateManager;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

@CivCommand(id = "ctf")
public class Fortification extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		UUID uuid = NameAPI.getUUID(player.getName());
		String groupName = null;
		if (args.length == 0) {
			groupName = NameAPI.getGroupManager().getDefaultGroup(uuid);
			if (groupName == null) {
				Utility.sendAndLog(player, ChatColor.RED,
						"You need to fortify to a group! Try /fortify groupname. \n Or use /create groupname if you don't have a group yet.");
				return true;
			}
		} else {
			groupName = args[0];
		}
		PlayerStateManager stateManager = Citadel.getInstance().getStateManager();
		Group group = GroupManager.getGroup(groupName);
		if (group == null) {
			Utility.sendAndLog(player, ChatColor.RED, "The group" + groupName + "does not exist.");
			stateManager.setState(player, null);
			return true;
		}
		ReinforcementType type = Citadel.getInstance().getReinforcementTypeManager()
				.getByItemStack(player.getInventory().getItemInMainHand());
		if (type == null) {
			Utility.sendAndLog(player, ChatColor.RED, "You can not reinforce with this item");
			stateManager.setState(player, null);
			return true;
		}
		boolean hasAccess = NameAPI.getGroupManager().hasAccess(group.getName(), player.getUniqueId(),
				PermissionType.getPermission(Citadel.reinforcePerm));
		if (!hasAccess) {
			Utility.sendAndLog(player, ChatColor.RED, "You do not have permission to reinforce on " + group.getName());
			stateManager.setState(player, null);
			return true;
		}
		AbstractPlayerState currentState = Citadel.getInstance().getStateManager().getState(player);
		if (currentState instanceof FortificationState) {
			FortificationState fortState = (FortificationState) currentState;
			if (fortState.getGroup() == group && fortState.getType() == type) {
				stateManager.setState(player, null);
				return true;
			}
		}
		stateManager.setState(player, new FortificationState(player, currentState.isBypassEnabled(), type, group));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (args.length == 0)
			return GroupTabCompleter.complete(null, PermissionType.getPermission(Citadel.reinforcePerm),
					(Player) sender);
		else if (args.length == 1)
			return GroupTabCompleter.complete(args[0], PermissionType.getPermission(Citadel.reinforcePerm),
					(Player) sender);
		else {
			return new ArrayList<String>();
		}
	}
}
