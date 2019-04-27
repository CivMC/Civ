package vg.civcraft.mc.civchat2.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civchat2.database.CivChatDAO;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

@CivCommand(id = "ignore")
public class IgnoreGroup extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Group group = GroupManager.getGroup(args [0]);
		Player player = (Player) sender;
		if (group == null) {
			player.sendMessage(ChatStrings.chatGroupNotFound);
			return true;
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
			return true;
		} else {
			db.removeIgnoredGroup(player.getUniqueId(), ignore);
			player.sendMessage(String.format(ChatStrings.chatStoppedIgnoring, ignore));
			return true;
		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (args.length == 0) {
			return GroupTabCompleter.complete(null, PermissionType.getPermission("READ_CHAT"), (Player) sender);
		}
		return GroupTabCompleter.complete(args [0], PermissionType.getPermission("READ_CHAT"), (Player) sender);
	}
}
