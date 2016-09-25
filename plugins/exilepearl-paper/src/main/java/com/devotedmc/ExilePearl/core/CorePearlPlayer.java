package com.devotedmc.ExilePearl.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

import com.devotedmc.ExilePearl.PearlPlayer;
import com.devotedmc.ExilePearl.PlayerNameProvider;
import com.devotedmc.ExilePearl.util.TextUtil;

class CorePearlPlayer extends CorePlayerWrapper implements PearlPlayer {

	// Players that are receiving prison pearl broadcast messages
	private final Set<PearlPlayer> bcastPlayers = new HashSet<PearlPlayer>();
	
	// The last player who requested a pearl broadcast
	private PearlPlayer broadcastRequestPlayer;
	
	public CorePearlPlayer(final Player player, final PlayerNameProvider nameProvider) {
		super(player, nameProvider);
	}

	@Override
	public void msg(String str, Object... args) {
		if (str == null || str == "") {
			return; // Silently ignore null or empty strings
		}
		
		if (isOnline()) {
			sendMessage(TextUtil.instance().parse(str, args));
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
	public PearlPlayer getRequestedBcastPlayer() {
		return this.broadcastRequestPlayer;
	}

	@Override
	public void setRequestedBcastPlayer(PearlPlayer broadcastRequestPlayer) {
		this.broadcastRequestPlayer = broadcastRequestPlayer;
	}
}
