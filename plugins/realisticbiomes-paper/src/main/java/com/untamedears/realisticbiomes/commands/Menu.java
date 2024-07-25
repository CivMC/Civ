package com.untamedears.realisticbiomes.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.untamedears.realisticbiomes.utils.RealisticBiomesGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Menu extends BaseCommand {

    @CommandAlias("rb|rbmenu|plants")
    @Syntax("[biome]")
    @Description("Opens a GUI allowing you to browse RealisticBiomes growth rates for current biome")
    @CommandCompletion("@RB_Biomes")
    public void onCommand(CommandSender sender, @Optional String biome) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        if (player.isInsideVehicle()) {
            player.sendMessage(ChatColor.RED + "You can't use this command in vehicles");
            return;
        }
        if (biome == null) {
            RealisticBiomesGUI gui = new RealisticBiomesGUI(player);
            gui.showRBOverview(null);
        } else {
            if (!player.hasPermission("rb.pickBiome")) {
                player.sendMessage(ChatColor.RED + "You lack permission to use this command with arguments");
                return;
            }
            String concat = String.join(" ", biome);
            for (Biome b : Biome.values()) {
                if (b.toString().equals(concat)) {
                    RealisticBiomesGUI gui = new RealisticBiomesGUI(player);
                    gui.showRBOverview(b);
                    return;
                }
            }
            player.sendMessage(ChatColor.RED + "The biome " + concat + " does not exist");
        }
    }
}
