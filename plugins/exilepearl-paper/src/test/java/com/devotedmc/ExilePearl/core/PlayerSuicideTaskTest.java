package com.devotedmc.ExilePearl.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.Before;
import org.junit.Test;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.PearlConfig;
import com.devotedmc.ExilePearl.PearlPlayer;

public class PlayerSuicideTaskTest {
	
	private BukkitScheduler mockScheduler;
	private PearlConfig pearlConfig;
	private ExilePearlApi pearlApi;
	private PlayerSuicideTask dut;

	@Before
	public void setUp() throws Exception {
		
		mockScheduler = mock(BukkitScheduler.class);
		
		pearlConfig = mock(PearlConfig.class);
		when(pearlConfig.getSuicideTimeoutSeconds()).thenReturn(100);
		
		pearlApi = mock(ExilePearlApi.class);
		when(pearlApi.getPearlConfig()).thenReturn(pearlConfig);
		when(pearlApi.getScheduler()).thenReturn(mockScheduler);
		
		dut = spy(new PlayerSuicideTask(pearlApi));
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
		
		assertFalse(dut.isRunning());
		
		dut.start();
		verify(mockScheduler).scheduleSyncRepeatingTask(null, dut, PlayerSuicideTask.TICKS_PER_SECOND, PlayerSuicideTask.TICKS_PER_SECOND);
		assertTrue(dut.isRunning());
		
		dut.start();
		verify(mockScheduler).scheduleSyncRepeatingTask(null, dut, PlayerSuicideTask.TICKS_PER_SECOND, PlayerSuicideTask.TICKS_PER_SECOND);
		assertTrue(dut.isRunning());
		
		dut.stop();
		verify(mockScheduler).cancelTask(anyInt());
		assertFalse(dut.isRunning());
	}

	@Test
	public void testRestart() {		
		dut.restart();
		verify(dut).stop();
		verify(dut).start();
		assertTrue(dut.isRunning());
	}

	@Test
	public void testRun() {
		
		final UUID uid = UUID.randomUUID();		
		PearlPlayer player = mock(PearlPlayer.class);
		when(player.getUniqueId()).thenReturn(uid);
		
		when(pearlConfig.getSuicideTimeoutSeconds()).thenReturn(20);

		dut.start();
		dut.addPlayer(player);
		verify(player).msg(Lang.suicideInSeconds, 20);
		
		for (int i = 0; i < 10; i++) {
			dut.run();
		}
		verify(player).msg(Lang.suicideInSeconds, 10);
		
		for (int i = 0; i < 5; i++) {
			dut.run();
		}
		verify(player).msg(Lang.suicideInSeconds, 5);
		
		dut.run();
		verify(player).msg(Lang.suicideInSeconds, 4);
		
		dut.run();
		verify(player).msg(Lang.suicideInSeconds, 3);
		
		dut.run();
		verify(player).msg(Lang.suicideInSeconds, 2);
		
		dut.run();
		verify(player).msg(Lang.suicideInSeconds, 1);

		dut.run();
		verify(player).setHealth(0);
	}

	@Test
	public void testAddPlayer() {
		final UUID uid = UUID.randomUUID();		
		PearlPlayer player = mock(PearlPlayer.class);
		when(player.getUniqueId()).thenReturn(uid);
	
		assertFalse(dut.isAdded(player));
		
		dut.addPlayer(player);
		verify(player).msg(Lang.suicideInSeconds, pearlConfig.getSuicideTimeoutSeconds());
		assertTrue(dut.isAdded(player));
		
		PlayerMoveEvent e = new PlayerMoveEvent(player, mock(Location.class), mock(Location.class));
		
		dut.onPlayerMove(e);
		verify(player).msg(Lang.suicideCancelled);
		assertFalse(dut.isAdded(player));
	}
}
