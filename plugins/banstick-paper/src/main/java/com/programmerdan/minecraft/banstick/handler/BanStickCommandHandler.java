package com.programmerdan.minecraft.banstick.handler;

import com.programmerdan.minecraft.banstick.BanStick;
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
		BanStick.getPlugin().getCommand(BanStickCommand.name).setExecutor(new BanStickCommand());
		BanStick.getPlugin().getCommand(DoubleTapCommand.name).setExecutor(new DoubleTapCommand());
		BanStick.getPlugin().getCommand(ForgiveCommand.name).setExecutor(new ForgiveCommand());
		BanStick.getPlugin().getCommand(BanSaveCommand.name).setExecutor(new BanSaveCommand());
		BanStick.getPlugin().getCommand(LoveTapCommand.name).setExecutor(new LoveTapCommand());
		BanStick.getPlugin().getCommand(TakeItBackCommand.name).setExecutor(new TakeItBackCommand());
		BanStick.getPlugin().getCommand(DowsingRodCommand.name).setExecutor(new DowsingRodCommand());
		BanStick.getPlugin().getCommand(DrillDownCommand.name).setExecutor(new DrillDownCommand());
		BanStick.getPlugin().getCommand(UntangleCommand.name).setExecutor(new UntangleCommand());
		BanStick.getPlugin().getCommand(GetAltsCommand.name).setExecutor(new GetAltsCommand());
	}

}
