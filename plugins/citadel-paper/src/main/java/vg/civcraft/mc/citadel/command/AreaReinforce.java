package vg.civcraft.mc.citadel.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

public class AreaReinforce extends BaseCommand {

	@CommandAlias("ctar")
	@Syntax("<lowX> <lowY> <lowZ> <highX> <highY> <highZ> <group>")
	@Description("Using the reinforcement item in your main hand, reinforces an area to your default or a target group.")
	@CommandCompletion("@nothing @nothing @nothing @nothing @nothing @nothing @CT_Groups")
	public void execute(Player p, String minX, String minY, String minZ, String maxX, String maxY, String maxZ, @Optional String targetGroup) {
		UUID uuid = NameAPI.getUUID(p.getName());
		ReinforcementType reinType = Citadel.getInstance().getReinforcementTypeManager()
				.getByItemStack(p.getInventory().getItemInMainHand());
		if (reinType == null) {
			CitadelUtility.sendAndLog(p, ChatColor.RED, "The item you are holding is not a possible reinforcement");
			return;
		}
		String groupName = null;
		if (targetGroup == null) {
			groupName = NameAPI.getGroupManager().getDefaultGroup(uuid);
			if (groupName == null) {
				CitadelUtility.sendAndLog(p, ChatColor.RED, "You need to set a default group \n Use /nlsdg to do so");
				return;
			}
		} else {
			groupName = targetGroup;
		}
		Group group = GroupManager.getGroup(groupName);
		if (group == null) {
			CitadelUtility.sendAndLog(p, ChatColor.RED, "That group does not exist.");
			return;
		}
		// no additional group permission check here because the player is
		// admin/op anyway
		int xMin, yMin, zMin, xMax, yMax, zMax;
		try {
			int x1 = Integer.parseInt(minX);
			int y1 = Integer.parseInt(minY);
			int z1 = Integer.parseInt(minZ);
			int x2 = Integer.parseInt(maxX);
			int y2 = Integer.parseInt(maxY);
			int z2 = Integer.parseInt(maxZ);
			xMin = Math.min(x1, x2);
			yMin = Math.min(y1, y2);
			zMin = Math.min(z1, z2);
			xMax = Math.max(x1, x2);
			yMax = Math.max(y1, y2);
			zMax = Math.max(z1, z2);
		} catch (NumberFormatException e) {
			CitadelUtility.sendAndLog(p, ChatColor.RED, "One of the arguments you provided was not a number");
			return;
		}
		int count = 0;
		for (int x = xMin; x <= xMax; x++) {
			for (int y = yMin; y <= yMax; y++) {
				for (int z = zMin; z <= zMax; z++) {
					Block current = p.getWorld().getBlockAt(x, y, z);
					if (!reinType.canBeReinforced(current.getType())) {
						continue;
					}
					Block protecting = ReinforcementLogic.getResponsibleBlock(current);
					if (!current.getLocation().equals(protecting.getLocation())) {
						continue;
					}
					count++;
					Reinforcement rein = ReinforcementLogic.callReinforcementCreationEvent(p, current, reinType, group);
					if (rein != null) {
						ReinforcementLogic.createReinforcement(rein);
					}
				}
			}
		}

		CitadelUtility.sendAndLog(p, ChatColor.GREEN, "Successfully created " + count + "reinforcements");
	}
}
