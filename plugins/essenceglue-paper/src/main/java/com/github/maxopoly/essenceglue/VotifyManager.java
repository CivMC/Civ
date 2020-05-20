package com.github.maxopoly.essenceglue;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.playersettings.impl.LongSetting;
import vg.civcraft.mc.civmodcore.util.TextUtil;

public class VotifyManager implements Listener {

	private final RewardManager rewardMan;
	private final Map<String, VotingSite> perSiteCooldowns;
	private final Map<String, LongSetting> perSiteSettings;

	public VotifyManager(RewardManager rewardMan, Map<String, VotingSite> perSiteCooldowns) {
		this.rewardMan = rewardMan;
		this.perSiteSettings = new HashMap<>();
		this.perSiteCooldowns = new HashMap<>(perSiteCooldowns);
		for (String s : perSiteCooldowns.keySet()) {
			LongSetting setting =
					new LongSetting(EssenceGluePlugin.instance(), 0L, "Last vote " + s, "essenceGlueVoteTime" + s);
			PlayerSettingAPI.registerSetting(setting, null);
			perSiteSettings.put(s, setting);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onVotifierEvent(VotifierEvent event) {
		Vote vote = event.getVote();
		Player player = Bukkit.getPlayer(vote.getUsername());
		if (player == null) {
			return;
		}
		VotingSite site = perSiteCooldowns.get(vote.getServiceName());
		if (site == null) {
			EssenceGluePlugin.instance().getLogger()
					.warning("Received vote from unknown service " + vote.getServiceName());
			player.sendMessage(
					ChatColor.RED + "You voted on a website not properly supported or setup right now, " + vote
							.getServiceName());
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

	public long getLastVote(String internalSiteKey, UUID player) {
		return perSiteSettings.get(internalSiteKey).getValue(player);
	}

}
