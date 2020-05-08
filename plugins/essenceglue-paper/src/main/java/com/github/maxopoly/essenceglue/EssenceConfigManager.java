package com.github.maxopoly.essenceglue;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.CoreConfigManager;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;
import vg.civcraft.mc.civmodcore.util.ConfigParsing;

public class EssenceConfigManager extends CoreConfigManager {

	private ManagedDatasource db;
	private int maxStreak;
	private long streakDelay;
	private long streakGracePeriod;
	private long timeForGain;
	private ItemMap loginReward;
	private ItemMap votingReward;
	private Map<String, VotingSite> votingCooldowns;

	public EssenceConfigManager(ACivMod plugin) {
		super(plugin);
	}

	@Override
	protected boolean parseInternal(ConfigurationSection config) {
		if (config.contains("database")) {
			db = (ManagedDatasource) config.get("database");
		}
		maxStreak = config.getInt("max_streak", 8);
		streakDelay = ConfigParsing.parseTime(config.getString("streak_delay", "20 hours"));
		streakGracePeriod = ConfigParsing.parseTime(config.getString("streak_grace_period", "1 day"));
		timeForGain = ConfigParsing.parseTime(config.getString("online_for_reward", "30 minutes"));
		loginReward = ConfigParsing.parseItemMap(config.getConfigurationSection("login_reward"));
		votingReward = ConfigParsing.parseItemMap(config.getConfigurationSection("voting_reward"));
		votingCooldowns = new HashMap<>();
		if (config.isConfigurationSection("voting_sites")) {
			ConfigurationSection votingKeySection = config.getConfigurationSection("voting_cooldowns");
			for (String key : votingKeySection.getKeys(false)) {
				if (votingKeySection.isConfigurationSection(key)) {
					ConfigurationSection current = votingKeySection.getConfigurationSection(key);
					long votingCooldown = current.getLong("voting_cooldown");
					String votingUrl = current.getString("voting_url");
					String internalKey = current.getString("internal_key");
					String name = current.getString("name");
					votingCooldowns.put(internalKey, new VotingSite(name, votingUrl, internalKey, votingCooldown));
				} else {
					logger.warning("Ignoring invalid entry " + key + " at  " + votingKeySection.getCurrentPath());
				}
			}
		}
		return true;
	}

	public ManagedDatasource getDatabase() {
		return db;
	}
	
	public Map<String,VotingSite> getVotingCooldowns() {
		return votingCooldowns;
	}

	public ItemMap getVotingReward() {
		return votingReward;
	}

	public ItemMap getLoginReward() {
		return loginReward;
	}

	public int getMaxStreak() {
		return maxStreak;
	}

	public long getStreakDelay() {
		return streakDelay;
	}

	public long getStreakGracePeriod() {
		return streakGracePeriod;
	}

	public long getOnlineTimeForReward() {
		return timeForGain;
	}

}
