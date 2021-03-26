package vg.civcraft.mc.namelayer.command.commands;

import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.PlayerCommandMiddle;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;

public class LeaveGroup extends PlayerCommandMiddle{

	public LeaveGroup(String name) {
		super(name);
		setIdentifier("nlleg");
		setDescription("Leave a group");
		setUsage("/nlleg <group>");
		setArguments(1,1);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage("Nope, be player");
			return true;
		}
		Player p = (Player) sender;
		Group g = gm.getGroup(args[0]);
		if (groupIsNull(sender, args[0], g)) {
			return true;
		}
		UUID uuid = NameAPI.getUUID(p.getName());
		if (!g.isCurrentMember(uuid)){
			p.sendMessage(ChatColor.RED + "You are not a member of this group.");
			return true;
		}
		if (g.isDisciplined()){
			p.sendMessage(ChatColor.RED + "This group is disciplined.");
			return true;
		}
		g.removeMember(uuid);
		p.sendMessage(ChatColor.GREEN + "You have been removed from the group.");
		return true;
	}

	@Override
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
