package com.devotedmc.ExilePearl.listener;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.ExileRule;
import com.devotedmc.ExilePearl.config.PearlConfig;

import isaac.bastion.BastionBlock;
import isaac.bastion.event.BastionCreateEvent;
import isaac.bastion.event.BastionDamageEvent;
import isaac.bastion.event.BastionDamageEvent.Cause;

public class BastionListenerTest {
	
	private ExilePearlApi pearlApi;
	private PearlConfig config;
	private BastionListener dut;
	
	final UUID uid = UUID.randomUUID();
	final Player player = mock(Player.class);

	@Before
	public void setUp() throws Exception {
		config = mock(PearlConfig.class);
		
		pearlApi = mock(ExilePearlApi.class);
		when(pearlApi.getPearlConfig()).thenReturn(config);
		
		dut = new BastionListener(pearlApi);
		
		when(player.getUniqueId()).thenReturn(uid);
	}

	@Test
	public void testBastionListener() {
		// Null arguments throw exceptions
		Throwable e = null;
		try { new BastionListener(null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testOnBastionCreate() {
		BastionCreateEvent e = new BastionCreateEvent(mock(BastionBlock.class), player);
		when(config.canPerform(ExileRule.CREATE_BASTION)).thenReturn(true);
		dut.onBastionCreate(e);
		assertFalse(e.isCancelled());

		e = new BastionCreateEvent(mock(BastionBlock.class), player);
		when(config.canPerform(ExileRule.CREATE_BASTION)).thenReturn(false);
		dut.onBastionCreate(e);
		assertFalse(e.isCancelled());

		e = new BastionCreateEvent(mock(BastionBlock.class), player);
		when(config.canPerform(ExileRule.CREATE_BASTION)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onBastionCreate(e);
		assertFalse(e.isCancelled());
		
		e = new BastionCreateEvent(mock(BastionBlock.class), player);
		when(config.canPerform(ExileRule.CREATE_BASTION)).thenReturn(false);
		dut.onBastionCreate(e);
		assertTrue(e.isCancelled());
	}

	@Test
	public void testOnBastionDamage() {
		BastionDamageEvent e = new BastionDamageEvent(mock(BastionBlock.class), player, Cause.BLOCK_PLACED);
		when(config.canPerform(ExileRule.DAMAGE_BASTION)).thenReturn(true);
		dut.onBastionDamage(e);
		assertFalse(e.isCancelled());

		e = new BastionDamageEvent(mock(BastionBlock.class), player, Cause.BLOCK_PLACED);
		when(config.canPerform(ExileRule.DAMAGE_BASTION)).thenReturn(false);
		dut.onBastionDamage(e);
		assertFalse(e.isCancelled());

		e = new BastionDamageEvent(mock(BastionBlock.class), player, Cause.BLOCK_PLACED);
		when(config.canPerform(ExileRule.DAMAGE_BASTION)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onBastionDamage(e);
		assertFalse(e.isCancelled());
		
		e = new BastionDamageEvent(mock(BastionBlock.class), player, Cause.BLOCK_PLACED);
		when(config.canPerform(ExileRule.DAMAGE_BASTION)).thenReturn(false);
		dut.onBastionDamage(e);
		assertTrue(e.isCancelled());
		
		e = new BastionDamageEvent(mock(BastionBlock.class), player, Cause.PEARL);
		dut.onBastionDamage(e);
		assertTrue(e.isCancelled());
		
		e = new BastionDamageEvent(mock(BastionBlock.class), player, Cause.ELYTRA);
		dut.onBastionDamage(e);
		assertTrue(e.isCancelled());
	}
}
