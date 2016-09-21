package com.devotedmc.ExilePearl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.devotedmc.ExilePearl.util.Guard;
import com.devotedmc.ExilePearl.util.TextUtil;

/**
 * Wrapper class for an exile pearl player
 * @author Gordon
 *
 */
public class PearlPlayer {
	
	private final Player bukkitPlayer;
	private final String name;
	
	// Players that are receiving prison pearl broadcast messages
	private Set<PearlPlayer> bcastPlayers;
	
	// The last player who requested a pearl broadcast
	private PearlPlayer broadcastRequestPlayer;
	
	/**
	 * Creates a new PearlPlayer instance
	 * @param bukkitPlayer The bukkit player
	 */
	public PearlPlayer(final Player bukkitPlayer, final String name) {
		Guard.ArgumentNotNull(bukkitPlayer, "bukkitPlayer");
		Guard.ArgumentNotNullOrEmpty(name, "name");
		
		this.bukkitPlayer = bukkitPlayer;
		this.name = name;
		
		this.bcastPlayers = new HashSet<PearlPlayer>();
		this.broadcastRequestPlayer = null;
	}
	
	/**
	 * Gets the player UUID
	 * @return The player UUID
	 */
	public UUID getUniqueId() {
		return bukkitPlayer.getUniqueId();
	}
	
	/**
	 * Gets the player name
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the bukkit player
	 * @return The bukkit player
	 */
	public Player getBukkitPlayer() {
		return bukkitPlayer;
	}
	
	
	/**
	 * Gets the broadcasting players
	 * @return The broadcast players
	 */
	public Set<PearlPlayer> getBcastPlayers() {
		return this.bcastPlayers;
	}
	
	
	/**
	 * Adds a broadcast player
	 * @param sp The broadcast player
	 */
	public void addBcastPlayer(PearlPlayer sp) {
		this.bcastPlayers.add(sp);
	}
	
	
	/**
	 * Gets the player requested to broadcast pearl location
	 * @return the requested broadcast player
	 */
	public PearlPlayer getRequestedBcastPlayer() {
		return this.broadcastRequestPlayer;
	}
	
	
	/**
	 * Sets the requested broadcast player
	 * @param broadcastRequestPlayer the requested broadcast player
	 */
	public void setRequestedBcastPlayer(PearlPlayer broadcastRequestPlayer) {
		this.broadcastRequestPlayer = broadcastRequestPlayer;
	}

	/**
	 * Sends a message to an online player
	 * @param str
	 * @param args
	 */
	public void msg(final String str, final Object... args) {
		if (str == null || str == "") {
			return; // Silently ignore null or empty strings
		}
		
		if (bukkitPlayer.isOnline()) {
			this.getBukkitPlayer().sendMessage(TextUtil.instance().parse(str, args));
		}
	}
}
