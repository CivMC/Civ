package com.devotedmc.ExilePearl.listener;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.junit.Before;
import org.junit.Test;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.ExileRule;
import com.devotedmc.ExilePearl.config.PearlConfig;

public class JukeAlertListenerTest {
	
	private ExilePearlApi pearlApi;
	private PearlConfig config;
	private JukeAlertListener dut;
	
	final UUID uid = UUID.randomUUID();
	final Player player = mock(Player.class);

	@Before
	public void setUp() throws Exception {
		config = mock(PearlConfig.class);
		
		pearlApi = mock(ExilePearlApi.class);
		when(pearlApi.getPearlConfig()).thenReturn(config);
		
		dut = new JukeAlertListener(pearlApi);
		
		when(player.getUniqueId()).thenReturn(uid);
	}

	@Test
	public void testJukeAlertListener() {
		// Null arguments throw exceptions
		Throwable e = null;
		try { new JukeAlertListener(null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testOnSnitchPlaced() {
		Block block = mock(Block.class);
		when(block.getType()).thenReturn(Material.JUKEBOX);
		
		BlockPlaceEvent e = new BlockPlaceEvent(block, null, null, null, player, true, null);
		when(config.canPerform(ExileRule.SNITCH)).thenReturn(true);
		dut.onSnitchPlaced(e);
		assertFalse(e.isCancelled());

		e = new BlockPlaceEvent(block, null, null, null, player, true, null);
		when(config.canPerform(ExileRule.SNITCH)).thenReturn(false);
		dut.onSnitchPlaced(e);
		assertFalse(e.isCancelled());

		e = new BlockPlaceEvent(block, null, null, null, player, true, null);
		when(config.canPerform(ExileRule.SNITCH)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onSnitchPlaced(e);
		assertFalse(e.isCancelled());
		
		e = new BlockPlaceEvent(block, null, null, null, player, true, null);
		when(config.canPerform(ExileRule.SNITCH)).thenReturn(false);
		dut.onSnitchPlaced(e);
		assertTrue(e.isCancelled());
		
		when(block.getType()).thenReturn(Material.NOTE_BLOCK);
		e = new BlockPlaceEvent(block, null, null, null, player, true, null);
		dut.onSnitchPlaced(e);
		assertTrue(e.isCancelled());
		
		when(block.getType()).thenReturn(Material.STONE);
		e = new BlockPlaceEvent(block, null, null, null, player, true, null);
		dut.onSnitchPlaced(e);
		assertFalse(e.isCancelled());
	}
}
