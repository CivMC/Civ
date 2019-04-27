package vg.civcraft.mc.civchat2.commands;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "afk")
public class Afk extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		boolean isAfk = CivChat2.getInstance().getCivChat2Manager().togglePlayerAfk(player);
		if (isAfk) {
			player.sendMessage(ChatStrings.chatAfk);
		} else {
			player.sendMessage(ChatStrings.chatNotAfk);
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new LinkedList<>();
	}
}
