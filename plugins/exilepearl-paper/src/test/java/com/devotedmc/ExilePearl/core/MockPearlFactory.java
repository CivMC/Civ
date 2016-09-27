package com.devotedmc.ExilePearl.core;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PearlManager;
import com.devotedmc.ExilePearl.PearlPlayer;
import com.devotedmc.ExilePearl.PearlWorker;
import com.devotedmc.ExilePearl.PlayerNameProvider;

public class MockPearlFactory implements PearlFactory {
	
	private PlayerNameProvider nameProvider;
	
	public MockPearlFactory(PlayerNameProvider nameProvider) {
		this.nameProvider = nameProvider;
	}

	@Override
	public ExilePearl createExilePearl(UUID uid, UUID killedBy, Location location) {
		return new MockPearl(nameProvider, uid, killedBy, location);
	}

	@Override
	public ExilePearl createExilePearl(UUID uid, Player killedBy, int health) {
		ExilePearl pearl = new MockPearl(nameProvider, uid, killedBy.getUniqueId(), killedBy.getLocation());
		pearl.setHealth(health);
		return pearl;
	}

	@Override
	public PearlManager createPearlManager() {
		return null;
	}

	@Override
	public PearlWorker createPearlWorker() {
		return null;
	}

	@Override
	public PearlPlayer createPearlPlayer(Player player) {
		return null;
	}

}
