package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.Set;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class ShowBlacklist extends BaseCommandMiddle {

	@CommandAlias("nlsbl|showblacklist")
	@Syntax("<group>")
	@Description("Shows all blacklisted players for a specific group")
	@CommandCompletion("@NL_Groups")
	public void execute(CommandSender sender, String groupName) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED
					+ "Why do you have to make this so difficult?");
			return;
		}
		Player p = (Player) sender;
		Group g = gm.getGroup(groupName);
		if (g == null) {
			p.sendMessage(ChatColor.RED + "This group does not exist");
			return;
		}
		if (!gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("BLACKLIST"))
				&& !(p.isOp() || p.hasPermission("namelayer.admin"))) {
			p.sendMessage(ChatColor.RED + "You do not have the required permissions to do this");
			return;
		}
		Set <UUID> ids = NameLayerPlugin.getBlackList().getBlacklist(g);
		if (ids.size() == 0) {
			p.sendMessage(ChatColor.GOLD + "There are no blacklisted players for the group " + g.getName());
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(ChatColor.GOLD + "Blacklisted players for group " + g.getName() + " are: ");
		for(UUID id : ids) {
			sb.append(NameAPI.getCurrentName(id));
			sb.append(", ");
		}
		String reply = sb.toString();
		//remove last ", "
		p.sendMessage(reply.substring(0, reply.length() - 2));
	}
}
