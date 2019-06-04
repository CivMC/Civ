package com.github.maxopoly.KiraBukkitGateway.command;

import com.github.maxopoly.KiraBukkitGateway.command.commands.CreateDiscordGroupChatCommand;
import com.github.maxopoly.KiraBukkitGateway.command.commands.DeleteDiscordGroupChatCommand;
import com.github.maxopoly.KiraBukkitGateway.command.commands.GenerateDiscordAuthCodeCommand;
import com.github.maxopoly.KiraBukkitGateway.command.commands.ReloadKiraCommand;
import com.github.maxopoly.KiraBukkitGateway.command.commands.SyncDiscordChannelAccessCommand;

import vg.civcraft.mc.civmodcore.command.CommandHandler;

public class KiraCommandHandler extends CommandHandler{

	@Override
	public void registerCommands() {
		addCommands(new GenerateDiscordAuthCodeCommand());
		addCommands(new CreateDiscordGroupChatCommand());
		addCommands(new DeleteDiscordGroupChatCommand());
		addCommands(new SyncDiscordChannelAccessCommand());
		addCommands(new ReloadKiraCommand());
	}

}
