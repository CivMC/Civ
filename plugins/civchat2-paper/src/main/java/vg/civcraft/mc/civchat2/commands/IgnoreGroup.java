package vg.civcraft.mc.civchat2.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civchat2.database.CivChatDAO;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

@CommandAlias("ignoregroup")
public class IgnoreGroup extends BaseCommand {

	@Syntax("/ignoregroup <group>")
	@Description("Toggles ignoring a group")
	public void execute(CommandSender sender, String targetGroup) {
		Group group = GroupManager.getGroup(targetGroup);
		Player player = (Player) sender;
		if (group == null) {
			player.sendMessage(ChatStrings.chatGroupNotFound);
			return;
		}
		String ignore = group.getName();
		CivChatDAO db = CivChat2.getInstance().getDatabaseManager();
		if (!db.isIgnoringGroup(player.getUniqueId(), ignore)) {
			db.addIgnoredGroup(player.getUniqueId(), ignore);
			CivChat2Manager chatMan = CivChat2.getInstance().getCivChat2Manager();
			player.sendMessage(String.format(ChatStrings.chatNowIgnoring, ignore));
			if (group.equals(chatMan.getGroupChatting(player))) {
				chatMan.removeGroupChat(player);
				player.sendMessage(ChatStrings.chatMovedToGlobal);
			}
			return;
		} else {
			db.removeIgnoredGroup(player.getUniqueId(), ignore);
			player.sendMessage(String.format(ChatStrings.chatStoppedIgnoring, ignore));
		}
	}

	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (args.length == 0) {
			return GroupTabCompleter.complete(null, PermissionType.getPermission("READ_CHAT"), (Player) sender);
		}
		return GroupTabCompleter.complete(args [0], PermissionType.getPermission("READ_CHAT"), (Player) sender);
	}
}
