package com.github.maxopoly.essenceglue.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.github.maxopoly.essenceglue.EssenceGluePlugin;
import com.github.maxopoly.essenceglue.StreakManager;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.utilities.TextUtil;

public class StreakCommand extends BaseCommand {

    @CommandAlias("streak")
    @Description("Displays stats about your daily login streak")
    public void execute(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        UUID uuid = StreakManager.getTrueUUID(player.getUniqueId());
        StreakManager streakMan = EssenceGluePlugin.instance().getStreakManager();
        player.sendMessage(ChatColor.GREEN + "Your current login streak is " + streakMan.getRecalculatedCurrentStreak(uuid));
        long cooldown = streakMan.getRewardCooldown(uuid);
        if (cooldown > 0) {
            player.sendMessage(ChatColor.YELLOW + "You will be eligible for daily rewards again in " + TextUtil
                .formatDuration(cooldown));
        } else {
            long left = streakMan.untilTodaysReward(uuid);
            player.sendMessage(ChatColor.GREEN + "You will receive your daily rewards in " + TextUtil.formatDuration(left));
        }
    }
}
