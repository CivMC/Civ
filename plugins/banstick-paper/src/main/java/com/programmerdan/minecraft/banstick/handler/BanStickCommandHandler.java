package com.programmerdan.minecraft.banstick.handler;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.commands.BanRegistrarCommand;
import com.programmerdan.minecraft.banstick.commands.BanSaveCommand;
import com.programmerdan.minecraft.banstick.commands.BanStickCommand;
import com.programmerdan.minecraft.banstick.commands.DoubleTapCommand;
import com.programmerdan.minecraft.banstick.commands.DowsingRodCommand;
import com.programmerdan.minecraft.banstick.commands.DrillDownCommand;
import com.programmerdan.minecraft.banstick.commands.ForgiveCommand;
import com.programmerdan.minecraft.banstick.commands.GetAltsCommand;
import com.programmerdan.minecraft.banstick.commands.LoveTapCommand;
import com.programmerdan.minecraft.banstick.commands.TakeItBackCommand;
import com.programmerdan.minecraft.banstick.commands.UntangleCommand;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Handles Commands for this plugin. Check plugin.yml for details!
 *
 * @author <a href="mailto:programmerdan@gmail.com">ProgrammerDan</a>
 *
 */
public class BanStickCommandHandler {

	public BanStickCommandHandler(FileConfiguration config) {
		registerCommands();
	}

	private void registerCommands() {
		safeRegisterCommand(BanStickCommand.name, new BanStickCommand());
		safeRegisterCommand(DoubleTapCommand.name, new DoubleTapCommand());
		safeRegisterCommand(ForgiveCommand.name, new ForgiveCommand());
		safeRegisterCommand(BanSaveCommand.name, new BanSaveCommand());
		safeRegisterCommand(LoveTapCommand.name, new LoveTapCommand());
		safeRegisterCommand(TakeItBackCommand.name, new TakeItBackCommand());
		safeRegisterCommand(DowsingRodCommand.name, new DowsingRodCommand());
		safeRegisterCommand(DrillDownCommand.name, new DrillDownCommand());
		safeRegisterCommand(UntangleCommand.name, new UntangleCommand());
		safeRegisterCommand(GetAltsCommand.name, new GetAltsCommand());
		safeRegisterCommand(BanRegistrarCommand.name, new BanRegistrarCommand());
	}
	
	private void safeRegisterCommand(String name, CommandExecutor executor) {
		PluginCommand command = BanStick.getPlugin().getCommand(name);
		if (command != null) {
			command.setExecutor(executor);
		} else {
			BanStick.getPlugin().severe("Failed to register command: " + name);
		}
	}

}
