package com.github.maxopoly.finale.command;

import com.github.maxopoly.finale.Finale;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "gamma")
public class GammaBrightCommand extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player p = (Player) sender;
		Finale.getPlugin().getSettingsManager().getGammaBrightSetting().toggleValue(p.getUniqueId());
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return Collections.emptyList();
	}

}
