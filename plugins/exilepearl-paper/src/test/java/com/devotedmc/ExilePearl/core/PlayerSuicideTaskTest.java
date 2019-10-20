package com.devotedmc.ExilePearl.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static vg.civcraft.mc.civmodcore.util.TextUtil.*;

import java.util.UUID;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.config.PearlConfig;
import com.devotedmc.testbukkit.TestBukkit;
import com.devotedmc.testbukkit.TestBukkitRunner;

@RunWith(TestBukkitRunner.class)
public class PlayerSuicideTaskTest {

	private PearlConfig pearlConfig;
	private ExilePearlApi pearlApi;
	private PlayerSuicideTask dut;

	@Before
	public void setUp() throws Exception {

		pearlConfig = mock(PearlConfig.class);
		when(pearlConfig.getSuicideTimeoutSeconds()).thenReturn(100);

		pearlApi = mock(ExilePearlApi.class);
		when(pearlApi.getPearlConfig()).thenReturn(pearlConfig);

		dut = new PlayerSuicideTask(pearlApi);
		dut.loadConfig(pearlConfig);
	}

	@Test
	public void testPlayerSuicideTask() {
		// Null arguments throw exceptions
		Throwable e = null;
		try { new PlayerSuicideTask(null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testStartStop() {
		final BukkitScheduler scheduler = TestBukkit.getServer().getScheduler();
		reset(scheduler);

		assertFalse(dut.isRunning());

		dut.start();
		verify(scheduler).scheduleSyncRepeatingTask(pearlApi, dut, PlayerSuicideTask.TICKS_PER_SECOND, PlayerSuicideTask.TICKS_PER_SECOND);
		assertTrue(dut.isRunning());

		dut.start();
		verify(scheduler).scheduleSyncRepeatingTask(pearlApi, dut, PlayerSuicideTask.TICKS_PER_SECOND, PlayerSuicideTask.TICKS_PER_SECOND);
		assertTrue(dut.isRunning());

		dut.stop();
		verify(scheduler).cancelTask(anyInt());
		assertFalse(dut.isRunning());
	}

	@Test
	public void testRestart() {		
		dut.restart();
		assertTrue(dut.isRunning());
	}

	@Test
	public void testRun() {
		final UUID uid = UUID.randomUUID();		
		Player player = mock(Player.class);
		when(player.getUniqueId()).thenReturn(uid);
		when(player.isOnline()).thenReturn(true);

		when(pearlApi.getPlayer(uid)).thenReturn(player);

		when(pearlConfig.getSuicideTimeoutSeconds()).thenReturn(20);
		dut.loadConfig(pearlConfig);

		dut.start();
		dut.addPlayer(player);
		verify(player).sendMessage(parse(Lang.suicideInSeconds, 20));

		for (int i = 0; i < 10; i++) {
			dut.run();
		}
		verify(player).sendMessage(parse(Lang.suicideInSeconds, 10));

		for (int i = 0; i < 5; i++) {
			dut.run();
		}
		verify(player).sendMessage(parse(Lang.suicideInSeconds, 5));

		dut.run();
		verify(player).sendMessage(parse(Lang.suicideInSeconds, 4));

		dut.run();
		verify(player).sendMessage(parse(Lang.suicideInSeconds, 3));

		dut.run();
		verify(player).sendMessage(parse(Lang.suicideInSeconds, 2));

		dut.run();
		verify(player).sendMessage(parse(Lang.suicideInSeconds, 1));

		dut.run();
		verify(player).setHealth(0);
	}

	@Test
	public void testAddPlayer() {
		final UUID uid = UUID.randomUUID();
		Player player = mock(Player.class);
		when(player.getUniqueId()).thenReturn(uid);
		when(player.isOnline()).thenReturn(true);

		when(pearlApi.getPlayer(uid)).thenReturn(player);

		assertFalse(dut.isAdded(uid));

		dut.addPlayer(player);
		verify(player).sendMessage(parse(Lang.suicideInSeconds, pearlConfig.getSuicideTimeoutSeconds()));
		assertTrue(dut.isAdded(uid));

		Location l1 = mock(Location.class);
		when(player.getLocation()).thenReturn(l1);

		Location l2 = mock(Location.class);
		when(l2.distance(any(Location.class))).thenReturn(3.0);
		PlayerMoveEvent e = new PlayerMoveEvent(player, l1, l2);

		dut.onPlayerMove(e);
		verify(player).sendMessage(parse(Lang.suicideCancelled));
		assertFalse(dut.isAdded(uid));
	}
}
