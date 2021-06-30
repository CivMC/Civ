package vg.civcraft.mc.civchat2.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.database.CivChatDAO;

@CommandAlias("ignore")
public class Ignore extends BaseCommand {

	@Syntax("/ignore <player>")
	@Description("Toggles ignoring a player")
	public void execute(CommandSender sender, String targetPlayer) {
		Player player = (Player) sender;
		Player ignoredPlayer = Bukkit.getServer().getPlayer(targetPlayer);
		if (ignoredPlayer == null) {
			player.sendMessage(ChatStrings.chatPlayerNotFound);
			return;
		}
		if (player == ignoredPlayer) {
			player.sendMessage(ChatStrings.chatCantIgnoreSelf);
			return;
		}
		CivChatDAO db = CivChat2.getInstance().getDatabaseManager();
		if (!db.isIgnoringPlayer(player.getUniqueId(), ignoredPlayer.getUniqueId())) {
			// Player added to the list
			db.addIgnoredPlayer(player.getUniqueId(), ignoredPlayer.getUniqueId());
			player.sendMessage(String.format(ChatStrings.chatNowIgnoring, ignoredPlayer.getDisplayName()));
			return;
		} else {
			// Player removed from the list
			db.removeIgnoredPlayer(player.getUniqueId(), ignoredPlayer.getUniqueId());
			player.sendMessage(String.format(ChatStrings.chatStoppedIgnoring, ignoredPlayer.getDisplayName()));
			return;
		}
	}
}
