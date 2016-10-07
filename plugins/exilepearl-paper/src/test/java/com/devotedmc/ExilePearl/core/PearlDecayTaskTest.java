package com.devotedmc.ExilePearl.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.Before;
import org.junit.Test;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Util.BukkitTestCase;
import com.devotedmc.ExilePearl.config.PearlConfig;

public class PearlDecayTaskTest extends BukkitTestCase {
	
	private PearlConfig pearlConfig;
	private ExilePearlApi pearlApi;
	private PearlDecayTask dut;

	@Before
	public void setUp() throws Exception {
		
		pearlConfig = mock(PearlConfig.class);
		when(pearlConfig.getPearlHealthDecayIntervalMin()).thenReturn(60);
		
		pearlApi = mock(ExilePearlApi.class);
		when(pearlApi.getPearlConfig()).thenReturn(pearlConfig);
		
		dut = spy(new PearlDecayTask(pearlApi));
	}

	@Test
	public void testPlayerSuicideTask() {
		// Null arguments throw exceptions
		Throwable e = null;
		try { new PearlDecayTask(null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testStartStop() {
		final BukkitScheduler scheduler = getServer().getScheduler();
		reset(scheduler);
		
		assertFalse(dut.isRunning());
		
		dut.start();
		verify(scheduler).scheduleSyncRepeatingTask(null, dut, 72000, 72000);
		assertTrue(dut.isRunning());
		
		dut.start();
		verify(scheduler).scheduleSyncRepeatingTask(null, dut, 72000, 72000);
		assertTrue(dut.isRunning());
		
		dut.stop();
		verify(scheduler).cancelTask(anyInt());
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
		dut.run();
		verify(pearlApi, times(0)).decayPearls();

		dut.start();
		dut.run();
		verify(pearlApi).decayPearls();
	}
}
