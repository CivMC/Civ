package com.github.maxopoly.KiraBukkitGateway.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import com.github.maxopoly.KiraBukkitGateway.rabbit.RabbitCommands;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class GenerateDiscordAuthCodeCommand extends BaseCommand {

	@CommandAlias("discordauth")
	@Description("Create an auth code for linking your ingame account to your Discord account")
	public void execute(Player sender) {
		Player p = (Player) sender;
		String code = KiraBukkitGatewayPlugin.getInstance().getAuthcodeManager().getNewCode();
		if (code == null) {
			sender.sendMessage(ChatColor.RED + "Failed to generate code. You should probably tell an admin about this");
			return;
		}
		// lets make the code upper case to make it easier on people
		code = code.toUpperCase();
		RabbitCommands rabbit = KiraBukkitGatewayPlugin.getInstance().getRabbit();
		rabbit.sendAuthCode(code, p.getName(), p.getUniqueId());
		sender.sendMessage(String.format(
				"%sYour code is '%s'. Execute '/auth %s' in the official discord to authenticate and link your account. Note that upper/lower case does not matter.",
				ChatColor.GOLD, code, code));
	}
}
