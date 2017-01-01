package com.untamedears.JukeAlert.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.untamedears.JukeAlert.JukeAlert;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RateLimiter {

	private RateLimiter() { }

	private static final int FRAME_COUNT = 4;

	private static final int TICKS_TO_FRAME_ADVANCE = 5;

	private static int messageRateLimitPerWindow_ = 70;

	private static int currentFrame_ = 0;

	private static List<Map<String, Integer>> playerMessageRates_ = new ArrayList<Map<String, Integer>>(FRAME_COUNT);

	static {
		for (int i = 0; i < FRAME_COUNT; ++i) {
			playerMessageRates_.add(new HashMap<String, Integer>());
		}
	}

	public static void initialize(JukeAlert plugin) {

		messageRateLimitPerWindow_ = plugin.getConfigManager().getAlertRateLimit();
		Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			@Override
			public void run() {
				try {
					RateLimiter.advanceFrame();
				} catch (Exception ex) {
					JukeAlert.getInstance().log("RateLimiter task exception: " + ex.toString());
				}
			}
		}, TICKS_TO_FRAME_ADVANCE, TICKS_TO_FRAME_ADVANCE);
	}

	public static void advanceFrame() {

		RateLimiter.currentFrame_ = (RateLimiter.currentFrame_ + 1) % FRAME_COUNT;
		RateLimiter.playerMessageRates_.set(RateLimiter.currentFrame_, new HashMap<String, Integer>());
	}

	public static void sendMessage(Player player, String message) {

		final String playerName = player.getName().toLowerCase();
		final Map<String, Integer> currentRates = playerMessageRates_.get(currentFrame_);
		long sum = 0;
		for (Map<String, Integer> playerRates : playerMessageRates_) {
			Integer rate = playerRates.get(playerName);
			if (rate != null) {
				sum += rate;
			}
		}
		if (sum > messageRateLimitPerWindow_) {
			return;
		}
		Integer curRate = currentRates.get(playerName);
		if (curRate == null) {
			curRate = 0;
		}
		currentRates.put(playerName, curRate + 1);
		player.sendMessage(message);
	}

	public static int getMaxRate() {

		return messageRateLimitPerWindow_;
	}

	public static void setMaxRate(int rate) {

		if (rate > 7) {
			messageRateLimitPerWindow_ = rate;
		}
	}
}
