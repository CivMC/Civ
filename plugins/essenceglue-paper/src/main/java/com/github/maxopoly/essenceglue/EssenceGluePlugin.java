package com.github.maxopoly.essenceglue;

import org.bukkit.Bukkit;
import vg.civcraft.mc.civmodcore.ACivMod;

public final class EssenceGluePlugin extends ACivMod {

	private static EssenceGluePlugin instance;
	private EssenceConfigManager configMan;
	private StreakManager streakMan;
	private RewardManager rewardMan;
	private VotifyManager votifyMan;

	public static EssenceGluePlugin instance() {
		return instance;
	}

	@Override
	public void onEnable() {
		super.onEnable();
		instance = this;
		configMan = new EssenceConfigManager(this);
		if (!configMan.parse()) {
			getLogger().severe("Failed to read config, disabling");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		streakMan = new StreakManager(this, configMan.getStreakDelay(), configMan.getStreakGracePeriod(),
				configMan.getMaxStreak(), configMan.getOnlineTimeForReward(), configMan.giveRewardToPearled());
		if (configMan.getDatabase() != null) {
			EssenceDAO dao = new EssenceDAO(this, configMan.getDatabase());
			if (!dao.update()) {
				getLogger().severe("Failed to apply database updates, disabling");
				Bukkit.getPluginManager().disablePlugin(this);
				return;
			}
		}
		rewardMan = new RewardManager(configMan.getLoginReward(), configMan.getVotingReward());
		if (Bukkit.getPluginManager().isPluginEnabled("Votifier")) {
			votifyMan = new VotifyManager(rewardMan, configMan.getVotingCooldowns());
			Bukkit.getPluginManager().registerEvents(votifyMan, this);
		} else {
			getLogger().info("Votifier is not enabled, no voting support is possible");
		}
		Bukkit.getPluginManager().registerEvents(new ExilePearListener(streakMan, configMan.multiplyPearlCost()), this);
	}

	public StreakManager getStreakManager() {
		return streakMan;
	}

	public RewardManager getRewardManager() {
		return rewardMan;
	}

	public EssenceConfigManager getConfigManager() {
		return configMan;
	}

	public VotifyManager getVoteManager() {
		return votifyMan;
	}

	@Override
	public void onDisable() {
		super.onDisable();
	}

}
