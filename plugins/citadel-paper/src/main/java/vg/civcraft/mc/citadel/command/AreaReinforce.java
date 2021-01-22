package vg.civcraft.mc.citadel.command;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

@CivCommand(id = "ctar")
public class AreaReinforce extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Must be a player to perform this command.");
			return true;
		}
		Player p = (Player) sender;
		UUID uuid = NameAPI.getUUID(p.getName());
		ReinforcementType reinType = Citadel.getInstance().getReinforcementTypeManager()
				.getByItemStack(p.getInventory().getItemInMainHand());
		if (reinType == null) {
			CitadelUtility.sendAndLog(p, ChatColor.RED, "The item you are holding is not a possible reinforcement");
			return true;
		}
		String groupName = null;
		if (args.length == 6) {
			groupName = NameAPI.getGroupManager().getDefaultGroup(uuid);
			if (groupName == null) {
				CitadelUtility.sendAndLog(p, ChatColor.RED, "You need to set a default group \n Use /nlsdg to do so");
				return true;
			}
		} else {
			groupName = args[0];
		}
		Group group = GroupManager.getGroup(groupName);
		if (group == null) {
			CitadelUtility.sendAndLog(p, ChatColor.RED, "That group does not exist.");
			return true;
		}
		// no additional group permission check here because the player is
		// admin/op anyway
		int xMin, yMin, zMin, xMax, yMax, zMax;
		int offset = args.length == 7 ? 1 : 0;
		try {
			int x1 = Integer.parseInt(args[offset]);
			int y1 = Integer.parseInt(args[offset + 1]);
			int z1 = Integer.parseInt(args[offset + 2]);
			int x2 = Integer.parseInt(args[offset + 3]);
			int y2 = Integer.parseInt(args[offset + 4]);
			int z2 = Integer.parseInt(args[offset + 5]);
			xMin = Math.min(x1, x2);
			yMin = Math.min(y1, y2);
			zMin = Math.min(z1, z2);
			xMax = Math.max(x1, x2);
			yMax = Math.max(y1, y2);
			zMax = Math.max(z1, z2);
		} catch (NumberFormatException e) {
			CitadelUtility.sendAndLog(p, ChatColor.RED, "One of the arguments you provided was not a number");
			return false;
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
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new ArrayList<>();
	}
}
