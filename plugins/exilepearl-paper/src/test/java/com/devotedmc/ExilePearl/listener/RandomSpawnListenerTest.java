package com.devotedmc.ExilePearl.listener;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.config.PearlConfig;

import me.josvth.randomspawn.events.NewPlayerSpawn;

public class RandomSpawnListenerTest {

	private ExilePearlApi pearlApi;
	private PearlConfig config;
	private RandomSpawnListener dut;

	final UUID uid = UUID.randomUUID();
	final Player player = mock(Player.class);

	@Before
	public void setUp() throws Exception {
		config = mock(PearlConfig.class);

		pearlApi = mock(ExilePearlApi.class);
		when(pearlApi.getPearlConfig()).thenReturn(config);

		dut = new RandomSpawnListener(pearlApi);

		when(player.getUniqueId()).thenReturn(uid);
	}

	@Test
	public void testRandomSpawnListener() {
		// Null arguments throw exceptions
		Throwable e = null;
		try { new RandomSpawnListener(null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testOnRandomSpawn() {
		World world = mock(World.class);
		when(config.getRulePearlRadius()).thenReturn(100);

		ExilePearl pearl = mock(ExilePearl.class);
		when(pearl.getLocation()).thenReturn(new Location(world, 0, 0, 0));

		NewPlayerSpawn e = new NewPlayerSpawn(player, new Location(world, 0, 0, 0));
		dut.onRandomSpawn(e);
		assertFalse(e.isCancelled());

		when(pearlApi.getPearl(uid)).thenReturn(pearl);
		e = new NewPlayerSpawn(player, new Location(world, 0, 0, 0));
		dut.onRandomSpawn(e);
		assertTrue(e.isCancelled());

		e = new NewPlayerSpawn(player, new Location(world, 0, 150, 0));
		dut.onRandomSpawn(e);
		assertTrue(e.isCancelled());

		e = new NewPlayerSpawn(player, new Location(world, 0, 50, 99));
		dut.onRandomSpawn(e);
		assertTrue(e.isCancelled());

		e = new NewPlayerSpawn(player, new Location(world, 0, 50, 100));
		dut.onRandomSpawn(e);
		assertFalse(e.isCancelled());

		e = new NewPlayerSpawn(player, new Location(world, 0, 0, 50));
		dut.onRandomSpawn(e);
		assertTrue(e.isCancelled());

		e = new NewPlayerSpawn(player, new Location(mock(World.class), 0, 0, 50));
		dut.onRandomSpawn(e);
		assertFalse(e.isCancelled());
	}
}
