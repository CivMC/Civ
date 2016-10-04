package com.devotedmc.ExilePearl.core;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlConfig;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PearlLoreGenerator;
import com.devotedmc.ExilePearl.PearlManager;
import com.devotedmc.ExilePearl.PearlPlayer;
import com.devotedmc.ExilePearl.PlayerProvider;
import com.devotedmc.ExilePearl.SuicideHandler;
import com.devotedmc.ExilePearl.util.ExilePearlRunnable;

public class MockPearlFactory implements PearlFactory {
	
	private PlayerProvider nameProvider;
	
	public MockPearlFactory(PlayerProvider nameProvider) {
		this.nameProvider = nameProvider;
	}

	@Override
	public ExilePearl createExilePearl(UUID uid, String killedByName, int pearlId, Location location) {
		return new MockPearl(nameProvider, uid, killedByName, pearlId, location);
	}

	@Override
	public ExilePearl createExilePearl(UUID uid, Player killedBy, int pearlId) {
		ExilePearl pearl = new MockPearl(nameProvider, uid, killedBy.getName(), pearlId, killedBy.getLocation());
		return pearl;
	}

	@Override
	public PearlManager createPearlManager() {
		return null;
	}

	@Override
	public ExilePearlTask createPearlDecayWorker() {
		return null;
	}

	@Override
	public PearlPlayer createPearlPlayer(UUID uid) {
		return null;
	}

	@Override
	public PearlLoreGenerator createLoreGenerator() {
		return null;
	}

	@Override
	public PearlConfig createPearlConfig() {
		return null;
	}

	@Override
	public SuicideHandler createSuicideHandler() {
		return null;
	}

	@Override
	public ExilePearlRunnable createPearlBorderTask() {
		return null;
	}

}
