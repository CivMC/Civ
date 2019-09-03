package vg.civcraft.mc.civmodcore.ratelimiting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

public class RateLimiting {

	private static Map<String, RateLimiter> limiterMapping = new HashMap<>();

	/**
	 * Creates a new rate limiter instance
	 * 
	 * @param name            Name of the instance to create and the one to use for
	 *                        future access
	 * @param initialCapacity The initial amount of tokens given to a player when he
	 *                        consumes a token for the first time
	 * @param maximumCapacity Maximum amount of tokens which can save up for a
	 *                        player
	 * @param refillAmount    How many tokens are refilled per refill run
	 * @param refillIntervall How long until tokens are refill in milli seconds
	 * @return
	 */
	public static RateLimiter createRateLimiter(String name, int initialCapacity, int maximumCapacity, int refillAmount,
			long refillIntervall) {
		if (limiterMapping.containsKey(name)) {
			throw new IllegalArgumentException("Rate limiter with name " + name + " already exists");
		}
		RateLimiter limiter = new RateLimiter(initialCapacity, maximumCapacity, refillAmount, refillIntervall);
		limiterMapping.put(name, limiter);
		return limiter;
	}

	/**
	 * Attempts to pull a token for the player with the given UUID from the limiter
	 * with the given name
	 * 
	 * @param limiterName Name of the rate limiter
	 * @param player      UUID of the player
	 * @return True if a token was available and successfully consumed, false
	 *         otherwise
	 */
	public static boolean isAllowed(String limiterName, UUID player) {
		RateLimiter limiter = limiterMapping.get(limiterName);
		if (limiter == null) {
			throw new IllegalArgumentException("No rate limiter with the name " + limiterName + " is known");
		}
		return limiter.pullToken(player);
	}

	/**
	 * Attempts to pull a token for the given player from the limiter with the given
	 * name
	 * 
	 * @param limiterName Name of the rate limiter
	 * @param player      Player to retrieve token for
	 * @return True if a token was available and successfully consumed, false
	 *         otherwise
	 */
	public static boolean isAllowed(String limiterName, Player player) {
		if (player == null) {
			throw new IllegalArgumentException("Can not retrieve token for null player");
		}
		return isAllowed(limiterName, player.getUniqueId());
	}

	/**
	 * To save the time needed for lookups you can also directly retrieve a rate
	 * limiter. This is encouraged if you are frequently using the same limiter
	 * locally
	 * 
	 * @param name Name of the rate limiter
	 * @return Rate limiter instance with the given name or null if no such limiter
	 *         exists
	 */
	public static RateLimiter getRateLimiter(String name) {
		return limiterMapping.get(name);
	}

}
