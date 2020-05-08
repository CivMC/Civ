package com.github.maxopoly.essenceglue;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.programmerdan.minecraft.banstick.data.BSPlayer;

import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.playersettings.impl.IntegerSetting;
import vg.civcraft.mc.civmodcore.playersettings.impl.LongSetting;

public class StreakManager {

	private static final long MILLIS_IN_DAY = TimeUnit.DAYS.toMillis(1);

	// streak as a bitwise integer where a 0 is a missed day and a 1 is an online
	// day, in LSB order from current to past
	private IntegerSetting playerStreaks;
	private LongSetting lastPlayerUpdate;
	private long streakDelay;
	private long streakGracePeriod;
	private Map<UUID, Integer> currentOnlineTime;
	private int maximumStreak;
	private long countRequiredForGain;
	private static Map<UUID, UUID> mainAccountCache = new TreeMap<>();
	

	public StreakManager(EssenceGluePlugin plugin, long streakDelay, long streakGracePeriod, int maximumStreak, long onlineTimePerDay) {
		playerStreaks = new IntegerSetting(plugin, 0, "Player essence streak", "essenceGluePlayerStreak");
		PlayerSettingAPI.registerSetting(playerStreaks, null);
		lastPlayerUpdate = new LongSetting(plugin, 0L, "Player streak refresh", "essenceGlueLastUpdate");
		PlayerSettingAPI.registerSetting(lastPlayerUpdate, null);
		Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, 20 * 60L, 20 * 60L);
		this.streakDelay = streakDelay;
		this.streakGracePeriod = streakGracePeriod;
		this.currentOnlineTime = new TreeMap<>();
		this.maximumStreak = maximumStreak;
		this.countRequiredForGain = TimeUnit.MILLISECONDS.toMinutes(onlineTimePerDay);
	}

	private void updateAll() {
		long currentMillis = System.currentTimeMillis();
		for (Player p : Bukkit.getOnlinePlayers()) {
			UUID uuid = getTrueUUID(p.getUniqueId());
			long sinceLastClaim = currentMillis - lastPlayerUpdate.getValue(uuid);
			if (sinceLastClaim >= streakDelay) {
				Integer currentCount = currentOnlineTime.computeIfAbsent(uuid, e -> 0);
				if (currentCount >= countRequiredForGain) {
					incrementPlayerStreak(uuid);
					currentOnlineTime.remove(uuid);
					EssenceGluePlugin.instance().getRewardManager().giveLoginReward(p, getCurrentStreak(uuid));
				} else {
					currentOnlineTime.put(uuid, currentCount + 1);
				}
			}
		}
	}
	
	public long getRewardCooldown(UUID uuid) {
		long sinceLastClaim = System.currentTimeMillis() - lastPlayerUpdate.getValue(uuid);
		return Math.max(0, streakDelay - sinceLastClaim);
	}
	
	public long untilTodaysReward(UUID uuid) {
		Integer currentCount = currentOnlineTime.getOrDefault(uuid, 0);
		return TimeUnit.MINUTES.toMillis(countRequiredForGain - currentCount);
	}
	
	public static UUID getTrueUUID(UUID uuid) {
		UUID cached = mainAccountCache.get(uuid);
		if (cached != null) {
			return cached;
		}
		BSPlayer bsPlayer = BSPlayer.byUUID(uuid);
		if (bsPlayer == null) {
			return null;
		}
		long minID = bsPlayer.getId();
		BSPlayer ogAcc = bsPlayer;
		for(BSPlayer alt : bsPlayer.getTransitiveSharedPlayers(true)) {
			if (alt.getId() < minID) {
				minID = alt.getId();
				ogAcc = alt;
			}
		}
		mainAccountCache.put(uuid, ogAcc.getUUID());
		return ogAcc.getUUID();
	}

	private void incrementPlayerStreak(UUID player) {
		long now = System.currentTimeMillis();
		long lastIncrement = lastPlayerUpdate.getValue(player);
		long timePassed = now - lastIncrement;
		timePassed -= streakDelay;
		timePassed -= streakGracePeriod;
		int daysPassed = 1;
		if (timePassed > 0) {
			daysPassed += (int) (timePassed / MILLIS_IN_DAY + 1);
		}
		int streak = playerStreaks.getValue(player);
		// shift to left by amount of days missed
		streak <<= daysPassed;
		// add new day
		streak |= 1;
		// cap maximum
		streak &= ~((~0) << maximumStreak);
		playerStreaks.setValue(player, streak);
		lastPlayerUpdate.setValue(player, now);
	}

	public int getCurrentStreak(UUID uuid) {
		return Integer.bitCount(playerStreaks.getValue(uuid));
	}
	
	public void setStreakRaw(int streak, long timeStamp, UUID player) {
		playerStreaks.setValue(player, streak);
		lastPlayerUpdate.setValue(player, timeStamp);
	}

}
