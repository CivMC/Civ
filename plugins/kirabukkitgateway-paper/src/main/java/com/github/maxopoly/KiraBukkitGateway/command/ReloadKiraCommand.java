package com.github.maxopoly.KiraBukkitGateway.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadKiraCommand extends BaseCommand {

    @CommandAlias("kirareload")
    @CommandPermission("kira.op")
    @Description("Reloads KiraBukkitGateway")
    public void execute(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        KiraBukkitGatewayPlugin.getInstance().reload();
        sender.sendMessage(ChatColor.GREEN + "Reloaded KiraBukkitGateway");
    }
}
