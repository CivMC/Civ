package vg.civcraft.mc.namelayer.command.commands;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.command.PlayerCommandMiddle;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.gui.GUIGroupOverview;
import vg.civcraft.mc.namelayer.gui.MainGroupGUI;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class NameLayerGroupGui extends PlayerCommandMiddle {
	
	public NameLayerGroupGui(String name) {
		super(name);
		setIdentifier("nl");
		setDescription("Open the group management GUI");
		setUsage("/nl [group]");
		setArguments(0,1);
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.BLUE + "Go back home console man, we dont want you here");
			return true;
		}
		if (args.length == 0) {
			GUIGroupOverview gui = new GUIGroupOverview((Player) sender);
			gui.showScreen();
			return true;
		}
		Group g = gm.getGroup(args [0]);
		if (g == null) {
			sender.sendMessage(ChatColor.RED + "This group doesn't exist");
			return true;
		}
		if (!gm.hasAccess(g, ((Player) sender).getUniqueId(), PermissionType.getPermission("OPEN_GUI"))) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do this");
			return true;
		}
		MainGroupGUI gui = new MainGroupGUI((Player) sender, g);
		return true;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.BLUE + "Go back home console man, we dont want you here");
			return null;
		}
		if (args.length == 0) {
			return GroupTabCompleter.complete(null, PermissionType.getPermission("OPEN_GUI"), (Player)sender);
		}
		else {
			return GroupTabCompleter.complete(args [0], PermissionType.getPermission("OPEN_GUI"), (Player)sender);
		}
	}

}
