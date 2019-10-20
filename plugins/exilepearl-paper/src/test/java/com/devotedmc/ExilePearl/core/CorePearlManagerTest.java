package com.devotedmc.ExilePearl.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PearlFreeReason;
import com.devotedmc.ExilePearl.LoreProvider;
import com.devotedmc.ExilePearl.PlayerProvider;
import com.devotedmc.ExilePearl.StorageProvider;
import com.devotedmc.ExilePearl.config.PearlConfig;
import com.devotedmc.ExilePearl.event.PearlDecayEvent;
import com.devotedmc.ExilePearl.event.PearlDecayEvent.DecayAction;
import com.devotedmc.ExilePearl.event.PlayerFreedEvent;
import com.devotedmc.ExilePearl.event.PlayerPearledEvent;
import com.devotedmc.ExilePearl.holder.PearlHolder;
import com.devotedmc.ExilePearl.holder.PlayerHolder;
import com.devotedmc.ExilePearl.storage.PluginStorage;
import com.devotedmc.testbukkit.TestBukkitRunner;

@RunWith(TestBukkitRunner.class)
public class CorePearlManagerTest {

	private ExilePearlApi pearlApi;
	private PearlFactory pearlFactory;
	private PluginStorage storage;
	private PearlConfig config;
	private CorePearlManager manager;
	private StorageProvider storageProvider;

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
		when(player.isOnline()).thenReturn(true);
		when(player.getLocation()).thenReturn(new Location(mock(World.class), 0, 0, 0));

		killer = mock(Player.class);
		when(killer.getUniqueId()).thenReturn(killerId);
		when(killer.getName()).thenReturn(killerName);
		when(killer.isOnline()).thenReturn(true);
		when(killer.getLocation()).thenReturn(new Location(mock(World.class), 0, 0, 0));

		pearlApi = mock(ExilePearlApi.class);
		when(pearlApi.getPlayer(playerName)).thenReturn(player);
		when(pearlApi.getPlayer(playerId)).thenReturn(player);
		when(pearlApi.getPlayer(killerName)).thenReturn(killer);
		when(pearlApi.getPlayer(killerId)).thenReturn(killer);

		PlayerProvider nameProvider = mock(PlayerProvider.class);
		when(nameProvider.getRealPlayerName(playerId)).thenReturn(playerName);
		when(nameProvider.getRealPlayerName(killerId)).thenReturn(killerName);

		pearlFactory = mock(PearlFactory.class);

		when(pearlFactory.createExilePearl(any(UUID.class), any(Player.class), anyInt())).then(new Answer<ExilePearl>() {

			@Override
			public ExilePearl answer(InvocationOnMock invocation) throws Throwable {

				try {
					UUID uid1 = (UUID)invocation.getArguments()[0];
					Player p2 = (Player)invocation.getArguments()[1];
					int pearlId = (int)invocation.getArguments()[2];
					return new MockPearl(nameProvider, uid1, p2.getUniqueId(), pearlId, p2.getLocation());
				} catch (Exception ex) {
					return null;
				}
			}
		});

		when(pearlFactory.createExilePearl(any(UUID.class), any(UUID.class), anyInt(), any(PearlHolder.class))).then(new Answer<ExilePearl>() {

			@Override
			public ExilePearl answer(InvocationOnMock invocation) throws Throwable {

				try {
					UUID uid1 = (UUID)invocation.getArguments()[0];
					UUID uid2 = (UUID)invocation.getArguments()[1];
					int pearlId = (int)invocation.getArguments()[2];
					PearlHolder holder = (PearlHolder)invocation.getArguments()[3];
					return new MockPearl(nameProvider, uid1, uid2, pearlId, holder.getLocation());
				} catch (Exception ex) {
					return null;
				}
			}
		});

		storage = mock(PluginStorage.class);
		config = mock(PearlConfig.class);

		when(pearlApi.getPearlConfig()).thenReturn(config);

		final PluginManager pluginManager = Bukkit.getPluginManager();
		reset(pluginManager);
		doNothing().when(pluginManager).callEvent(any(Event.class));

		storageProvider = mock(StorageProvider.class);
		when(storageProvider.getStorage()).thenReturn(storage);

