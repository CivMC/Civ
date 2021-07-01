package vg.civcraft.mc.citadel.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelPermissionHandler;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.playerstate.AbstractPlayerState;
import vg.civcraft.mc.citadel.playerstate.FortificationState;
import vg.civcraft.mc.citadel.playerstate.PlayerStateManager;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

public class Fortification extends BaseCommand {

	@CommandAlias("ctf|reinforce|fortify")
	@Syntax("<group>")
	@Description("Enters fortification mode. All blocks placed in fortification mode will automatically be reinforced. If no group is given, your default group will be used")
	@CommandCompletion("@CT_Groups")
	public void execute(Player player, @Optional String targetGroup) {
		PlayerStateManager stateManager = Citadel.getInstance().getStateManager();
		AbstractPlayerState currentState = stateManager.getState(player);
		if (targetGroup == null && currentState instanceof FortificationState) {
			stateManager.setState(player, null);
			return;
		}
		ReinforcementType type = Citadel.getInstance().getReinforcementTypeManager()
				.getByItemStack(player.getInventory().getItemInMainHand());
		if (type == null) {
			CitadelUtility.sendAndLog(player, ChatColor.RED, "You can not reinforce with this item");
			stateManager.setState(player, null);
			return;
		}
		
		String groupName = null;
		if (targetGroup == null) {
			groupName = NameAPI.getGroupManager().getDefaultGroup(player.getUniqueId());
			if (groupName == null) {
				CitadelUtility.sendAndLog(player, ChatColor.RED,
						"You need to fortify to a group! Try /fortify groupname. \n Or use /create groupname if you don't have a group yet.");
				return;
			}
		} else {
			groupName = targetGroup;
		}
		
		Group group = GroupManager.getGroup(groupName);
		if (group == null) {
			CitadelUtility.sendAndLog(player, ChatColor.RED, "The group " + groupName + " does not exist.");
			stateManager.setState(player, null);
			return;
		}
		boolean hasAccess = NameAPI.getGroupManager().hasAccess(group.getName(), player.getUniqueId(),
				CitadelPermissionHandler.getReinforce());
		if (!hasAccess) {
			CitadelUtility.sendAndLog(player, ChatColor.RED, "You do not have permission to reinforce on " + group.getName());
			stateManager.setState(player, null);
			return;
		}
		if (currentState instanceof FortificationState) {
			FortificationState fortState = (FortificationState) currentState;
			if (fortState.getGroup() == group && fortState.getType() == type) {
				stateManager.setState(player, null);
				return;
			}
		}
		stateManager.setState(player, new FortificationState(player, type, group));
	}
}
