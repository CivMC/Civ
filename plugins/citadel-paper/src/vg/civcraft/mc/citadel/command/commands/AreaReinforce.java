package vg.civcraft.mc.citadel.command.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.lang.Integer;
import java.lang.Math;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public class AreaReinforce extends PlayerCommand {
	private ReinforcementManager rm = Citadel.getReinforcementManager();
	private GroupManager gm = NameAPI.getGroupManager();

	public AreaReinforce(String name) {
		super(name);
		setIdentifier("ctar");
		setDescription("Allows you to reinforce entire areas");
		setUsage("/ctar <group> <x1> <y1> <z1> <x2> <y2> <z2>");
		setArguments(6, 7);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Must be a player to perform this command.");
			return true;
		}
		Player p = (Player) sender;
		UUID uuid = NameAPI.getUUID(p.getName());
		if (!p.isOp() && !p.hasPermission("citadel.admin")) {
			// This should never actually happen thanks to the plugin.yml, but
			// we just want to be sure
			Utility.sendAndLog(p, ChatColor.RED, "Nice try");
			return true;
		}

		ReinforcementType rt = ReinforcementType.getReinforcementType(p.getInventory().getItemInMainHand());

		if (rt == null) {
			Utility.sendAndLog(p, ChatColor.RED, "The item you are holding is not a possible reinforcement");
			return true;
		}
		String groupName = null;
		if (args.length == 6) {
			groupName = gm.getDefaultGroup(uuid);
			if (groupName == null) {
				Utility.sendAndLog(p, ChatColor.RED, "You need to set a default group \n Use /nlsdg to do so");
				return true;
			}
		} else {
			groupName = args[0];
		}
		Group g = GroupManager.getGroup(groupName);
		if (g == null) {
			Utility.sendAndLog(p, ChatColor.RED, "That group does not exist.");
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
			Utility.sendAndLog(p, ChatColor.RED, "One of the arguments you provided was not a number");
			return false;
		}
		for (int x = xMin; x <= xMax; x++) {
			for (int y = yMin; y <= yMax; y++) {
				for (int z = zMin; z <= zMax; z++) {
					Block current = Utility.getRealBlock(p.getWorld()
							.getBlockAt(x, y, z));
					if (!(current.getType() == Material.AIR) && !rm.isReinforced(current)
							&& !Utility.wouldPlantDoubleReinforce(current)) {
						if (!rt.canBeReinforced(current.getType())) {
							Utility.sendAndLog(p, ChatColor.RED, "The block at " + x + ", " + y + ", " + z +
									" was not reinforced because the material type you are using cannot reinforce that type of block.");
							continue;
						}
						Utility.createPlayerReinforcementWithoutMaterialConsumption(p, g, current, rt);
					}
				}
			}
		}

		Utility.sendAndLog(p, ChatColor.GREEN, "Successfully created reinforcements");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new ArrayList<String>();
	}
}
