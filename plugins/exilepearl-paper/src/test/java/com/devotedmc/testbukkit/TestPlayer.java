package com.devotedmc.testbukkit;

import static org.mockito.Mockito.*;

import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public abstract class TestPlayer implements Player {

	public String name;
	public UUID uid;
	public Location location;
	public boolean isOnline;
	public PlayerInventory inventory;

	public static TestPlayer create(String name, UUID uid) {
		TestPlayer player = mock(TestPlayer.class);
		player.name = name;
		player.uid = uid;
		player.location = null;
		player.isOnline = false;
		player.inventory = mock(PlayerInventory.class);

		when(player.getName()).thenCallRealMethod();
		when(player.getUniqueId()).thenCallRealMethod();
		when(player.isOnline()).thenCallRealMethod();
		when(player.toString()).thenCallRealMethod();
		when(player.getServer()).thenCallRealMethod();
		when(player.runCommand(any())).thenCallRealMethod();
		when(player.goOnline()).thenCallRealMethod();
		when(player.goOffline()).thenCallRealMethod();
		when(player.getInventory()).thenReturn(player.inventory);

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

	@Override
	public Server getServer() {
		return TestBukkit.getServer();
	}

	public boolean goOnline() {
		TestBukkit.getServer().addPlayer(this);
		isOnline = true;
		return isOnline;
	}

	public boolean goOffline() {
		TestBukkit.getServer().getOnlinePlayers().remove(this);
		isOnline = false;
		return isOnline;
	}

    public boolean runCommand(String commandLine) {
    	getServer().getLogger().log(Level.INFO, String.format("Running player command '%s'", commandLine));
    	return getServer().dispatchCommand(this, commandLine);
    }
}
