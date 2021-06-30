package vg.civcraft.mc.citadel.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelPermissionHandler;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.playerstate.AbstractPlayerState;
import vg.civcraft.mc.citadel.playerstate.PlayerStateManager;
import vg.civcraft.mc.citadel.playerstate.ReinforcingState;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;

@CommandAlias("ctr")
public class Reinforce extends BaseCommand {

	@Syntax("/ctr <group>")
	@Description("Enters reinforcing mode. Interacting with blocks in reinforcing mode will reinforce them or modify their reinforcement")
	public void execute(CommandSender sender, @Optional String targetGroup) {
		Player player = (Player) sender;
		UUID uuid = NameAPI.getUUID(player.getName());
		String groupName = null;
		if (targetGroup == null) {
			groupName = NameAPI.getGroupManager().getDefaultGroup(uuid);
			if (groupName == null) {
				CitadelUtility.sendAndLog(player, ChatColor.RED,
						"You need to reinforce to a group! Try /reinforce groupname. \n Or use /create groupname if you don't have a group yet.");
				return;
			}
		} else {
			groupName = targetGroup;
		}
		PlayerStateManager stateManager = Citadel.getInstance().getStateManager();
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
		AbstractPlayerState currentState = Citadel.getInstance().getStateManager().getState(player);
		if (currentState instanceof ReinforcingState) {
			ReinforcingState reinState = (ReinforcingState) currentState;
			if (reinState.getGroup() == group) {
				stateManager.setState(player, null);
				return;
			}
		}
		stateManager.setState(player, new ReinforcingState(player, group));
	}

	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player))
			return null;

		if (args.length == 0)
			return GroupTabCompleter.complete(null, CitadelPermissionHandler.getReinforce(),
					(Player) sender);
		else if (args.length == 1)
			return GroupTabCompleter.complete(args[0], CitadelPermissionHandler.getReinforce(),
					(Player) sender);
		else {
			return new ArrayList<>();
		}
	}
}
