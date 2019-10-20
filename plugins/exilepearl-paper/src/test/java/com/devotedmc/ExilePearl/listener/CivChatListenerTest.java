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

import vg.civcraft.mc.civchat2.event.GlobalChatEvent;

public class CivChatListenerTest {

	private ExilePearlApi pearlApi;
	private PearlConfig config;
	private CivChatListener dut;

	final UUID uid = UUID.randomUUID();
	final Player player = mock(Player.class);

	@Before
	public void setUp() throws Exception {
		config = mock(PearlConfig.class);

		pearlApi = mock(ExilePearlApi.class);
		when(pearlApi.getPearlConfig()).thenReturn(config);

		dut = new CivChatListener(pearlApi);

		when(player.getUniqueId()).thenReturn(uid);
	}

	@Test
	public void testCivChatListener() {
		// Null arguments throw exceptions
		Throwable e = null;
		try { new CivChatListener(null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testOnAcidBlockEvent() {
		GlobalChatEvent e = new GlobalChatEvent(player, "", "");
		when(config.canPerform(ExileRule.CHAT)).thenReturn(true);
		dut.onChatEvent(e);
		assertFalse(e.isCancelled());

		e = new GlobalChatEvent(player, "", "");
		when(config.canPerform(ExileRule.CHAT)).thenReturn(false);
		dut.onChatEvent(e);
		assertFalse(e.isCancelled());

		e = new GlobalChatEvent(player, "", "");
		when(config.canPerform(ExileRule.CHAT)).thenReturn(true);
		when(pearlApi.isPlayerExiled(uid)).thenReturn(true);
		dut.onChatEvent(e);
		assertFalse(e.isCancelled());

		e = new GlobalChatEvent(player, "", "");
		when(config.canPerform(ExileRule.CHAT)).thenReturn(false);
		dut.onChatEvent(e);
		assertTrue(e.isCancelled());
	}
}
