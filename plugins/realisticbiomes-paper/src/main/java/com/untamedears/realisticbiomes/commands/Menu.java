package com.untamedears.realisticbiomes.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.realisticbiomes.utils.RealisticBiomesGUI;

import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;


@CivCommand(id = "rb")
public class Menu extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player p = (Player) sender;
		if (p.isInsideVehicle()) {
			p.sendMessage(ChatColor.RED + "You can't use this command in vehicles");
			return true;
		}
		RealisticBiomesGUI gui = new RealisticBiomesGUI((Player) sender);
		gui.showRBOverview(true);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] args) {
		return null;
	}
}
