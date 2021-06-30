package vg.civcraft.mc.civchat2.commands;

import static vg.civcraft.mc.civchat2.ChatStrings.localChatFormat;


import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;

@CommandAlias("exit")
public class Exit extends BaseCommand {

	@Syntax("/exit")
	@Description("Exit private or group chats")
	public void execute(CommandSender sender, @Optional String message) {
		CivChat2Manager chatMan = CivChat2.getInstance().getCivChat2Manager();
		Player player = (Player) sender;
		if (message == null) {
			chatMan.removeChannel(player);
			chatMan.removeGroupChat(player);
			player.sendMessage(ChatStrings.chatMovedToGlobal);
			return;
		}
		StringBuilder chatMsg = new StringBuilder();
		if (!message.isEmpty()) {
			chatMsg.append(message);
		}
		Set<Player> players = new HashSet<>(CivChat2.getInstance().getServer().getOnlinePlayers());
		chatMan.broadcastMessage(player, chatMsg.toString(), localChatFormat, players);
	}
}
