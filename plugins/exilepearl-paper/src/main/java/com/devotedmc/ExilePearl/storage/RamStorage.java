package com.devotedmc.ExilePearl.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import com.devotedmc.ExilePearl.ExilePearl;

class RamStorage implements PluginStorage {

	private HashMap<UUID, ExilePearl> pearls = new HashMap<UUID, ExilePearl>();

	public RamStorage() {

	}

	@Override
	public Collection<ExilePearl> loadAllPearls() {
		return pearls.values();
	}

	@Override
	public void pearlInsert(ExilePearl pearl) {
		pearls.put(pearl.getPlayerId(), pearl);
	}

	@Override
	public void pearlRemove(ExilePearl pearl) {
		pearls.remove(pearl.getPlayerId());
	}

	@Override
	public void updatePearlLocation(ExilePearl pearl) { }

	@Override
	public void updatePearlHealth(ExilePearl pearl) { }

	@Override
	public void updatePearlFreedOffline(ExilePearl pearl) { }

	@Override
	public void updatePearlType(ExilePearl pearl) { }

	@Override
	public void updatePearlKiller(ExilePearl pearl) { }

	@Override
	public boolean connect() { return true; }

	@Override
	public void disconnect() { }

	@Override
	public boolean isConnected() { return true; }

	@Override
	public void updatePearlLastOnline(ExilePearl pearl) { }

	@Override
	public void updatePearlSummoned(ExilePearl pearl) { }

	@Override
	public void updateReturnLocation(ExilePearl pearl) { }
}
