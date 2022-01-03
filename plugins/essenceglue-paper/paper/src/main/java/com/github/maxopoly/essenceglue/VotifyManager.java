package com.github.maxopoly.essenceglue;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.impl.LongSetting;
import vg.civcraft.mc.civmodcore.utilities.TextUtil;

public class VotifyManager implements Listener {

	private final RewardManager rewardMan;
	private final Map<String, VotingSite> perSiteCooldowns;
	private final Map<String, LongSetting> perSiteSettings;

	public VotifyManager(RewardManager rewardMan, Map<String, VotingSite> perSiteCooldowns) {
		this.rewardMan = rewardMan;
		this.perSiteSettings = new HashMap<>();
		this.perSiteCooldowns = new HashMap<>(perSiteCooldowns);
		for (String s : perSiteCooldowns.keySet()) {
			LongSetting setting = new LongSetting(EssenceGluePlugin.instance(), 0L, "Last vote " + s,
					"essenceGlueVoteTime" + s);
			PlayerSettingAPI.registerSetting(setting, null);
			perSiteSettings.put(s, setting);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onVotifierEvent(VotifierEvent event) {
		Vote vote = event.getVote();
		Player player = Bukkit.getPlayer(vote.getUsername());
		if (player != null) {
			handOutVotingReward(player, vote);
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(EssenceGluePlugin.instance(), () -> {
			//Mojang API rate limit is 600 requests per 10 minutes, which is not a problem for now
			UUID uuid = NewNameResolver.getUUIDForMojangName(vote.getUsername());
			if (uuid == null) {
				EssenceGluePlugin.instance().getLogger().info("Could not resolve vote from " + vote.getUsername());
				return;
			}
			Player altPlayer = Bukkit.getPlayer(uuid);
			if (altPlayer == null) {
				EssenceGluePlugin.instance().getLogger()
						.info("Found uuid " + uuid + " but could not find player for vote from " + vote.getUsername());
				return;
			}
			Bukkit.getScheduler().runTask(EssenceGluePlugin.instance(), () -> {
				handOutVotingReward(altPlayer, vote);
			});
		});
	}

	public void handOutVotingReward(Player player, Vote vote) {
		VotingSite site = perSiteCooldowns.get(vote.getServiceName());
		if (site == null) {
			EssenceGluePlugin.instance().getLogger()
					.warning("Received vote from unknown service " + vote.getServiceName());
			player.sendMessage(ChatColor.RED + "You voted on a website not properly supported or setup right now, "
					+ vote.getServiceName());
			return;
		}
		UUID uuid = StreakManager.getTrueUUID(player.getUniqueId());
		LongSetting serverCooldown = perSiteSettings.get(vote.getServiceName());
		long lastVoted = serverCooldown.getValue(uuid);
		long now = System.currentTimeMillis();
		long timePassed = now - lastVoted;
		long coolDown = site.getVotingCooldown();
		if (timePassed < coolDown) {
			long remaining = coolDown - timePassed;
			player.sendMessage(
					ChatColor.RED + "You already voted on this site today, you can receive rewards for it again in "
							+ ChatColor.AQUA + TextUtil.formatDuration(remaining));
			return;
		}
		serverCooldown.setValue(uuid, now);
		rewardMan.giveVoteReward(player, site.getName());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onLogin(PlayerLoginEvent event) {
		Player p = event.getPlayer();
		if (!EssenceGluePlugin.instance().getStreakManager().getShowSitesOnLogin(p.getUniqueId())) {
			return;
		}
		for (VotingSite site : EssenceGluePlugin.instance().getConfigManager().getVotingCooldowns().values()) {
			UUID trueUUID = StreakManager.getTrueUUID(p.getUniqueId());
			long lastVote = getLastVote(site.getInternalKey(), trueUUID);
			boolean canVote = (System.currentTimeMillis() - lastVote) > site.getVotingCooldown();
			if (canVote) {
				TextComponent text = new TextComponent(
						ChatColor.GREEN + "Receive rewards for voting on " + site.getName()
								+ ". Click this message to open the link!");
				text.setClickEvent(
						new ClickEvent(ClickEvent.Action.OPEN_URL, site.getVotingUrl().replace("%PLAYER%", p.getName())));
				text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new Text("Click to open the voting link for " + site.getName())));
				Bukkit.getScheduler().runTaskLater(EssenceGluePlugin.instance(), () -> {
					p.sendMessage(text);
				}, 20L);
			}
		}
	}

	public long getLastVote(String internalSiteKey, UUID player) {
		return perSiteSettings.get(internalSiteKey).getValue(player);
	}

}
