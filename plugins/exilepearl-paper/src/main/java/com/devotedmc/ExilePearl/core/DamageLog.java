package com.devotedmc.ExilePearl.core;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.entity.Player;

import com.devotedmc.ExilePearl.util.Clock;

import vg.civcraft.mc.civmodcore.util.Guard;

/**
 * Logs damage dealt to a player by other players.
 * 
 * @author Gordon
 */
class DamageLog {
	private final Clock clock;
	private final UUID playerId;
	private final Map<UUID, DamageRecord> damagers = new HashMap<UUID, DamageRecord>();
	
	/**
	 * Creates a new DamageLog instance
	 * @param playerId The player Id
	 */
	public DamageLog(Clock clock, UUID playerId) {
		Guard.ArgumentNotNull(clock, "clock");
		Guard.ArgumentNotNull(playerId, "playerId");
		
		this.clock = clock;
		this.playerId = playerId;
	}
	
	/**
	 * Gets the player ID
	 * @return The player ID
	 */
	public UUID getPlayerId() {
		return playerId;
	}	

	/**
	 * Records damage for the player
	 * @param damager The damager
	 * @param amount The damage amount
	 * @param maxAmount The max damage amount that should be tracked
	 */
	public void recordDamage(Player damager, double amount, double maxAmount) {
		DamageRecord rec = damagers.get(damager.getUniqueId());
		if (rec == null) {
			rec = new DamageRecord(clock, damager.getUniqueId());
			damagers.put(damager.getUniqueId(), rec);
		}

		rec.recordDamage(amount, maxAmount);
	}
	
	/**
	 * Decays all the damage records by a given amount.
	 * 
	 * Once the damage amount for a particular player reaches zero,
	 * it is removed from tracking.
	 * 
	 * When there are no longer any players being tracked, then
	 * this method returns false to indicate that it can be removed.
	 * 
	 * @param amount The damage amount to decay
	 * @return true if there are still damagers being tracked
	 */
	public boolean decayDamage(double decayAmount) {		
		Iterator<DamageRecord> it = damagers.values().iterator();
		while (it.hasNext()) {
			final DamageRecord rec = it.next();
			if (!rec.decayDamage(decayAmount)) {
				it.remove();
			}
		}
		return damagers.size() > 0;
	}
	
	/**
	 * Gets the time-sorted damagers
	 * The first object in the list will be the most recent damager and
	 * the last object will be the least recent damager.
	 * @return The time-sorted damager players
	 */
	public List<DamageRecord> getTimeSortedDamagers() {
		List<DamageRecord> recs = new LinkedList<DamageRecord>(damagers.values());
		
		Collections.sort(recs, new Comparator<DamageRecord>() {
		     public int compare(DamageRecord o1, DamageRecord o2) {
		         if(o1.getTime() == o2.getTime()) {
		             return 0;
		         }
		         return o1.getTime() > o2.getTime() ? -1 : 1;
		     }
		});
		
		return recs;
	}
	
	/**
	 * Gets the damage-sorted damagers
	 * The first object in the list will be the player who damaged the most and
	 * the last object will be the player who damaged the least.
	 * @return The damage-sorted damager players
	 */
	public List<DamageRecord> getDamageSortedDamagers() {
		List<DamageRecord> recs = new LinkedList<DamageRecord>(damagers.values());
		
		Collections.sort(recs, new Comparator<DamageRecord>() {
		     public int compare(DamageRecord o1, DamageRecord o2) {
		         if(o1.getAmount() == o2.getAmount()) {
		             return 0;
		         }
		         return o1.getAmount() > o2.getAmount() ? -1 : 1;
		     }
		});
		
		return recs;
	}
	
	@Override
	public String toString() {
		return "DamageLog{player: " + playerId.toString() + ", damagers:{" + damagers + "}}";
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31)
				.append(playerId)
				.append(damagers)
				.toHashCode();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		DamageLog other = (DamageLog) o;

		return new EqualsBuilder()
				.append(playerId, other.playerId)
				.append(damagers, other.damagers)
				.isEquals();
	}
}
