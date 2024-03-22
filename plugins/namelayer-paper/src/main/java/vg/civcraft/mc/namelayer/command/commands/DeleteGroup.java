package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
		String confirm = "CONFIRM_DELETION";
		if(x.toLowerCase().contains(confirm.toLowerCase()))
		{
			//check if they met the 15 second window
			if(confirmDeleteGroup.containsKey(uuid)){
				//user is in the hashmap
				String[] entry = confirmDeleteGroup.get(uuid);
				Group gD = gm.getGroup(entry[0]);

				if(x.equalsIgnoreCase("%s_%s".formatted(confirm, gD.getName()))) {

					//player could have lost delete permission in the mean time
					if (!NameAPI.getGroupManager().hasAccess(gD, uuid, PermissionType.getPermission("DELETE"))){
						p.sendMessage(Component.text("You do not have permission to run that command.").color(NamedTextColor.RED));
						return;
					}
					Date now = new Date(System.currentTimeMillis() - 15000);
					//if it has been less than 15 seconds
					if(now.getTime() < Long.parseLong(entry[1]))
					{
						//good to go delete the group
						if(gm.deleteGroup(gD.getName()))
							p.sendMessage(Component.text("Group was successfully deleted.").color(NamedTextColor.GREEN));
						else
							p.sendMessage(Component.text("Group is now disciplined. Check back later to see if group is deleted.").color(NamedTextColor.GREEN));

						confirmDeleteGroup.remove(uuid);
						return;
					}
					else{
						p.sendMessage(Component.text("You did not do /nldg %s_%s fast enough, you will need to start over".formatted(confirm, gD.getName())).color(NamedTextColor.RED));
						confirmDeleteGroup.remove(uuid);
						return;
					}
				}
			}
			
			
		}
		Group g = gm.getGroup(x);
		if (groupIsNull(sender, x, g)) {
			return;
		}
		if (!NameAPI.getGroupManager().hasAccess(g, uuid, PermissionType.getPermission("DELETE"))){
			p.sendMessage(Component.text("You do not have permission to run that command.").color(NamedTextColor.RED));
			return;
		}
		PlayerType pType = g.getPlayerType(uuid);
		if (pType == null && !p.hasPermission("namelayer.admin")){
			p.sendMessage(Component.text("You are not on that group.").color(NamedTextColor.RED));
			return;
		}
		if (g.isDisciplined() && !p.hasPermission("namelayer.admin")){
			p.sendMessage(Component.text("Group is disiplined.").color(NamedTextColor.RED));
			return;
		}
		//set that user can confirm group in 15 seconds
		Date date = new Date();
		Long dateString = date.getTime();
		String[] groupDate = new String[] {g.getName(), dateString.toString()};
		p.sendMessage(Component.text("To confirm the IRREVERSIBLE deletion of the group '%s' along with ALL reinforcements, bastions and snitches on it:\nType /nldg %s_%s within 15 seconds.".formatted(g.getName(), confirm, g.getName())).color(NamedTextColor.RED));
		confirmDeleteGroup.put(uuid, groupDate);
		return;
	}
}
