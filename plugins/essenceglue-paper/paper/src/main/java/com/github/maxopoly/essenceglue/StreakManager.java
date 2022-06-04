package com.github.maxopoly.essenceglue;

import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.programmerdan.minecraft.banstick.data.BSPlayer;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.players.settings.impl.BooleanSetting;
import vg.civcraft.mc.civmodcore.players.settings.impl.IntegerSetting;
import vg.civcraft.mc.civmodcore.players.settings.impl.LongSetting;

public class StreakManager {

	private static final long MILLIS_IN_DAY = TimeUnit.DAYS.toMillis(1);
	private static final Map<UUID, UUID> mainAccountCache = new TreeMap<>();
	// streak as a bitwise integer where a 0 is a missed day and a 1 is an online
	// day, in LSB order from current to past
	private final IntegerSetting playerStreaks;
	private final LongSetting lastPlayerUpdate;
	private final BooleanSetting receiveRewards;
	private final long streakDelay;
	private final long streakGracePeriod;
	private final Map<UUID, Integer> currentOnlineTime;
	private final int maximumStreak;
	private final long countRequiredForGain;
	private final boolean giveRewardToPearled;
	private final BooleanSetting showSitesOnLogin;

	public StreakManager(EssenceGluePlugin plugin, long streakDelay, long streakGracePeriod, int maximumStreak,
			long onlineTimePerDay, boolean giveRewardToPearled) {
		playerStreaks = new IntegerSetting(plugin, 0, "Player essence streak", "essenceGluePlayerStreak");
		PlayerSettingAPI.registerSetting(playerStreaks, null);
		lastPlayerUpdate = new LongSetting(plugin, 0L, "Player streak refresh", "essenceGlueLastUpdate");
		PlayerSettingAPI.registerSetting(lastPlayerUpdate, null);
		receiveRewards = new BooleanSetting(plugin, true, "Receive essence", "essenceGlueReceiveEssence",
				"Whether you will receive essence on this account");
		showSitesOnLogin = new BooleanSetting(plugin, true, "Show voting sites on login?", "essenceGlueShowSitesOnLogin",
				"Show the sites you can vote on currently when you login?");
		MenuSection menu = PlayerSettingAPI.getMainMenu().createMenuSection("Essence",
				"Essence and voting related settings", new ItemStack(Material.ENDER_EYE));
		PlayerSettingAPI.registerSetting(receiveRewards, menu);
		PlayerSettingAPI.registerSetting(showSitesOnLogin, menu);
		Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, 20 * 60L, 20 * 60L);
		this.streakDelay = streakDelay;
		this.streakGracePeriod = streakGracePeriod;
		this.currentOnlineTime = new TreeMap<>();
		this.maximumStreak = maximumStreak;
		this.giveRewardToPearled = giveRewardToPearled;
		this.countRequiredForGain = TimeUnit.MILLISECONDS.toMinutes(onlineTimePerDay);
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
		for (BSPlayer alt : bsPlayer.getTransitiveSharedPlayers(true)) {
			if (alt.getId() < minID) {
				minID = alt.getId();
				ogAcc = alt;
			}
		}
		mainAccountCache.put(uuid, ogAcc.getUUID());
		return ogAcc.getUUID();
	}

	private void updateAll() {
		long currentMillis = System.currentTimeMillis();
		for (Player p : Bukkit.getOnlinePlayers()) {
			UUID uuid = getTrueUUID(p.getUniqueId());
			if (uuid == null) {
				EssenceGluePlugin.instance().getLogger().severe(p.getName() + " had main account in BanStick?");
				continue;
			}
			long sinceLastClaim = currentMillis - lastPlayerUpdate.getValue(uuid);
			if (sinceLastClaim >= streakDelay) {
				int currentCount = currentOnlineTime.computeIfAbsent(uuid, e -> 0);
				if (currentCount >= countRequiredForGain && receiveRewards.getValue(p)) {
					updatePlayerStreak(uuid);
					currentOnlineTime.remove(uuid);
//					p.sendMessage(ChatColor.GREEN + "Your login streak is now " + ChatColor.LIGHT_PURPLE
//							+ getCurrentStreak(uuid, true));
					if (giveRewardToPearled || ExilePearlPlugin.getApi().getExiledAlts(uuid, true) < 1) {
						EssenceGluePlugin.instance().getRewardManager().giveLoginReward(p,
								getCurrentStreak(uuid, true));
					}
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

	public void updatePlayerStreak(UUID player) {
		long now = System.currentTimeMillis();
		long lastIncrement = lastPlayerUpdate.getValue(player);
		long timePassed = now - lastIncrement;
		timePassed -= streakDelay;
		int daysPassed;
		if (timePassed > 0) {
			daysPassed = 1;
		} else {
			daysPassed = 0;
		}
		timePassed -= streakGracePeriod;

		if (timePassed > 0) {
			daysPassed += (int) (timePassed / MILLIS_IN_DAY + 1);
		}
		int streak = playerStreaks.getValue(player);
		daysPassed = Math.min(daysPassed, maximumStreak);
		// shift to left by amount of days missed
		streak <<= daysPassed;
		// add new day
		streak |= 1;
		// cap maximum with a bit string containing maximumStreak many 1 at the end and
		// only 0 otherwise
		streak = capStreak(streak);
		EssenceGluePlugin.instance().getLogger()
				.info(String.format("Streak for %s was updated, now %d (raw: %d), passed: %d", player.toString(),
						Integer.bitCount(streak), streak, daysPassed));
		playerStreaks.setValue(player, streak);
		lastPlayerUpdate.setValue(player, now);
	}

	public int getCurrentStreak(UUID uuid, boolean isMain) {
		if (!isMain) {
			uuid = getTrueUUID(uuid);
		}
		return Integer.bitCount(playerStreaks.getValue(uuid));
	}
	
	public int getRecalculatedCurrentStreak(UUID uuid) {
		uuid = getTrueUUID(uuid);
		int unshiftedValue = playerStreaks.getValue(uuid);
		long timePassed = System.currentTimeMillis() - lastPlayerUpdate.getValue(uuid) - streakGracePeriod;
		int daysPassed = 0;
		if (timePassed > 0) {
			daysPassed = (int) (timePassed / MILLIS_IN_DAY + 1);
			daysPassed = Math.min(daysPassed, maximumStreak);
		}
		int adjustedStreak = unshiftedValue << daysPassed;
		adjustedStreak = capStreak(adjustedStreak);
		return Integer.bitCount(adjustedStreak);		
	}
	
	private int capStreak(int uncapped) {
		return uncapped & ~((~0) << maximumStreak);
	}

	public void setStreakRaw(int streak, long timeStamp, UUID player) {
		playerStreaks.setValue(player, streak);
		lastPlayerUpdate.setValue(player, timeStamp);
	}

	public boolean getShowSitesOnLogin(UUID uuid) {
		return showSitesOnLogin.getValue(uuid);
	}
}
