package vg.civcraft.mc.civmodcore.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.ScoreBoardAPI;

public class SidebarOffCommand extends BaseCommand {

    @CommandAlias("sidebar")
    @Description("Disables all sidebar settings for a player")
    public void execute(CommandSender sender, @Optional String targetPlayer) {
        if (sender instanceof Player player) {
            if (targetPlayer == null) {
                ScoreBoardAPI.purge(player);
                sender.sendMessage(Component.text("Sidebar settings disabled, you must manually re-enable them through /config", NamedTextColor.YELLOW));
                return;
            }
            if (sender.isOp()) {
                handleAdminUsage(sender, targetPlayer);
            }
            sender.sendMessage(Component.text("You cannot disable other players sidebars", NamedTextColor.RED));
            return;
        }
        //We want console & kira to be able to disable players scoreboards
        handleAdminUsage(sender, targetPlayer);
    }

    private void handleAdminUsage(CommandSender sender, String targetPlayer) {
        Player target = Bukkit.getPlayer(targetPlayer);
        if (target == null) {
            sender.sendMessage(Component.text("Could not find player: " + targetPlayer, NamedTextColor.RED));
            return;
        }
        ScoreBoardAPI.purge(target);
        sender.sendMessage(Component.text("Sidebar settings disabled for player " + target.getName(), NamedTextColor.YELLOW));
    }
}
