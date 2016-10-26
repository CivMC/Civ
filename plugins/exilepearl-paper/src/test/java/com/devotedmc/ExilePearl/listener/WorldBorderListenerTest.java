package com.devotedmc.ExilePearl.listener;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.PearlFreeReason;
import com.devotedmc.ExilePearl.config.PearlConfig;
import com.devotedmc.ExilePearl.event.PearlDecayEvent;
import com.devotedmc.ExilePearl.event.PearlDecayEvent.DecayAction;

public class WorldBorderListenerTest {
	
	private ExilePearlApi pearlApi;
	private PearlConfig config;
	private WorldBorderListener dut;
	
	final UUID uid = UUID.randomUUID();
	final Player player = mock(Player.class);

	@Before
	public void setUp() throws Exception {
		config = mock(PearlConfig.class);
		
		pearlApi = mock(ExilePearlApi.class);
		when(pearlApi.getPearlConfig()).thenReturn(config);
		
		dut = new WorldBorderListener(pearlApi);
		
		when(player.getUniqueId()).thenReturn(uid);
	}

	@Test
	public void testWorldBorderListener() {
		// Null arguments throw exceptions
		Throwable e = null;
		try { new WorldBorderListener(null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testOnPearlDecay() {
		when(config.getShouldAutoFreeWorldBorder()).thenReturn(false);
		when(pearlApi.isLocationInsideBorder(any(Location.class))).thenReturn(true);
		
		List<ExilePearl> pearls = new ArrayList<ExilePearl>();
		when(pearlApi.getPearls()).thenReturn(pearls);
		
		PearlDecayEvent e = new PearlDecayEvent(DecayAction.COMPLETE);
		dut.onPearlDecay(e);
		verify(pearlApi, times(0)).freePearl(any(ExilePearl.class), any(PearlFreeReason.class));

		ExilePearl pearl = mock(ExilePearl.class);
		when(pearl.getLocation()).thenReturn(new Location(mock(World.class), 100, 0, 0));
		pearls.add(pearl);
		dut.onPearlDecay(e);
		verify(pearlApi, times(0)).freePearl(any(ExilePearl.class), any(PearlFreeReason.class));
		
		when(config.getShouldAutoFreeWorldBorder()).thenReturn(true);
		e = new PearlDecayEvent(DecayAction.COMPLETE);
		dut.onPearlDecay(e);
		verify(pearlApi, times(0)).freePearl(any(ExilePearl.class), any(PearlFreeReason.class));

		when(pearlApi.isLocationInsideBorder(any(Location.class))).thenReturn(false);
		e = new PearlDecayEvent(DecayAction.START);
		dut.onPearlDecay(e);
		verify(pearlApi, times(0)).freePearl(any(ExilePearl.class), any(PearlFreeReason.class));
		
		e = new PearlDecayEvent(DecayAction.COMPLETE);
		dut.onPearlDecay(e);
		verify(pearlApi, times(1)).freePearl(pearl, PearlFreeReason.OUTSIDE_WORLD_BORDER);

		pearls.remove(pearl);
		dut.onPearlDecay(e);
		verify(pearlApi, times(1)).freePearl(pearl, PearlFreeReason.OUTSIDE_WORLD_BORDER);
	}
}
