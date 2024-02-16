package vg.civcraft.mc.civchat2.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.google.common.base.Strings;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameAPI;

public class WhoAmI extends BaseCommand {

	@CommandAlias("whoami")
	@Description("Tells you who you are")
	public void execute(CommandSender sender) {
		final String response = ChatColor.YELLOW + "You are: " + ChatColor.RESET;
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			final String name = NameAPI.getCurrentName(player.getUniqueId());
			if (!Strings.isNullOrEmpty(name)) {
				player.sendMessage(response + name);
				return;
			}
		}
		sender.sendMessage(response + sender.getName());
	}
}
