package com.devotedmc.ExilePearl.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlAccess;
import com.devotedmc.ExilePearl.PearlPlayer;
import com.devotedmc.ExilePearl.PlayerProvider;
import com.devotedmc.ExilePearl.util.Guard;
import com.devotedmc.ExilePearl.util.TextUtil;

/**
 * Extends the Bukkit Player class to add ExilePearl specific data
 * @author Gordon
 *
 */
class CorePearlPlayer implements PearlPlayer {
	
	private final UUID playerId;
	private final PlayerProvider playerProvider;
	private final PearlAccess pearlAccess;

	@Override
	public UUID getUniqueId() {
		return playerId;
	}

	@Override
	public String getName() {
		return playerProvider.getRealPlayerName(playerId);
	}

	@Override
	public boolean isOnline() {
		Player p = getPlayer();
		return p != null && p.isOnline();
	}

	@Override
	public Player getPlayer() {
		return playerProvider.getPlayer(playerId);
	}

	// Players that are receiving prison pearl broadcast messages
	private final Set<PearlPlayer> bcastPlayers = new HashSet<PearlPlayer>();
	
	// The last player who requested a pearl broadcast
	private PearlPlayer broadcastRequestPlayer;
	
	public CorePearlPlayer(final UUID playerId, final PlayerProvider nameProvider, final PearlAccess pearlAccess) {
		Guard.ArgumentNotNull(playerId, "playerId");
		Guard.ArgumentNotNull(nameProvider, "nameProvider");
		Guard.ArgumentNotNull(pearlAccess, "pearlAccess");
		
		this.playerId = playerId;
		this.playerProvider = nameProvider;
		this.pearlAccess = pearlAccess;
	}

	@Override
	public void msg(String str, Object... args) {
		if (str == null || str == "") {
			return; // Silently ignore null or empty strings
		}
		
		Player p = getPlayer();
		if (p != null && p.isOnline()) {
			p.sendMessage(TextUtil.instance().parse(String.format(str, args)));
		}
	}
	
	@Override
	public Set<PearlPlayer> getBcastPlayers() {
		return Collections.unmodifiableSet(bcastPlayers);
	}

	@Override
	public void addBcastPlayer(PearlPlayer sp) {
		this.bcastPlayers.add(sp);
	}

	@Override
	public void removeBcastPlayer(PearlPlayer sp) {
		this.bcastPlayers.remove(sp);
	}

	@Override
	public PearlPlayer getRequestedBcastPlayer() {
		return this.broadcastRequestPlayer;
	}

	@Override
	public void setRequestedBcastPlayer(PearlPlayer broadcastRequestPlayer) {
		this.broadcastRequestPlayer = broadcastRequestPlayer;
	}

	@Override
	public boolean isExiled() {
		return pearlAccess.isPlayerExiled(playerId);
	}

	@Override
	public ExilePearl getExilePearl() {
		return pearlAccess.getPearl(this.getUniqueId());
	}
}
