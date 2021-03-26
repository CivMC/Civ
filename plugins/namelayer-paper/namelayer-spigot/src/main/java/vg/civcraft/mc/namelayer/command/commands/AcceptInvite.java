package vg.civcraft.mc.namelayer.command.commands;

import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.PlayerCommandMiddle;
import vg.civcraft.mc.namelayer.command.TabCompleters.InviteTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;

public class AcceptInvite extends PlayerCommandMiddle{

	public AcceptInvite(String name) {
		super(name);
		setIdentifier("nlag");
		setDescription("Accept an invitation to a group.");
		setUsage("/nlag <group>");
		setArguments(1,1);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.YELLOW + "Baby you dont got a uuid, why you got to make this difficult for everyone :(");
			return true;
		}
		Player p = (Player) sender;
		Group group = gm.getGroup(args[0]);
		if (groupIsNull(sender, args[0], group)) {
			return true;
		}
		UUID uuid = NameAPI.getUUID(p.getName());
		PlayerType type = group.getInvite(uuid);
		if (type == null){
			p.sendMessage(ChatColor.RED + "You were not invited to that group.");
			return true;
		}
		if (group.isDisciplined()){
			p.sendMessage(ChatColor.RED + "That Group is disiplined.");
			return true;
		}
		if (group.isMember(uuid)){
			p.sendMessage(ChatColor.RED + "You are already a member you cannot join again.");
			group.removeInvite(uuid, true);
			return true;
		}
		group.addMember(uuid, type);
		group.removeInvite(uuid, true);
		PlayerListener.removeNotification(uuid, group);
		p.sendMessage(ChatColor.GREEN + "You have successfully been added to the group as a " + type.name() +".");
		return true;
	}
	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			return null;
		}

		if (args.length > 0)
			return InviteTabCompleter.complete(args[0], (Player) sender);
		else
			return InviteTabCompleter.complete(null, (Player)sender);
	}
}
