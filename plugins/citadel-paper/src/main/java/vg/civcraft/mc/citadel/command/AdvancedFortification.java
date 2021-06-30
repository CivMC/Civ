package vg.civcraft.mc.citadel.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
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
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;

@CommandAlias("cta")
public class AdvancedFortification extends BaseCommand {

	@Syntax("/cta <group>")
	@Description("Enters advanced fortification mode or adds configurations to it. Advanced configuration mode allows you to place on different groups with different reinforcement types at once")
	public void execute(CommandSender sender, @Optional String targetGroup) {
		Player player = (Player) sender;
		PlayerStateManager stateManager = Citadel.getInstance().getStateManager();
		AbstractPlayerState currentState = stateManager.getState(player);
		AdvancedFortificationState advFortState = null;
		if (currentState instanceof AdvancedFortificationState) {
			advFortState = (AdvancedFortificationState) currentState;
			if (targetGroup.isEmpty()) {
				stateManager.setState(player, null);
				return;
			}
		}
		ItemStack mainHand = player.getInventory().getItemInMainHand();
		if (mainHand.getType() == Material.AIR) {
			CitadelUtility.sendAndLog(player, ChatColor.RED,
					"You need to hold an item in your main hand to specify the block type to reinforce");
			return;
		}
		ItemStack offHand = player.getInventory().getItemInOffHand();
		if (offHand.getType() == Material.AIR) {
			CitadelUtility.sendAndLog(player, ChatColor.RED, "You need to hold a reinforcement item in your off hand");
			return;
		}
		ReinforcementType type = Citadel.getInstance().getReinforcementTypeManager().getByItemStack(offHand);
		if (type == null) {
			CitadelUtility.sendAndLog(player, ChatColor.RED, "You can not reinforce with the item in your off hand");
			return;
		}
		if (!type.canBeReinforced(mainHand.getType())) {
			CitadelUtility.sendAndLog(player, ChatColor.AQUA,
					type.getName() + ChatColor.RED + " can not reinforce " + mainHand.getType().name());
			return;
		}
		String groupName = null;
		if (targetGroup.isEmpty()) {
			groupName = NameAPI.getGroupManager().getDefaultGroup(player.getUniqueId());
			if (groupName == null) {
				CitadelUtility.sendAndLog(player, ChatColor.RED,
						"You don't have a default group and can thus not use this command without specifying a group");
				return;
			}
		} else {
			groupName = targetGroup;
		}

		Group group = GroupManager.getGroup(groupName);
		if (group == null) {
			CitadelUtility.sendAndLog(player, ChatColor.RED, "The group " + groupName + " does not exist.");
			return;
		}
		boolean hasAccess = NameAPI.getGroupManager().hasAccess(group.getName(), player.getUniqueId(),
				CitadelPermissionHandler.getReinforce());
		if (!hasAccess) {
			CitadelUtility.sendAndLog(player, ChatColor.RED, "You do not have permission to reinforce on " + group.getName());
			return;
		}
		if (advFortState == null) {
			advFortState =  new AdvancedFortificationState(player);
			stateManager.setState(player, advFortState);
		}
		advFortState.addSetup(mainHand, type, group);
	}

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
