package com.github.maxopoly.essenceglue.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.github.maxopoly.essenceglue.EssenceGluePlugin;
import com.github.maxopoly.essenceglue.StreakManager;
import com.github.maxopoly.essenceglue.VotifyManager;
import com.github.maxopoly.essenceglue.VotingSite;
import java.util.UUID;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.utilities.TextUtil;

public class VoteCommand extends BaseCommand {

	@CommandAlias("vote")
	@Description("Lists all available voting websites")
	public void execute(Player sender) {
		VotifyManager voteMan = EssenceGluePlugin.instance().getVoteManager();
		if (voteMan == null) {
			sender.sendMessage(ChatColor.RED + "Voting is not enabled");
			return;
		}
		Player p = (Player) sender;
		for (VotingSite site : EssenceGluePlugin.instance().getConfigManager().getVotingCooldowns().values()) {
			UUID trueUUID = StreakManager.getTrueUUID(p.getUniqueId());
			long lastVote = voteMan.getLastVote(site.getInternalKey(), trueUUID);
			boolean canVote = (System.currentTimeMillis() - lastVote) > site.getVotingCooldown();
			if (canVote) {
				TextComponent text = new TextComponent(
						ChatColor.GREEN + "Receive rewards for voting on " + site.getName()
								+ ". Click this message to open the link!");
				text.setClickEvent(
						new ClickEvent(Action.OPEN_URL, site.getVotingUrl().replace("%PLAYER%", p.getName())));
				text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new Text("Click to open the voting link for " + site.getName())));
				p.spigot().sendMessage(text);
			} else {
				long remaining = site.getVotingCooldown() - (System.currentTimeMillis() - lastVote);
				p.sendMessage(
						ChatColor.YELLOW + "You already voted on " + site.getName() + " and may vote there again in "
								+ TextUtil.formatDuration(remaining));
			}
		}
	}
}
