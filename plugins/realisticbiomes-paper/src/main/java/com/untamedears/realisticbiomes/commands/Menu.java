package com.untamedears.realisticbiomes.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Biome;
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
		if (args.length == 0 || args[0].length() == 0) {
			RealisticBiomesGUI gui = new RealisticBiomesGUI((Player) sender);
			gui.showRBOverview(null);
		} else {
			if (!p.hasPermission("rb.pickBiome")) {
				p.sendMessage(ChatColor.RED + "You lack permission to use this command with arguments");
				return true;
			}
			String concat = String.join(" ", args);
			for (Biome b : Biome.values()) {
				if (b.toString().equals(concat)) {
					RealisticBiomesGUI gui = new RealisticBiomesGUI((Player) sender);
					gui.showRBOverview(b);
					return true;
				}
			}
			p.sendMessage(ChatColor.RED + "The biome " + concat + " does not exist");
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return doTabComplete(args [0], Arrays.asList(Biome.values()), Biome::name, false);
	}
}
