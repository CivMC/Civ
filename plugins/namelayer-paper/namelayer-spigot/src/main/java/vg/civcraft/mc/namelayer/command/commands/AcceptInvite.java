package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.command.TabCompleters.InviteTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;

@CommandAlias("nlacceptinvite|nlacceptgroup|acceptinvite|acceptgroup|accept|join|ctjoin|ctj|ag")
public class AcceptInvite extends BaseCommandMiddle {

	@Syntax("/nlag <group>")
	@Description("Accept an invitation to a group.")
	public void execute(CommandSender sender, String groupName) {
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.YELLOW + "Baby you dont got a uuid, why you got to make this difficult for everyone :(");
			return;
		}
		Player p = (Player) sender;
		Group group = gm.getGroup(groupName);
		if (groupIsNull(sender, groupName, group)) {
			return;
		}
		UUID uuid = NameAPI.getUUID(p.getName());
		PlayerType type = group.getInvite(uuid);
		if (type == null){
			p.sendMessage(Component.text("You were not invited to that group.").color(NamedTextColor.RED));
			return;
		}
		if (group.isDisciplined()){
			p.sendMessage(Component.text("That group is disiplined.").color(NamedTextColor.RED));
			return;
		}
		if (group.isMember(uuid)){
			p.sendMessage(Component.text("You are already a member you cannot join again.").color(NamedTextColor.RED));
			group.removeInvite(uuid, true);
			return;
		}
		group.addMember(uuid, type);
		group.removeInvite(uuid, true);
		PlayerListener.removeNotification(uuid, group);
		p.sendMessage(Component.text("You have successfully been added to the group as a " + type.name() +".").color(NamedTextColor.RED));
	}

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
