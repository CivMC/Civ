package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class DeleteGroup extends BaseCommandMiddle {
	
	private Map<UUID, String[]> confirmDeleteGroup = new HashMap<UUID, String[]>();

	@CommandAlias("nldg|delete|deletegroup|groupdelete")
	@Syntax("<group>")
	@Description("Delete a group.")
	@CommandCompletion("@NL_Groups")
	public void execute(Player sender, String groupName) {
		Player p = (Player) sender;
		UUID uuid = NameAPI.getUUID(p.getName());
		String x = groupName;
		String confirm = "CONFIRM";
		if(x.equals(confirm))
		{
			//check if they met the 15 second window
			if(confirmDeleteGroup.containsKey(uuid)){
				//user is in the hashmap
				String[] entry = confirmDeleteGroup.get(uuid);
				Group gD = gm.getGroup(entry[0]);
				//player could have lost delete permission in the mean time
				if (!NameAPI.getGroupManager().hasAccess(gD, uuid, PermissionType.getPermission("DELETE"))){
					p.sendMessage(ChatColor.RED + "You do not have permission to run that command.");
					return;
				}
				Date now = new Date(System.currentTimeMillis() - 15000);
				//if it has been less than 15 seconds
				if(now.getTime() < Long.parseLong(entry[1]))
				{
					//good to go delete the group
					if(gm.deleteGroup(gD.getName()))
						p.sendMessage(ChatColor.GREEN + "Group was successfully deleted.");
					else
						p.sendMessage(ChatColor.GREEN + "Group is now disciplined."
								+ " Check back later to see if group is deleted.");
					
					confirmDeleteGroup.remove(uuid);
					return;
				}
				else{
					p.sendMessage(ChatColor.RED + "You did not do /nldg CONFIRM fast enough, you will need to start over");
					confirmDeleteGroup.remove(uuid);
					return;
				}
			}
			
			
		}
		Group g = gm.getGroup(x);
		if (groupIsNull(sender, x, g)) {
			return;
		}
		if (!NameAPI.getGroupManager().hasAccess(g, uuid, PermissionType.getPermission("DELETE"))){
			p.sendMessage(ChatColor.RED + "You do not have permission to run that command.");
			return;
		}
		PlayerType pType = g.getPlayerType(uuid);
		if (pType == null && !p.hasPermission("namelayer.admin")){
			p.sendMessage(ChatColor.RED + "You are not on that group.");
			return;
		}
		if (g.isDisciplined() && !p.hasPermission("namelayer.admin")){
			p.sendMessage(ChatColor.RED + "Group is disiplined.");
			return;
		}
		//set that user can confirm group in 15 seconds
		Date date = new Date();
		Long dateString = date.getTime();
		String[] groupDate = new String[] {g.getName(), dateString.toString()};
		p.sendMessage(ChatColor.GREEN + "To confirm deletion of group: " + g.getName() + "\nuse /nldg CONFIRM within 15 seconds");
		confirmDeleteGroup.put(uuid, groupDate);
		return;
	}
}
