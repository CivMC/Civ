package com.untamedears.realisticbiomes.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.untamedears.realisticbiomes.utils.RealisticBiomesGUI;
import org.bukkit.ChatColor;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("rb|rbmenu|plants")
@CommandPermission("rb.public")
public class Menu extends BaseCommand {

	@Syntax("rb")
	@Description("Opens a GUI allowing you to browse RealisticBiomes growth rates for current biome")
	public void onCommand(CommandSender sender, String biome) {
		Player p = (Player) sender;
		if (p.isInsideVehicle()) {
			p.sendMessage(ChatColor.RED + "You can't use this command in vehicles");
			return;
		}
		if (biome.isEmpty()) {
			RealisticBiomesGUI gui = new RealisticBiomesGUI((Player) sender);
			gui.showRBOverview(null);
		} else {
			if (!p.hasPermission("rb.pickBiome")) {
				p.sendMessage(ChatColor.RED + "You lack permission to use this command with arguments");
				return;
			}
			String concat = String.join(" ", biome);
			for (Biome b : Biome.values()) {
				if (b.toString().equals(concat)) {
					RealisticBiomesGUI gui = new RealisticBiomesGUI((Player) sender);
					gui.showRBOverview(b);
					return;
				}
			}
			p.sendMessage(ChatColor.RED + "The biome " + concat + " does not exist");
		}
	}
}
