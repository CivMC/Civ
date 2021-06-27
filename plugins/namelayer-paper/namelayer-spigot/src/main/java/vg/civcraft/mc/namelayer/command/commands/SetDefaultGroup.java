package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

@CommandAlias("nlsdg")
public class SetDefaultGroup extends BaseCommandMiddle {

	@Syntax("/nlsdg <group>")
	@Description("Set or change a default group")
	public void execute(CommandSender sender, String groupName) {
		if (!(sender instanceof Player)){
			sender.sendMessage("I don't think you need to do that.");
			return;
		}
		Player p = (Player) sender;
		UUID uuid = NameAPI.getUUID(p.getName());
		Group g = gm.getGroup(groupName);
		if (groupIsNull(sender, groupName, g)) {
			return;
		}
		
		PlayerType pType = g.getPlayerType(uuid);
		if (pType == null){
			p.sendMessage(ChatColor.RED + "You do not have access to that group.");
			return;
		}

		String x = gm.getDefaultGroup(uuid);
		if(x == null){
			g.setDefaultGroup(uuid);
			p.sendMessage(ChatColor.GREEN + "You have set your default group to " + g.getName());
		}
		else{
			g.changeDefaultGroup(uuid);
			p.sendMessage(ChatColor.GREEN + "You changed your default group from " + x + " to " + gm.getDefaultGroup(uuid));
		}
	}

	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player))
			return null;

		if (args.length == 1)
			return GroupTabCompleter.complete(args[0], PermissionType.getPermission("BLOCKS"), (Player) sender);
		else{
			return GroupTabCompleter.complete(null, PermissionType.getPermission("BLOCKS"), (Player)sender);
		}
	}
}
