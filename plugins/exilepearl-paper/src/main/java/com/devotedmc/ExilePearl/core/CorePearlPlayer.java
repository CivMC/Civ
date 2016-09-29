package com.devotedmc.ExilePearl.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlAccess;
import com.devotedmc.ExilePearl.PearlPlayer;
import com.devotedmc.ExilePearl.PlayerNameProvider;
import com.devotedmc.ExilePearl.util.Guard;
import com.devotedmc.ExilePearl.util.TextUtil;

/**
 * Extends the Bukkit Player class to add ExilePearl specific data
 * @author Gordon
 *
 */
class CorePearlPlayer extends CorePlayerWrapper implements PearlPlayer {
	
	private final PearlAccess pearlAccess;

	// Players that are receiving prison pearl broadcast messages
	private final Set<PearlPlayer> bcastPlayers = new HashSet<PearlPlayer>();
	
	// The last player who requested a pearl broadcast
	private PearlPlayer broadcastRequestPlayer;
	
	public CorePearlPlayer(final Player player, final PlayerNameProvider nameProvider, final PearlAccess pearlAccess) {
		super(player, nameProvider);
		Guard.ArgumentNotNull(pearlAccess, "pearlAccess");
		
		this.pearlAccess = pearlAccess;
	}

	@Override
	public void msg(String str, Object... args) {
		if (str == null || str == "") {
			return; // Silently ignore null or empty strings
		}
		
		if (isOnline()) {
			sendMessage(TextUtil.instance().parse(String.format(str, args)));
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
		return pearlAccess.isPlayerExiled(this);
	}

	@Override
	public ExilePearl getExilePearl() {
		return pearlAccess.getPearl(this.getUniqueId());
	}
}
