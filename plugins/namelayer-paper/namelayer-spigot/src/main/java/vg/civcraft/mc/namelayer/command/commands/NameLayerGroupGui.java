package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.gui.GUIGroupOverview;
import vg.civcraft.mc.namelayer.gui.MainGroupGUI;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class NameLayerGroupGui extends BaseCommandMiddle {

	@CommandAlias("nl")
	@Syntax("[group]")
	@Description("Open the group management GUI")
	@CommandCompletion("@NL_Groups")
	public void execute(CommandSender sender, @Optional String groupName) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.BLUE + "Go back home console man, we dont want you here");
			return;
		}
		if (groupName == null) {
			GUIGroupOverview gui = new GUIGroupOverview((Player) sender);
			gui.showScreen();
			return;
		}
		Group g = gm.getGroup(groupName);
		if (g == null) {
			sender.sendMessage(ChatColor.RED + "This group doesn't exist");
			return;
		}
		if (!gm.hasAccess(g, ((Player) sender).getUniqueId(), PermissionType.getPermission("OPEN_GUI"))) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do this");
			return;
		}
		MainGroupGUI gui = new MainGroupGUI((Player) sender, g);
	}
}
