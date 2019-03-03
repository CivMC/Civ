package vg.civcraft.mc.civmodcore.ratelimiting;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.entity.Player;

public class RateLimiter {

	private Map<UUID, TokenBucket> buckets;
	private int initialCapacity;
	private int maximumTokens;
	private long refillIntervall;
	private int refillAmount;

	/**
	 * Local constructor to enforce creation through RateLimiting class
	 * 
	 * @param initialCapacity Initial amount of tokens given to new bucket instances
	 * @param maxTokens       Maximum amount of tokens allowed in a bucket
	 * @param refillAmount    Amount added to a bucket per refill
	 * @param refillIntervall How often buckets are refilled in milli seconds
	 */
	RateLimiter(int initialCapacity, int maxTokens, int refillAmount, long refillIntervall) {
		this.buckets = new TreeMap<UUID, TokenBucket>();
		this.initialCapacity = initialCapacity;
		this.maximumTokens = maxTokens;
		this.refillIntervall = refillIntervall;
		this.refillAmount = refillAmount;
	}

	/**
	 * Attempts to pull a token for the player with the given UUID
	 * 
	 * @param uuid UUID of the player
	 * @return True if a token was available and successfully consumed, false
	 *         otherwise
	 */
	public boolean pullToken(UUID uuid) {
		TokenBucket bucket = buckets.get(uuid);
		if (bucket == null) {
			bucket = new TokenBucket(initialCapacity);
			buckets.put(uuid, bucket);
		}
		bucket.refill(maximumTokens, refillAmount, refillIntervall);
		return bucket.pullToken();
	}

	/**
	 * Attempts to pull a token for the player with the given UUID
	 * 
	 * @param uuid UUID of the player
	 * @return True if a token was available and successfully consumed, false
	 *         otherwise
	 */
	public boolean pullToken(Player player) {
		if (player == null) {
			throw new IllegalArgumentException("Can not retrieve token for null player");
		}
		return pullToken(player.getUniqueId());
	}
}
