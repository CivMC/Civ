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

import vg.civcraft.mc.citadel.events.AcidBlockEvent;
import vg.civcraft.mc.citadel.events.ReinforcementDamageEvent;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;

public class CitadelListenerTest {
	
	private ExilePearlApi pearlApi;
	private PearlConfig config;
	private CitadelListener dut;
	
	final UUID uid = UUID.randomUUID();
	final Player player = mock(Player.class);

	@Before
	public void setUp() throws Exception {
		config = mock(PearlConfig.class);
		
		pearlApi = mock(ExilePearlApi.class);
		when(pearlApi.getPearlConfig()).thenReturn(config);
		
		dut = new CitadelListener(pearlApi);
		
		when(player.getUniqueId()).thenReturn(uid);
	}

	@Test
	public void testCitadelListener() {
		// Null arguments throw exceptions
		Throwable e = null;
		try { new CitadelListener(null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testOnReinforcementDamage() {
		PlayerReinforcement rein = mock(PlayerReinforcement.class);
		
		ReinforcementDamageEvent e = new ReinforcementDamageEvent(rein, player, null);
		when(config.canPerform(ExileRule.DAMAGE_REINFORCEMENT)).thenReturn(true);
		dut.onReinforcementDamage(e);
		assertFalse(e.isCancelled());

		e = new ReinforcementDamageEvent(rein, player, null);
		when(config.canPerform(ExileRule.DAMAGE_REINFORCEMENT)).thenReturn(false);
		dut.onReinforcementDamage(e);
		assertFalse(e.isCancelled());

		e = new ReinforcementDamageEvent(rein, player, null);
		when(config.canPerform(ExileRule.DAMAGE_REINFORCEMENT)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onReinforcementDamage(e);
		assertFalse(e.isCancelled());
		
		e = new ReinforcementDamageEvent(rein, player, null);
		when(config.canPerform(ExileRule.DAMAGE_REINFORCEMENT)).thenReturn(false);
		dut.onReinforcementDamage(e);
		assertTrue(e.isCancelled());
		
		when(rein.canBypass(player)).thenReturn(true);
		e = new ReinforcementDamageEvent(rein, player, null);
		when(config.canPerform(ExileRule.DAMAGE_REINFORCEMENT)).thenReturn(false);
		dut.onReinforcementDamage(e);
		assertFalse(e.isCancelled());
	}

	@Test
	public void testOnAcidBlockEvent() {
		AcidBlockEvent e = new AcidBlockEvent(player, null, null);
		when(config.canPerform(ExileRule.DAMAGE_REINFORCEMENT)).thenReturn(true);
		dut.onAcidBlockEvent(e);
		assertFalse(e.isCancelled());

		e = new AcidBlockEvent(player, null, null);
		when(config.canPerform(ExileRule.DAMAGE_REINFORCEMENT)).thenReturn(false);
		dut.onAcidBlockEvent(e);
		assertFalse(e.isCancelled());

		e = new AcidBlockEvent(player, null, null);
		when(config.canPerform(ExileRule.DAMAGE_REINFORCEMENT)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onAcidBlockEvent(e);
		assertFalse(e.isCancelled());
		
		e = new AcidBlockEvent(player, null, null);
		when(config.canPerform(ExileRule.DAMAGE_REINFORCEMENT)).thenReturn(false);
		dut.onAcidBlockEvent(e);
		assertTrue(e.isCancelled());
	}
}
