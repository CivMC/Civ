package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;

@CommandAlias("nlleg")
public class LeaveGroup extends BaseCommandMiddle {

	@Syntax("/nlleg <group>")
	@Description("Leave a group")
	public void execute(CommandSender sender, String groupName) {
		if (!(sender instanceof Player)){
			sender.sendMessage("Nope, be player");
			return;
		}
		Player p = (Player) sender;
		Group g = gm.getGroup(groupName);
		if (groupIsNull(sender, groupName, g)) {
			return;
		}
		UUID uuid = NameAPI.getUUID(p.getName());
		if (!g.isCurrentMember(uuid)){
			p.sendMessage(ChatColor.RED + "You are not a member of this group.");
			return;
		}
		if (g.isDisciplined()){
			p.sendMessage(ChatColor.RED + "This group is disciplined.");
			return;
		}
		g.removeMember(uuid);
		p.sendMessage(ChatColor.GREEN + "You have been removed from the group.");
	}

	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player))
			return null;

		if (args.length > 0)
			return GroupTabCompleter.complete(args[0], null, (Player) sender);
		else{
			return GroupTabCompleter.complete(null, null, (Player)sender);
		}
	}
}
