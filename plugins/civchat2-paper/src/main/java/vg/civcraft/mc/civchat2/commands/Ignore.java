package vg.civcraft.mc.civchat2.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.database.CivChatDAO;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "ignore")
public class Ignore extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		Player ignoredPlayer = Bukkit.getServer().getPlayer(args [0]);
		if (ignoredPlayer == null) {
			player.sendMessage(ChatStrings.chatPlayerNotFound);
			return true;
		}
		if (player == ignoredPlayer) {
			player.sendMessage(ChatStrings.chatCantIgnoreSelf);
			return true;
		}
		CivChatDAO db = CivChat2.getInstance().getDatabaseManager();
		if (!db.isIgnoringPlayer(player.getUniqueId(), ignoredPlayer.getUniqueId())) {
			// Player added to the list
			db.addIgnoredPlayer(player.getUniqueId(), ignoredPlayer.getUniqueId());
			player.sendMessage(String.format(ChatStrings.chatNowIgnoring, ignoredPlayer.getDisplayName()));
			return true;
		} else {
			// Player removed from the list
			db.removeIgnoredPlayer(player.getUniqueId(), ignoredPlayer.getUniqueId());
			player.sendMessage(String.format(ChatStrings.chatStoppedIgnoring, ignoredPlayer.getDisplayName()));
			return true;
		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return null;
	}
}
