package vg.civcraft.mc.civchat2.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;

@CommandAlias("afk")
public class Afk extends BaseCommand {

	@Syntax("/afk")
	@Description("Toggle afk status")
	public void execute(CommandSender sender) {
		Player player = (Player) sender;
		boolean isAfk = CivChat2.getInstance().getCivChat2Manager().togglePlayerAfk(player);
		if (isAfk) {
			player.sendMessage(ChatStrings.chatAfk);
		} else {
			player.sendMessage(ChatStrings.chatNotAfk);
		}
	}
}
