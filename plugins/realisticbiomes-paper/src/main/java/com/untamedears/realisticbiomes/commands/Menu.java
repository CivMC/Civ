package com.untamedears.realisticbiomes.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.untamedears.realisticbiomes.utils.RealisticBiomesGUI;
import org.bukkit.ChatColor;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

public class Menu extends BaseCommand {

	@CommandAlias("rb|rbmenu|plants")
	@Syntax("[biome]")
	@Description("Opens a GUI allowing you to browse RealisticBiomes growth rates for current biome")
	@CommandCompletion("@RB_Biomes")
	public void onCommand(Player p, @Optional String biome) {
		if (p.isInsideVehicle()) {
			p.sendMessage(ChatColor.RED + "You can't use this command in vehicles");
			return;
		}
		if (biome == null) {
			RealisticBiomesGUI gui = new RealisticBiomesGUI(p);
			gui.showRBOverview(null);
		} else {
			if (!p.hasPermission("rb.pickBiome")) {
				p.sendMessage(ChatColor.RED + "You lack permission to use this command with arguments");
				return;
			}
			String concat = String.join(" ", biome);
			for (Biome b : Biome.values()) {
				if (b.toString().equals(concat)) {
					RealisticBiomesGUI gui = new RealisticBiomesGUI(p);
					gui.showRBOverview(b);
					return;
				}
			}
			p.sendMessage(ChatColor.RED + "The biome " + concat + " does not exist");
		}
	}
}
