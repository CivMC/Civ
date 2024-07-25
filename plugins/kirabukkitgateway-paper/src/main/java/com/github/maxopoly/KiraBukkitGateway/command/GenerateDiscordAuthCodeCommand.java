package com.github.maxopoly.KiraBukkitGateway.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import com.github.maxopoly.KiraBukkitGateway.rabbit.RabbitCommands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GenerateDiscordAuthCodeCommand extends BaseCommand {

    @CommandAlias("discordauth")
    @Description("Create an auth code for linking your ingame account to your Discord account")
    public void execute(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        String code = KiraBukkitGatewayPlugin.getInstance().getAuthcodeManager().getNewCode();
        if (code == null) {
            sender.sendMessage(ChatColor.RED + "Failed to generate code. You should probably tell an admin about this");
            return;
        }
        // lets make the code upper case to make it easier on people
        code = code.toUpperCase();
        RabbitCommands rabbit = KiraBukkitGatewayPlugin.getInstance().getRabbit();
        rabbit.sendAuthCode(code, player.getName(), player.getUniqueId());
        sender.sendMessage(String.format(
            "%sYour code is '%s'. Execute '/auth %s' in the official discord to authenticate and link your account. Note that upper/lower case does not matter.",
            ChatColor.GOLD, code, code));
    }
}
