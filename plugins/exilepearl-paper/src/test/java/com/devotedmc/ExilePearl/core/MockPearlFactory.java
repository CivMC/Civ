package com.devotedmc.ExilePearl.core;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PearlManager;
import com.devotedmc.ExilePearl.PearlPlayer;
import com.devotedmc.ExilePearl.PearlWorker;

public class MockPearlFactory implements PearlFactory {

	@Override
	public ExilePearl createExilePearl(UUID uid, UUID killedBy, Location location) {
		return new MockPearl(uid, killedBy, location);
	}

	@Override
	public ExilePearl createExilePearl(UUID uid, Player killedBy, int health) {
		ExilePearl pearl = new MockPearl(uid, killedBy.getUniqueId(), killedBy.getLocation());
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
