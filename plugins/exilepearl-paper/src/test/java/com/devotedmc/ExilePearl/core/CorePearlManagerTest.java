package com.devotedmc.ExilePearl.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.ExilePearlConfig;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PearlPlayer;
import com.devotedmc.ExilePearl.event.ExilePearlEvent;
import com.devotedmc.ExilePearl.storage.PearlStorage;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Bukkit.class)
public class CorePearlManagerTest {
	
	private ExilePearlApi pearlApi;
	private PearlFactory pearlFactory;
	private PearlStorage storage;
	private ExilePearlConfig config;
	private CorePearlManager manager;
	private PluginManager pluginManager;
	
	private final String playerName = "Player";
	private final UUID playerId = UUID.randomUUID();
	private final String killerName = "Killer";
	private final UUID killerId = UUID.randomUUID();
	private Player player;
	private Player killer;
	
	
	@Before
	public void setUp() throws Exception {
		
		player = mock(Player.class);
		when(player.getUniqueId()).thenReturn(playerId);
		when(player.getName()).thenReturn(playerName);
		PearlPlayer pPlayer = mock(PearlPlayer.class);
		when(pPlayer.getUniqueId()).thenReturn(playerId);
		when(pPlayer.getName()).thenReturn(playerName);
		
		killer = mock(Player.class);
		when(killer.getUniqueId()).thenReturn(killerId);
		when(killer.getName()).thenReturn(killerName);
		PearlPlayer pKiller = mock(PearlPlayer.class);
		when(pKiller.getUniqueId()).thenReturn(killerId);
		when(pKiller.getName()).thenReturn(killerName);
		
		pearlApi = mock(ExilePearlApi.class);
		when(pearlApi.getPearlPlayer(playerName)).thenReturn(pPlayer);
		when(pearlApi.getPearlPlayer(playerId)).thenReturn(pPlayer);
		when(pearlApi.getPearlPlayer(killerName)).thenReturn(pKiller);
		when(pearlApi.getPearlPlayer(killerId)).thenReturn(pPlayer);
		
		pearlFactory = new MockPearlFactory();
		
		storage = mock(PearlStorage.class);
		config = mock(ExilePearlConfig.class);
		manager = new CorePearlManager(pearlApi, pearlFactory, storage, config);
		
	    PowerMockito.mockStatic(Bukkit.class);
	    pluginManager = mock(PluginManager.class);
	    when(Bukkit.getPluginManager()).thenReturn(pluginManager);
	}

	@Test
	public void testCorePearlManager() {
		// Null arguments throw exceptions
		Throwable e = null;
		try { new CorePearlManager(null, pearlFactory, storage, config); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { new CorePearlManager(pearlApi, null, storage, config); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { new CorePearlManager(pearlApi, pearlFactory, null, config); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { new CorePearlManager(pearlApi, pearlFactory, storage, null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testLoadPearls() {		
		Collection<ExilePearl> pearls = manager.getPearls();
		assertEquals(pearls.size(), 0);
		
		// Collection should be unmodifiable
		Throwable e = null;
		try { pearls.clear(); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof UnsupportedOperationException);
		
		Collection<ExilePearl> pearlsToLoad = new HashSet<ExilePearl>();
		when(storage.loadAllPearls()).thenReturn(pearlsToLoad);
		
		manager.loadPearls();
		verify(storage).loadAllPearls();
		assertEquals(pearls.size(), 0);
		
		pearlsToLoad.add(mock(ExilePearl.class));

		manager.loadPearls();
		pearls = manager.getPearls();
		assertEquals(pearls.size(), 1);
	}

	@Test
	public void testExilePlayer() {
		assertFalse(manager.isPlayerExiled(player));
		
		ExilePearl pearl = manager.exilePlayer(player, killer);
		assertNotNull(pearl);
		assertFalse(manager.isPlayerExiled(player));
		assertEquals(pearl.getUniqueId(), player.getUniqueId());
		assertEquals(pearl.getPlayerName(), player.getName());
		verify(pluginManager).callEvent(any(ExilePearlEvent.class));
	}

	@Test
	public void testFreePearl() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPearlString() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPearlUUID() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetInventoryExilePearls() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetInventoryPearlStacks() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsPlayerExiledPlayer() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsPlayerExiledUUID() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPearlFromItemStack() {
		fail("Not yet implemented");
	}

	@Test
	public void testDecayPearls() {
		fail("Not yet implemented");
	}

}
