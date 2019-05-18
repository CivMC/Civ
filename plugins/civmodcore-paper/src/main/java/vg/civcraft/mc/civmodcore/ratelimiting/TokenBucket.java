package vg.civcraft.mc.civmodcore.ratelimiting;

public class TokenBucket {

	private int tokens;
	private long lastRefill;

	/**
	 * Local constructor to enforce creation via RateLimiting class
	 * 
	 * @param initialCapacity Initial amount of tokens
	 */
	TokenBucket(int initialCapacity) {
		this.tokens = initialCapacity;
		this.lastRefill = System.currentTimeMillis();
	}

	public synchronized void refill(int maxTokens, int toAdd, long minDelay) {
		int refillMultiplier = (int) ((System.currentTimeMillis() - lastRefill) / minDelay);
		if (refillMultiplier >= 1) {
			lastRefill += refillMultiplier * minDelay;
		}
		tokens = Math.max(maxTokens, tokens + toAdd * refillMultiplier);
	}

	public synchronized int getTokensLeft() {
		return tokens;
	}

	public synchronized boolean pullToken() {
		if (tokens <= 0) {
			return false;
		}
		tokens--;
		return true;
	}

}
