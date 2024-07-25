package com.github.maxopoly.essenceglue.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.github.maxopoly.essenceglue.EssenceGluePlugin;
import com.github.maxopoly.essenceglue.StreakManager;
import com.github.maxopoly.essenceglue.VotifyManager;
import com.github.maxopoly.essenceglue.VotingSite;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.utilities.TextUtil;

public class VoteCommand extends BaseCommand {

    @CommandAlias("vote")
    @Description("Lists all available voting websites")
    public void execute(CommandSender sender) {
        VotifyManager voteMan = EssenceGluePlugin.instance().getVoteManager();
        if (voteMan == null) {
            sender.sendMessage(ChatColor.RED + "Voting is not enabled");
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        for (VotingSite site : EssenceGluePlugin.instance().getConfigManager().getVotingCooldowns().values()) {
            UUID trueUUID = StreakManager.getTrueUUID(player.getUniqueId());
            long lastVote = voteMan.getLastVote(site.getInternalKey(), trueUUID);
            boolean canVote = (System.currentTimeMillis() - lastVote) > site.getVotingCooldown();
            if (canVote) {
                TextComponent text = new TextComponent(
                    ChatColor.GREEN + "Receive rewards for voting on " + site.getName()
                        + ". Click this message to open the link!");
                text.setClickEvent(
                    new ClickEvent(Action.OPEN_URL, site.getVotingUrl().replace("%PLAYER%", player.getName())));
                text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new Text("Click to open the voting link for " + site.getName())));
                player.spigot().sendMessage(text);
            } else {
                long remaining = site.getVotingCooldown() - (System.currentTimeMillis() - lastVote);
                player.sendMessage(
                    ChatColor.YELLOW + "You already voted on " + site.getName() + " and may vote there again in "
                        + TextUtil.formatDuration(remaining));
            }
        }
    }
}
