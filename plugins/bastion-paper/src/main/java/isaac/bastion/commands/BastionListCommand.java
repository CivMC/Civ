/**
 * Created by Aleksey on 12.08.2017.
 */

package isaac.bastion.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.base.Strings;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.Permissions;
import isaac.bastion.manager.BastionBlockManager;
import isaac.bastion.utils.ChatFiller;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class BastionListCommand implements CommandExecutor {
	private static class ArgsInfo {
		public int pageNumber;
		public List<String> groupNames;
	}

	private static class GroupInfo {
		public String name;
		public List<Integer> ids;
	}

	private static class BastionInfo {
		public String location;
		public String strength;
		public String group;
		public String typeName;
		public String typeShortName;
		public String hoverText;
	}

	private static class BastionBlockComparator implements Comparator<BastionBlock> {
		@Override
		public int compare(BastionBlock b1, BastionBlock b2) {
			Location l1 = b1.getLocation();
			Location l2 = b2.getLocation();

			int cmp = l1.getWorld().getName().compareTo(l2.getWorld().getName());
			if(cmp != 0) return cmp;

			cmp = Integer.compare(l1.getBlockX(), l2.getBlockX());
			if(cmp != 0) return cmp;

			cmp = Integer.compare(l1.getBlockZ(), l2.getBlockZ());
			if(cmp != 0) return cmp;

			return Integer.compare(l1.getBlockY(), l2.getBlockY());
		}
	}

	private static final int pageSize = 10;
	private static final double locationColWidth = 31;
	private static final double groupColWidth = 22;
	private static final double typeColWidth = 10;

	private static final BastionBlockComparator bastionComparator = new BastionBlockComparator();

	private Map<UUID, Long> lastExecutions = new HashMap<>();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only player could use this command.");
			return false;
		}

		final Player player = (Player)sender;
		final ArgsInfo argsInfo = new ArgsInfo();

		if(!parseArgs(player, args, argsInfo)) return false;

		Long lastExecution = this.lastExecutions.get(player.getUniqueId());

		if(lastExecution == null || System.currentTimeMillis() - lastExecution > Bastion.getCommonSettings().getListBastionTimeout()) {
			Bukkit.getScheduler().runTaskAsynchronously(Bastion.getPlugin(), new Runnable() {
				@Override
				public void run() {
					readListAndShow(player, argsInfo);
				}
			});

			this.lastExecutions.put(player.getUniqueId(), System.currentTimeMillis());
		} else {
			double timeRemained = Bastion.getCommonSettings().getListBastionTimeout() - (System.currentTimeMillis() - lastExecution);
			String message = ChatColor.RED  + String.format("Slow down, try again in %.1f seconds", (timeRemained / 1000d));

			player.sendMessage(message);
		}

		return true;
	}

	private static boolean parseArgs(Player player, String[] args, ArgsInfo result) {
		result.pageNumber = 1;
		result.groupNames = null;

		if (args.length == 0) return true;

		// Reassemble any arguments that are enclosed in quotes and were split
		List<String> fixedArgs = new ArrayList<String>();
		Scanner scanner = new Scanner(String.join(" ", args));
		scanner.useDelimiter("\\s(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
		while (scanner.hasNext()) {
			fixedArgs.add(scanner.next());
		}
		scanner.close();

		// Parse each argument
		for (String arg : fixedArgs) {
			arg = arg.toLowerCase().trim();
			if (arg.startsWith("groups=")) {
				if (arg.length() > 7) {
					String groupNamesRaw = arg.substring(7);
					// Strip quotes
					groupNamesRaw = groupNamesRaw.replaceAll("^[\"']|[\"']$", "");
					result.groupNames = Arrays.asList(groupNamesRaw.split(","));
					continue;
				}
			} else {
				try {
					result.pageNumber = Integer.parseInt(arg);
				} catch (NumberFormatException e) {
					result.pageNumber = 1;
				}
				continue;
			}

			player.sendMessage(ChatColor.RED + "Unrecognized argument: '" + arg + "'");
			return false;
		}

		return true;
	}

	private static void readListAndShow(Player player, ArgsInfo argsInfo) {
		List<BastionInfo> list = readList(player.getUniqueId(), argsInfo);

		if(list.size() == 0) {
			Bukkit.getScheduler().runTask(Bastion.getPlugin(), new Runnable() {
				@Override
				public void run() {
					player.sendMessage(ChatColor.AQUA + " * Page " + argsInfo.pageNumber + " is empty");
				}
			});
			return;
		}

		showList(player, argsInfo, list);
	}

	private static void showList(final Player player, ArgsInfo argsInfo, List<BastionInfo> list) {
		String topLine = ChatColor.WHITE + " Bastion List " + ChatColor.DARK_GRAY + Strings.repeat("-", 10) + "\n";

		String columnNames = ChatColor.GRAY
				+ ChatFiller.fillString("Location", locationColWidth)
				+ ChatFiller.fillString("Group", groupColWidth)
				+ ChatFiller.fillString("Type", typeColWidth)
				+ "\n"
				+ ChatColor.WHITE;

		final TextComponent output = new TextComponent(topLine);
		output.addExtra(columnNames);

		Map<String, String> shortenings = new HashMap<>();

		for(BastionInfo info : list) {
			TextComponent line = new TextComponent(ChatFiller.fillString(info.location, locationColWidth));
			line.addExtra(ChatFiller.fillString(info.group, groupColWidth));
			line.addExtra(ChatFiller.fillString(getBastionType(info.typeName, info.typeShortName, shortenings), typeColWidth));
			line.addExtra("\n");

			line.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(info.hoverText).create()));

			output.addExtra(line);
		}

		if(shortenings.size() > 0) {
			List<String> shortNames = new ArrayList<>();
			shortNames.addAll(shortenings.keySet());
			Collections.sort(shortNames);

			for(String shortName : shortNames) {
				String comment = ChatColor.GRAY + "* " + shortName + " = " + shortenings.get(shortName) + "\n";
				output.addExtra(comment);
			}
		}

		String bottomLine = ChatColor.DARK_GRAY + " * Page " + argsInfo.pageNumber + " ";
		bottomLine = bottomLine + Strings.repeat("-", bottomLine.length());
		output.addExtra(bottomLine);

		Bukkit.getScheduler().runTask(Bastion.getPlugin(), new Runnable() {
			@Override
			public void run() {
				player.spigot().sendMessage(output);
			}
		});
	}

	private static String getBastionType(String typeName, String typeShortName, Map<String, String> shortenings) {
		if(typeShortName != null && typeShortName.length() > 0) return typeShortName;

		if(typeName.length() < typeColWidth) return typeName;

		String[] parts = typeName.split(" ");
		StringBuilder result = new StringBuilder();

		for(String part : parts) {
			result.append(part.substring(0, 1).toUpperCase());
		}

		String shortName = result.toString();

		if(!shortenings.containsKey(shortName)) {
			shortenings.put(shortName, typeName);
		}

		return shortName;
	}

	private static List<BastionInfo> readList(UUID playerId, ArgsInfo argsInfo) {
		BastionBlockManager manager = Bastion.getBastionManager();
		List<GroupInfo> groups = getPlayerGroups(playerId, argsInfo.groupNames);
		List<BastionInfo> result = new ArrayList<>();
		List<BastionBlock> buffer = new ArrayList<>();
		int start = (argsInfo.pageNumber - 1) * pageSize;
		int end = start + pageSize - 1;
		int current = 0;

		for(GroupInfo groupInfo : groups) {
			if(current > end) break;

			manager.getBastionsByGroupIds(groupInfo.ids, buffer);

			Collections.sort(buffer, bastionComparator);

			for(BastionBlock bastion : buffer) {
				if(current >= start) {
					BastionInfo bastionInfo = new BastionInfo();
					bastionInfo.location = bastion.getLocationText();
					bastionInfo.strength = bastion.getStrengthText();
					bastionInfo.group = groupInfo.name;
					bastionInfo.typeName = bastion.getType().getItemName();
					bastionInfo.typeShortName = bastion.getType().getShortName();
					bastionInfo.hoverText = bastion.getHoverText();

					result.add(bastionInfo);
				}

				current++;

				if(current > end) break;
			}

			buffer.clear();
		}

		return result;
	}

	private static List<GroupInfo> getPlayerGroups(UUID playerId, List<String> groupNames) {
		PermissionType permission = PermissionType.getPermission(Permissions.BASTION_LIST);
		GroupManager groupManager = NameAPI.getGroupManager();
		List<GroupInfo> result = new ArrayList<GroupInfo>();

		if(groupNames == null) {
			groupNames = groupManager.getAllGroupNames(playerId);
		}

		Collections.sort(groupNames);

		Set<String> processedGroupNames = new HashSet<>();

		for (String groupName : groupNames) {
			if(processedGroupNames.contains(groupName.toLowerCase())) continue;

			processedGroupNames.add(groupName.toLowerCase());

			Group group = GroupManager.getGroup(groupName);

			if (group != null && groupManager.hasAccess(group, playerId, permission)) {
				GroupInfo info = new GroupInfo();
				info.name = group.getName();
				info.ids = group.getGroupIds();

				result.add(info);
			}
		}

		return result;
	}

}
