package vg.civcraft.mc.citadel.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelPermissionHandler;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.playerstate.AbstractPlayerState;
import vg.civcraft.mc.citadel.playerstate.AdvancedFortificationState;
import vg.civcraft.mc.citadel.playerstate.PlayerStateManager;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;

@CivCommand(id = "cta")
public class AdvancedFortification extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		PlayerStateManager stateManager = Citadel.getInstance().getStateManager();
		AbstractPlayerState currentState = stateManager.getState(player);
		AdvancedFortificationState advFortState = null;
		if (currentState instanceof AdvancedFortificationState) {
			advFortState = (AdvancedFortificationState) currentState;
			if (args.length == 0) {
				stateManager.setState(player, null);
				return true;
			}
		}
		ItemStack mainHand = player.getInventory().getItemInMainHand();
		if (mainHand.getType() == Material.AIR) {
			CitadelUtility.sendAndLog(player, ChatColor.RED,
					"You need to hold an item in your main hand to specify the block type to reinforce");
			return true;
		}
		ItemStack offHand = player.getInventory().getItemInOffHand();
		if (offHand.getType() == Material.AIR) {
			CitadelUtility.sendAndLog(player, ChatColor.RED, "You need to hold a reinforcement item in your off hand");
			return true;
		}
		ReinforcementType type = Citadel.getInstance().getReinforcementTypeManager().getByItemStack(offHand);
		if (type == null) {
			CitadelUtility.sendAndLog(player, ChatColor.RED, "You can not reinforce with the item in your off hand");
			return true;
		}
		if (!type.canBeReinforced(mainHand.getType())) {
			CitadelUtility.sendAndLog(player, ChatColor.AQUA,
					type.getName() + ChatColor.RED + " can not reinforce " + mainHand.getType().name());
			return true;
		}
		String groupName = null;
		if (args.length == 0) {
			groupName = NameAPI.getGroupManager().getDefaultGroup(player.getUniqueId());
			if (groupName == null) {
				CitadelUtility.sendAndLog(player, ChatColor.RED,
						"You don't have a default group and can thus not use this command without specifying a group");
				return true;
			}
		} else {
			groupName = args[0];
		}

		Group group = GroupManager.getGroup(groupName);
		if (group == null) {
			CitadelUtility.sendAndLog(player, ChatColor.RED, "The group " + groupName + " does not exist.");
			return true;
		}
		boolean hasAccess = NameAPI.getGroupManager().hasAccess(group.getName(), player.getUniqueId(),
				CitadelPermissionHandler.getReinforce());
		if (!hasAccess) {
			CitadelUtility.sendAndLog(player, ChatColor.RED, "You do not have permission to reinforce on " + group.getName());
			return true;
		}
		if (advFortState == null) {
			advFortState =  new AdvancedFortificationState(player);
			stateManager.setState(player, advFortState);
		}
		advFortState.addSetup(mainHand, type, group);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
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
