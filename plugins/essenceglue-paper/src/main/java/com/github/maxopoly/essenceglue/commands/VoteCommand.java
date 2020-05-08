package com.github.maxopoly.essenceglue.commands;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.maxopoly.essenceglue.EssenceGluePlugin;
import com.github.maxopoly.essenceglue.StreakManager;
import com.github.maxopoly.essenceglue.VotifyManager;
import com.github.maxopoly.essenceglue.VotingSite;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.civmodcore.util.TextUtil;

@CivCommand(id = "vote")
public class VoteCommand extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		VotifyManager voteMan = EssenceGluePlugin.instance().getVoteManager();
		if (voteMan == null) {
			sender.sendMessage(ChatColor.RED + "Voting is not enabled");
			return true;
		}
		Player p = (Player) sender;
		for (VotingSite site : EssenceGluePlugin.instance().getConfigManager().getVotingCooldowns().values()) {
			UUID trueUUID = StreakManager.getTrueUUID(p.getUniqueId());
			long lastVote = voteMan.getLastVote(site.getInternalKey(), trueUUID);
			boolean canVote = (System.currentTimeMillis() - lastVote) > site.getVotingCooldown();
			if (canVote) {
				TextComponent text = new TextComponent(ChatColor.GREEN + "Receive rewards for voting on "
						+ site.getName() + ". Click this message to open the link!");
				text.setClickEvent(
						new ClickEvent(Action.OPEN_URL, site.getVotingUrl().replace("%PLAYER%", p.getName())));
				text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new ComponentBuilder("Click to open the voting link for " + site.getName()).create()));
				p.spigot().sendMessage(text);
			} else {
				long remaining = site.getVotingCooldown() - (System.currentTimeMillis() - lastVote);
				p.sendMessage(ChatColor.YELLOW + "You already voted on " + site.getName()
						+ " and may vote there again in " + TextUtil.formatDuration(remaining));
			}
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return Collections.emptyList();
	}

}
