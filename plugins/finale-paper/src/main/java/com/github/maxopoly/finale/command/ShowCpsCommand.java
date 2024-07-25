package com.github.maxopoly.finale.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.combat.CPSHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShowCpsCommand extends BaseCommand {

    @CommandAlias("cps")
    @Description("Shows you how fast you are clicking per second.")
    public void execute(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }

        CPSHandler cpsHandler = Finale.getPlugin().getManager().getCPSHandler();
        if (cpsHandler.isShowingCPS(player)) {
            cpsHandler.hideCPS(player);
            player.sendMessage(ChatColor.RED + "You are no longer viewing your CPS!");
        } else {
            cpsHandler.showCPS(player);
            player.sendMessage(ChatColor.GREEN + "You are now viewing your CPS!");
        }
    }
}
