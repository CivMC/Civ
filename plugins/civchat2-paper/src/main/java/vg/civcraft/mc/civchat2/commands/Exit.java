package vg.civcraft.mc.civchat2.commands;

import static vg.civcraft.mc.civchat2.ChatStrings.localChatFormat;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "exit")
public class Exit extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		CivChat2Manager chatMan = CivChat2.getInstance().getCivChat2Manager();
		Player player = (Player) sender;
		if (args.length == 0) {
			chatMan.removeChannel(player);
			chatMan.removeGroupChat(player);
			player.sendMessage(ChatStrings.chatMovedToGlobal);
			return true;
		}
		StringBuilder chatMsg = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			chatMsg.append(args[i]);
			chatMsg.append(" ");
		}
		Set<Player> players = new HashSet<>(CivChat2.getInstance().getServer().getOnlinePlayers());
		chatMan.broadcastMessage(player, chatMsg.toString(), localChatFormat, players);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new LinkedList<>();
	}
}
