package com.github.maxopoly.KiraBukkitGateway.command.commands;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import com.github.maxopoly.KiraBukkitGateway.rabbit.RabbitCommands;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public class GenerateDiscordAuthCodeCommand extends PlayerCommand {

	public GenerateDiscordAuthCodeCommand() {
		super("discordauth");
		setIdentifier("discordauth");
		setDescription("Create an auth code for linking your ingame account to your discord account");
		setUsage("/discordauth");
		setArguments(0, 0);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You are not a player");
			return true;
		}
		Player p = (Player) sender;
		String code = KiraBukkitGatewayPlugin.getInstance().getAuthcodeManager().getNewCode();
		if (code == null) {
			sender.sendMessage(ChatColor.RED + "Failed to generate code. You should probably tell an admin about this");
			return true;
		}
		// lets make the code upper case to make it easier on people
		code = code.toUpperCase();
		RabbitCommands rabbit = KiraBukkitGatewayPlugin.getInstance().getRabbit();
		rabbit.sendAuthCode(code, p.getName(), p.getUniqueId());
		sender.sendMessage(String.format(
				"%sYour code is '%s', PM 'auth %s' to Kira to authentify. Note that upper/lower case does not matter",
				ChatColor.GOLD, code, code));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		return new LinkedList<>();
	}

}
