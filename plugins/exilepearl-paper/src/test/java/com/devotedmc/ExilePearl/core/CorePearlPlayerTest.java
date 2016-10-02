package com.devotedmc.ExilePearl.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlAccess;
import com.devotedmc.ExilePearl.PearlPlayer;
import com.devotedmc.ExilePearl.PlayerProvider;

public class CorePearlPlayerTest {

	private Player p1;
	private Player p2;
	private Player p3;
	
	private PlayerProvider nameProvider;
	private PearlAccess pearlAccess;
	
	private CorePearlPlayer pp1;
	private CorePearlPlayer pp2;
	private CorePearlPlayer pp3;
	
	
	@Before
	public void setUp() throws Exception {
		
		nameProvider = mock(PlayerProvider.class);
		pearlAccess = mock(PearlAccess.class);
		
		p1 = createMockPlayer("Player1");
		p2 = createMockPlayer("Player2");
		p3 = createMockPlayer("Player3");
		
		pp1 = new CorePearlPlayer(p1.getUniqueId(), nameProvider, pearlAccess);
		pp2 = new CorePearlPlayer(p2.getUniqueId(), nameProvider, pearlAccess);
		pp3 = new CorePearlPlayer(p3.getUniqueId(), nameProvider, pearlAccess);
		
		when(pp1.getPlayer()).thenReturn(p1);
		when(pp2.getPlayer()).thenReturn(p2);
		when(pp3.getPlayer()).thenReturn(p3);
		
	}

	@Test
	public void testCorePearlPlayer() {
		// Null arguments throw exceptions
		Throwable e = null;
		try { new CorePearlPlayer(null, nameProvider, pearlAccess); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { new CorePearlPlayer(p1.getUniqueId(), null, pearlAccess); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { new CorePearlPlayer(p1.getUniqueId(), nameProvider, null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testMsg() {
		pp1.msg("test %d", 5);
		verify(p1, times(0)).sendMessage(anyString());
		
		when(p1.isOnline()).thenReturn(true);
		
		pp1.msg("test %d", 5);
		verify(p1).sendMessage("test 5");
	}

	@Test
	public void testGetBcastPlayers() {
		Set<PearlPlayer> bcastPlayers = pp1.getBcastPlayers();
		assertNotNull(bcastPlayers);
		
		// Collection should be unmodifiable
		Throwable e = null;
		try { bcastPlayers.add(mock(PearlPlayer.class)); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof UnsupportedOperationException);
	}

	@Test
	public void testAddBcastPlayer() {
		
		assertEquals(pp1.getBcastPlayers().size(), 0);
		
		pp1.addBcastPlayer(pp2);
		assertTrue(pp1.getBcastPlayers().contains(pp2));
		assertEquals(pp1.getBcastPlayers().size(), 1);
		
		pp1.addBcastPlayer(pp3);
		assertTrue(pp1.getBcastPlayers().contains(pp3));
		assertEquals(pp1.getBcastPlayers().size(), 2);
	}

	@Test
	public void testRequestedBcastPlayer() {
		assertNull(pp1.getRequestedBcastPlayer());
		
		pp1.setRequestedBcastPlayer(pp2);
		assertEquals(pp1.getRequestedBcastPlayer(), pp2);

		pp1.setRequestedBcastPlayer(null);
		assertNull(pp1.getRequestedBcastPlayer());
	}

	@Test
	public void testIsExiled() {
		assertFalse(pp1.isExiled());
		
		when(pearlAccess.isPlayerExiled(pp1.getUniqueId())).thenReturn(true);
		assertTrue(pp1.isExiled());
	}

	@Test
	public void testGetExilePearl() {
		assertNull(pp1.getExilePearl());
		
		ExilePearl pearl = mock(ExilePearl.class);
		when(pearlAccess.getPearl(pp1.getUniqueId())).thenReturn(pearl);
		assertEquals(pp1.getExilePearl(), pearl);
	}
	
	
	private Player createMockPlayer(String name) {
		final UUID id = UUID.randomUUID();
		final Player p = mock(Player.class);
		when(p.getName()).thenReturn(name);
		when(p.getUniqueId()).thenReturn(id);

		when(nameProvider.getName(id)).thenReturn(name);
		when(nameProvider.getUniqueId(name)).thenReturn(id);
		
		return p;
	}

}
