package com.github.maxopoly.essenceglue;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.config.ConfigHelper;
import vg.civcraft.mc.civmodcore.config.ConfigParser;
import vg.civcraft.mc.civmodcore.dao.DatabaseCredentials;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

public class EssenceConfigManager extends ConfigParser {

	private ManagedDatasource db;
	private int maxStreak;
	private long streakDelay;
	private long streakGracePeriod;
	private long timeForGain;
	private ItemMap loginReward;
	private ItemMap votingReward;
	private Map<String, VotingSite> votingCooldowns;
	private boolean giveRewardToPearled;
	private boolean multiplyPearlCost;

	public EssenceConfigManager(ACivMod plugin) {
		super(plugin);
	}

	@Override
	protected boolean parseInternal(ConfigurationSection config) {
		if (config.contains("database")) {
			db = ManagedDatasource.construct((ACivMod) plugin, (DatabaseCredentials) config.get("database"));
		}
		maxStreak = config.getInt("max_streak", 8);
		streakDelay = ConfigHelper.parseTime(config.getString("streak_delay", "20 hours"));
		streakGracePeriod = ConfigHelper.parseTime(config.getString("streak_grace_period", "1 day"));
		timeForGain = ConfigHelper.parseTime(config.getString("online_for_reward", "30 minutes"));
		loginReward = ConfigHelper.parseItemMap(config.getConfigurationSection("login_reward"));
		votingReward = ConfigHelper.parseItemMap(config.getConfigurationSection("voting_reward"));
		votingCooldowns = new HashMap<>();
		if (config.isConfigurationSection("voting_sites")) {
			ConfigurationSection votingKeySection = config.getConfigurationSection("voting_sites");
			for (String key : votingKeySection.getKeys(false)) {
				if (votingKeySection.isConfigurationSection(key)) {
					ConfigurationSection current = votingKeySection.getConfigurationSection(key);
					long votingCooldown = ConfigHelper.parseTime(current.getString("voting_cooldown", "20h"));
					String votingUrl = current.getString("voting_url");
					String internalKey = current.getString("internal_key");
					String name = current.getString("name");
					votingCooldowns.put(internalKey, new VotingSite(name, votingUrl, internalKey, votingCooldown));
				} else {
					logger.warning("Ignoring invalid entry " + key + " at  " + votingKeySection.getCurrentPath());
				}
			}
		}
		giveRewardToPearled = config.getBoolean("give_reward_to_pearled", false);
		multiplyPearlCost = config.getBoolean("multiply_pearl_cost", true);
		return true;
	}

	public ManagedDatasource getDatabase() {
		return db;
	}

	public Map<String, VotingSite> getVotingCooldowns() {
		return votingCooldowns;
	}

	public boolean giveRewardToPearled() {
		return giveRewardToPearled;
	}

	public boolean multiplyPearlCost() {
		return multiplyPearlCost;
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