		manager = new CorePearlManager(pearlApi, pearlFactory, storageProvider);
	}

	@Test
	public void testCorePearlManager() {
		// Null arguments throw exceptions
		Throwable e = null;
		try { new CorePearlManager(null, pearlFactory, storageProvider); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);

		e = null;
		try { new CorePearlManager(pearlApi, null, storageProvider); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);

		e = null;
		try { new CorePearlManager(pearlApi, pearlFactory, null); } catch (Throwable ex) { e = ex; }
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
		final PluginManager pluginManager = Bukkit.getPluginManager();
		reset(pluginManager);

		when(config.getPearlHealthStartValue()).thenReturn(55);

		// Null arguments throw exceptions
		Throwable e = null;
		try { manager.exilePlayer(null, killer); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);

		e = null;
		try { manager.exilePlayer(player.getUniqueId(), null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);

		e = null;
		try { manager.exilePlayer(player.getUniqueId(), killer.getUniqueId(), (Location)null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);

		e = null;
		try { manager.exilePlayer(player.getUniqueId(), killer.getUniqueId(), (PearlHolder)null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);

		// This will cancel the new pearl event
	    doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				PlayerPearledEvent event;

				if (invocation.getArguments()[0] instanceof PlayerPearledEvent) {
					event = (PlayerPearledEvent)invocation.getArguments()[0];
				} else {
					return null;
				}

				// Cancel the event
				event.setCancelled(true);
				return null;
			}
		}).when(pluginManager).callEvent(any(Event.class));

	    // The pearl creation should fail
		ExilePearl pearl = manager.exilePlayer(player.getUniqueId(), killer);
		assertNull(pearl);

		// Now allow the event to pass
		reset(pluginManager);

		// Now it should succeed
		pearl = manager.exilePlayer(player.getUniqueId(), killer);
		assertNotNull(pearl);
		assertTrue(manager.isPlayerExiled(player));
		assertEquals(pearl.getPlayerId(), player.getUniqueId());
		assertEquals(pearl.getHealth(), 55);

		ArgumentCaptor<PlayerPearledEvent> eventArg = ArgumentCaptor.forClass(PlayerPearledEvent.class);
		verify(pluginManager).callEvent(eventArg.capture());
		assertEquals(eventArg.getValue().getPearl(), pearl);
	}

	@Test
	public void testFreePearl() {
		assertFalse(manager.isPlayerExiled(player));
		ExilePearl pearl = manager.exilePlayer(player.getUniqueId(), killer);
		assertTrue(manager.isPlayerExiled(player));
		assertNotNull(pearl);
		final PluginManager pluginManager = Bukkit.getPluginManager();
		reset(pluginManager);

		// Null arguments throw exceptions
		Throwable e = null;
		try { manager.freePearl(null, PearlFreeReason.FREED_BY_PLAYER); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);

		e = null;
		try { manager.freePearl(pearl, null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);

		// This will cancel the free pearl event
	    doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				PlayerFreedEvent event;

				if (invocation.getArguments()[0] instanceof PlayerFreedEvent) {
					event = (PlayerFreedEvent)invocation.getArguments()[0];
				} else {
					return null;
				}

				// Cancel the event
				event.setCancelled(true);
				return null;
			}
		}).when(pluginManager).callEvent(any(Event.class));

	    // Freeing the pearl should fail
	    assertFalse(manager.freePearl(pearl, PearlFreeReason.FREED_BY_PLAYER));
		assertTrue(manager.isPlayerExiled(player));

		// Now allow the event to pass
		reset(pluginManager);

	    assertTrue(manager.freePearl(pearl, PearlFreeReason.FREED_BY_PLAYER));
	    assertFalse(manager.isPlayerExiled(player));

		ArgumentCaptor<PlayerFreedEvent> eventArg = ArgumentCaptor.forClass(PlayerFreedEvent.class);
		verify(pluginManager).callEvent(eventArg.capture());
		assertEquals(eventArg.getValue().getPearl(), pearl);
	}

	@Test
	public void testGetPearl() {
		assertNull(manager.getPearl(player.getName()));
		assertNull(manager.getPearl(player.getUniqueId()));

		ExilePearl pearl = manager.exilePlayer(player.getUniqueId(), killer);

		assertEquals(pearl, manager.getPearl(player.getName()));
		assertEquals(pearl, manager.getPearl(player.getUniqueId()));
	}

	@Test
	public void testIsPlayerExiled() {
		assertFalse(manager.isPlayerExiled(player));
		assertFalse(manager.isPlayerExiled(player.getUniqueId()));

		manager.exilePlayer(player.getUniqueId(), killer);

		assertTrue(manager.isPlayerExiled(player));
		assertTrue(manager.isPlayerExiled(player.getUniqueId()));
	}

	@Test
	public void testGetPearlFromItemStack() {
		ExilePearl pearl = manager.exilePlayer(player.getUniqueId(), killer);
		ItemStack is = pearl.createItemStack();

		// Create mock lore generator
		LoreProvider loreGenerator = mock(LoreProvider.class);
		when(pearlApi.getLoreProvider()).thenReturn(loreGenerator);

		// Test fails when lore generator fails
		when(loreGenerator.getPearlIdFromItemStack(is)).thenReturn(0);
		assertNull(manager.getPearlFromItemStack(is));

		// Test passes when lore generator succeeds
		when(loreGenerator.getPearlIdFromItemStack(is)).thenReturn(pearl.getPearlId());
		assertEquals(manager.getPearlFromItemStack(is), pearl);

		// Test fails when the pearl is freed
		assertTrue(manager.freePearl(pearl, PearlFreeReason.FREED_BY_PLAYER));
		assertNull(manager.getPearlFromItemStack(is));

		when(loreGenerator.getPearlIdFromItemStack(is)).thenReturn(1);
		assertNull(manager.getPearlFromItemStack(is));
	}

	/**
	 * Tests converting a legacy prison pearl to an exile pearl
	 */
	@Test
	public void testLegacyGetPearlFromItemStack() {
		UUID legacyId = UUID.randomUUID();
		String legacyName = "Killer";

		ItemStack is = mock(ItemStack.class);

		// Create mock lore generator
		LoreProvider loreGenerator = mock(LoreProvider.class);
		when(pearlApi.getLoreProvider()).thenReturn(loreGenerator);

		manager.exilePlayer(player.getUniqueId(), killer);

		// Test if legacy pearl has same ID as an already pearled player
		when(loreGenerator.getPlayerIdFromLegacyPearl(is)).thenReturn(playerId);

		ExilePearl legacyPearl = manager.getPearlFromItemStack(is);
		assertNotNull(legacyPearl);
		assertEquals(playerId, legacyPearl.getPlayerId());
		assertEquals(legacyName, legacyPearl.getKillerName());

		// Now try to parse out an un-pearled player
		when(loreGenerator.getPlayerIdFromLegacyPearl(is)).thenReturn(legacyId);

		Player legacyPlayer = mock(Player.class);
		when(legacyPlayer.getUniqueId()).thenReturn(legacyId);
		when(pearlApi.getPlayer(legacyId)).thenReturn(legacyPlayer);

		legacyPearl = manager.getPearlFromItemStack(is);
		assertNull(legacyPearl);
		manager.exilePlayer(legacyId, killer.getUniqueId(), new PlayerHolder(killer));

		legacyPearl = manager.getPearlFromItemStack(is);
		assertNotNull(legacyPearl);
		assertEquals(legacyId, legacyPearl.getPlayerId());
	}


	@Test
	public void testDecayPearls() {
		final PluginManager pluginManager = Bukkit.getPluginManager();
		ArgumentCaptor<PearlDecayEvent> eventArg = ArgumentCaptor.forClass(PearlDecayEvent.class);
		reset(pluginManager);

		when(config.getPearlHealthStartValue()).thenReturn(10);
		when(config.getPearlHealthDecayAmount()).thenReturn(1);
		when(config.getPearlHealthDecayTimeout()).thenReturn(20000);

		ExilePearl pearl1 = manager.exilePlayer(player.getUniqueId(), killer);
		assertEquals(pearl1.getHealth(), 10);

		reset(pluginManager);

		// This will cancel the decay pearl event
	    doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Cancellable event;

				if (invocation.getArguments()[0] instanceof Cancellable) {
					event = (Cancellable)invocation.getArguments()[0];
				} else {
					return null;
				}

				// Cancel the event
				event.setCancelled(true);
				return null;
			}
		}).when(pluginManager).callEvent(any(PearlDecayEvent.class));

		manager.decayPearls();
		verify(pluginManager).callEvent(eventArg.capture());
		assertEquals(eventArg.getValue().getAction(), DecayAction.START);

		// Health should be unchanged
		assertEquals(pearl1.getHealth(), 10);

		// Now allow the event to pass
		reset(pluginManager);

		manager.decayPearls();

		// Event should be called for decay start and complete
		verify(pluginManager, times(2)).callEvent(eventArg.capture());
		assertEquals(eventArg.getAllValues().get(1).getAction(), DecayAction.START);
		assertEquals(eventArg.getAllValues().get(2).getAction(), DecayAction.COMPLETE);

		// Health should be lowered
		assertEquals(pearl1.getHealth(), 9);

		// Now add a second pearl
		ExilePearl pearl2 = manager.exilePlayer(killer.getUniqueId(), player);
		assertEquals(pearl2.getHealth(), 10);

		manager.decayPearls();

		assertEquals(pearl1.getHealth(), 8);
		assertEquals(pearl2.getHealth(), 9);

		when(config.getPearlHealthDecayAmount()).thenReturn(3);
		manager.decayPearls();

		assertEquals(pearl1.getHealth(), 5);
		assertEquals(pearl2.getHealth(), 6);

		when(config.getPearlHealthDecayAmount()).thenReturn(1);
		manager.decayPearls();
		manager.decayPearls();
		manager.decayPearls();
		manager.decayPearls();
		assertEquals(pearl1.getHealth(), 1);
		assertEquals(pearl2.getHealth(), 2);

		assertTrue(manager.isPlayerExiled(player));
		assertTrue(manager.isPlayerExiled(killer));
		manager.decayPearls();
		assertEquals(pearl1.getHealth(), 0);
		assertEquals(pearl2.getHealth(), 1);

		// Pearl 1 should be freed
		assertFalse(manager.isPlayerExiled(player));
		assertTrue(manager.isPlayerExiled(killer));

		manager.decayPearls();

		// Both should be freed now
		assertFalse(manager.isPlayerExiled(player));
		assertFalse(manager.isPlayerExiled(killer));
	}

}
