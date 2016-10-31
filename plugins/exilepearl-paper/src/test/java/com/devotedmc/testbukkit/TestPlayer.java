package com.devotedmc.testbukkit;

import static org.mockito.Mockito.mock;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.mockito.Mockito;

public abstract class TestPlayer implements Player {

	public String name;
	public UUID uid;
	public Location location;
	public boolean isOnline;
	
	public static TestPlayer create(String name, UUID uid) {
		TestPlayer player = mock(TestPlayer.class, Mockito.CALLS_REAL_METHODS);
		player.name = name;
		player.uid = uid;
		player.location = null;
		player.isOnline = false;
		return player;
	}

	public static TestPlayer create(String name) {
		return create(name, UUID.randomUUID());
	}
	
	protected TestPlayer() { }

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public UUID getUniqueId() {
		return uid;
	}
	
	@Override
	public boolean isOnline() {
		return isOnline;
	}
	
	@Override
	public String toString() {
		return String.format("TestPlayer{ name: %s, uid: %s }", name, uid.toString());
	}
	
	public void goOnline() {
		TestBukkit.getServer().addPlayer(this);
		isOnline = true;
	}
	
	public void goOffline() {
		TestBukkit.getServer().getOnlinePlayers().remove(this);
		isOnline = false;
	}
}
