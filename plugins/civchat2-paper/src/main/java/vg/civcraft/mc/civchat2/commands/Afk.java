package vg.civcraft.mc.civchat2.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;

public class Afk extends BaseCommand {

	@CommandAlias("afk")
	@Description("Toggle afk status")
	public void execute(Player player) {
		boolean isAfk = CivChat2.getInstance().getCivChat2Manager().togglePlayerAfk(player);
		if (isAfk) {
			player.sendMessage(ChatStrings.chatAfk);
		} else {
			player.sendMessage(ChatStrings.chatNotAfk);
		}
	}
}
