package com.devotedmc.ExilePearl.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.PearlConfig;
import com.devotedmc.ExilePearl.PearlLoreProvider;
import com.devotedmc.ExilePearl.PearlPlayer;
import com.devotedmc.ExilePearl.PlayerProvider;
import com.devotedmc.ExilePearl.Util.BukkitTestCase;
import com.devotedmc.ExilePearl.event.PearlMovedEvent;
import com.devotedmc.ExilePearl.holder.HolderVerifyResult;
import com.devotedmc.ExilePearl.holder.PearlHolder;
import com.devotedmc.ExilePearl.holder.PlayerHolder;
import com.devotedmc.ExilePearl.storage.PearlUpdateStorage;

public class CoreExilePearlTest extends BukkitTestCase {
	
	private CoreExilePearl pearl;
	private PearlUpdateStorage storage;
	private final String playerName = "Player";
	private final UUID playerId = UUID.randomUUID();
	private Player player;
	
	private final String killerName = "Killer";
	private final UUID killerId = UUID.randomUUID();
	private Player killer;
	private PearlHolder holder;
	
	private PearlConfig pearlConfig;
	private ExilePearlApi pearlApi;
	private PlayerProvider nameProvider;
	private PearlLoreProvider loreGenerator;
	

	@Before
	public void setUp() throws Exception {
		
		World world = mock(World.class);
		when(world.getName()).thenReturn("world");
		
		pearlApi = mock(ExilePearlApi.class);
		storage = mock(PearlUpdateStorage.class);
		player = mock(Player.class);
		when(player.getName()).thenReturn("Player");
		when(player.getUniqueId()).thenReturn(playerId);
		killer = mock(Player.class);
		when(killer.getName()).thenReturn("Killer");
		when(killer.getUniqueId()).thenReturn(killerId);
		when(player.getLocation()).thenReturn(new Location(world, 0, 1, 2));
		when(killer.getLocation()).thenReturn(new Location(world, 10, 20, 30));
		
		nameProvider = mock(PlayerProvider.class);
		when(nameProvider.getName(player.getUniqueId())).thenReturn(playerName);
		when(nameProvider.getName(killer.getUniqueId())).thenReturn(killerName);
		when(nameProvider.getUniqueId(playerName)).thenReturn(playerId);
		when(nameProvider.getUniqueId(killerName)).thenReturn(killerId);
		
		pearlConfig = mock(PearlConfig.class);
		when(pearlConfig.getPearlHealthMaxValue()).thenReturn(100);
		
		PearlPlayer p1 = new CorePearlPlayer(playerId, nameProvider, pearlApi);
		when(p1.getPlayer()).thenReturn(player);
		PearlPlayer p2 = new CorePearlPlayer(killerId, nameProvider, pearlApi);
		when(p2.getPlayer()).thenReturn(killer);
		
		when(pearlApi.getPearlPlayer(playerName)).thenReturn(p1);
		when(pearlApi.getPearlPlayer(playerId)).thenReturn(p1);
		when(pearlApi.getPearlPlayer(killerName)).thenReturn(p2);
		when(pearlApi.getPearlPlayer(killerId)).thenReturn(p2);
		when(pearlApi.getPearlConfig()).thenReturn(pearlConfig);
		
		loreGenerator = mock(PearlLoreProvider.class);
		when(pearlApi.getLoreGenerator()).thenReturn(loreGenerator);
		
		holder = new PlayerHolder(killer);
		
		pearl = new CoreExilePearl(pearlApi, storage, player.getUniqueId(), killer.getUniqueId(), 1, holder);
	}

	@Test
	public void testCoreExilePearl() {
		// Null arguments throw exceptions
		Throwable e = null;
		try { new CoreExilePearl(null, storage, player.getUniqueId(), killer.getUniqueId(), 1, new PlayerHolder(killer)); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { new CoreExilePearl(pearlApi, null, player.getUniqueId(), killer.getUniqueId(), 1, new PlayerHolder(killer)); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { new CoreExilePearl(pearlApi, storage, null, killer.getUniqueId(), 1, new PlayerHolder(killer)); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { new CoreExilePearl(pearlApi, storage, player.getUniqueId(), null, 1, new PlayerHolder(killer)); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { new CoreExilePearl(pearlApi, storage, player.getUniqueId(), killer.getUniqueId(), 1, null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testGetUniqueId() {
		assertEquals(pearl.getPlayerId(), player.getUniqueId());
	}

	@Test
	public void testGetPlayer() {		
		assertEquals(pearl.getPlayer().getName(), player.getName());
		assertEquals(pearl.getPlayer().getUniqueId(), player.getUniqueId());
	}

	@Test
	public void testGetSetPearledOn() {
		Date now = new Date();
		
		// Null arg throws exception
		Throwable e = null;
		try { pearl.setPearledOn(null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		// Can't modify invalid pearl
		e = null;
		pearl.enableStorage();
		try { pearl.setPearledOn(now); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof RuntimeException);

		// Set pearl valid
		when(pearlApi.isPlayerExiled(playerId)).thenReturn(true);
		
		pearl.setPearledOn(now);
		assertEquals(pearl.getPearledOn(), now);
	}

	@Test
	public void testGetPlayerName() {		
		assertEquals(pearl.getPlayerName(), player.getName());
	}

	@Test
	public void testGetSetHolder() {
		assertEquals(pearl.getHolder(), holder);

		PearlPlayer pPlayer = new CorePearlPlayer(player.getUniqueId(), nameProvider, pearlApi);
		
		pearl.enableStorage();

		// Can't modify invalid pearl
		Throwable e = null;
		try { pearl.setHolder(pPlayer); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof RuntimeException);

		// Set pearl valid
		when(pearlApi.isPlayerExiled(playerId)).thenReturn(true);
		
		pearl.setHolder(pPlayer);
		assertEquals(pearl.getHolder().getName(), pPlayer.getName());
	}

	@Test
	public void testSetHolderBlockLocation() {
		//Location l1 = mock(Location.class);
		Location l2 = mock(Location.class);
		Block b = mock(Block.class);
		when(b.getLocation()).thenReturn(l2);
		when(b.getType()).thenReturn(Material.CHEST);

		pearl.enableStorage();
		
		// Can't modify invalid pearl
		Throwable e = null;
		try { pearl.setHolder(b); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof RuntimeException);

		// Set pearl valid
		when(pearlApi.isPlayerExiled(playerId)).thenReturn(true);
		
		pearl.enableStorage();
		pearl.setHolder(b);
		assertEquals(pearl.getLocation(), l2);
		assertEquals(pearl.getHolder().getLocation(), l2);
		verify(storage).pearlUpdateLocation(pearl);
		
		// Null arg throws exception
		e = null;
		try { pearl.setHolder((PearlHolder)null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { pearl.setHolder((PlayerHolder)null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { pearl.setHolder((Block)null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { pearl.setHolder((Item)null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testGetSetHealth() {
		assertEquals(pearl.getHealth(), 10, 0);
		
		pearl.enableStorage();
		
		// Can't modify invalid pearl
		Throwable e = null;
		try { pearl.setHealth(0); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof RuntimeException);
		
		// Set pearl valid
		when(pearlApi.isPlayerExiled(playerId)).thenReturn(true);
		
		pearl.enableStorage();
		pearl.setHealth(0);
		verify(storage, times(1)).pearlUpdateHealth(pearl);
		
		pearl.setHealth(-10);
		assertEquals(pearl.getHealth(), 0, 0);
		verify(storage, times(2)).pearlUpdateHealth(pearl);
		
		pearl.setHealth(90);
		assertEquals(pearl.getHealth(), 90, 0);
		verify(storage, times(3)).pearlUpdateHealth(pearl);
		
		pearl.setHealth(100);
		assertEquals(pearl.getHealth(), 100, 0);
		verify(storage, times(4)).pearlUpdateHealth(pearl);
		
		pearl.setHealth(110);
		assertEquals(pearl.getHealth(), 100, 0);
		assertEquals(pearl.getHealthPercent(), 100, 0);
		verify(storage, times(5)).pearlUpdateHealth(pearl);
		
		// Health percent changes with max health value change
		when(pearlConfig.getPearlHealthMaxValue()).thenReturn(1000);
		assertEquals(pearl.getHealthPercent(), 10, 0);
	}

	@Test
	public void testGetItemName() {
		assertEquals(pearl.getItemName(), "Exile Pearl");
	}

	@Test
	public void testGetKillerUniqueId() {
		assertEquals(pearl.getKillerUniqueId(), killerId);
	}

	@Test
	public void testGetKillerName() {
		assertEquals(pearl.getKillerName(), killerName);
	}

	@Test
	public void testGetLocationDescription() {
		World world = mock(World.class);
		when(world.getName()).thenReturn("world");
		Location l = new Location(world, 1, 2, 3);
		Block b = mock(Block.class);
		when(b.getLocation()).thenReturn(l);
		when(b.getType()).thenReturn(Material.CHEST);

		when(pearlApi.isPlayerExiled(playerId)).thenReturn(true);
		pearl.setHolder(b);
		assertEquals(pearl.getLocationDescription(), "held by a chest at world 1 2 3");
		
		Item item = mock(Item.class);
		when(item.getLocation()).thenReturn(l);
		
		pearl.setHolder(item);
		assertEquals(pearl.getLocationDescription(), "held by nobody at world 1 2 3");
	}

	@Test
	public void testGetSetFreedOffline() {
		assertFalse(pearl.getFreedOffline());
		pearl.enableStorage();
		
		// Can't modify invalid pearl
		Throwable e = null;
		try { pearl.setFreedOffline(true); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof RuntimeException);

		// Set pearl valid
		when(pearlApi.isPlayerExiled(playerId)).thenReturn(true);
		
		pearl.setFreedOffline(true);
		assertTrue(pearl.getFreedOffline());
		verify(storage).pearlUpdateFreedOffline(pearl);

		pearl.setFreedOffline(false);
		assertFalse(pearl.getFreedOffline());
	}

	@Test
	public void testItemStack() {
	    
		when(loreGenerator.generateLore(pearl)).thenReturn(new LinkedList<String>());
		
		ItemStack is = pearl.createItemStack();

		// Positive test
		when(loreGenerator.getPearlIdFromItemStack(is)).thenReturn(pearl.getPearlId());
		assertTrue(pearl.validateItemStack(is));
		
		// Negative test
		when(loreGenerator.getPearlIdFromItemStack(is)).thenReturn(0);
		assertFalse(pearl.validateItemStack(is));
		
		// Null arg throws exception
		Throwable e = null;
		try { pearl.validateItemStack(null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testVerifyLocation() {
		PearlHolder holder1 = mock(PearlHolder.class);
		when(holder1.validate(any(ExilePearl.class))).thenReturn(HolderVerifyResult.IN_CHEST);
		when(pearlApi.isPlayerExiled(playerId)).thenReturn(true);
		reset(Bukkit.getPluginManager());
		
		pearl.setHolder(holder1);
		assertTrue(pearl.verifyLocation());
		
		ArgumentCaptor<PearlMovedEvent> eventArg = ArgumentCaptor.forClass(PearlMovedEvent.class);
		verify(Bukkit.getPluginManager()).callEvent(eventArg.capture());
		assertEquals(eventArg.getValue().getPearl(), pearl);
		
		pearl.setHolder(holder1);
		assertTrue(pearl.verifyLocation());
		
		pearl.setHolder(holder1);
		assertTrue(pearl.verifyLocation());
		
		pearl.setHolder(holder1);
		assertTrue(pearl.verifyLocation());
		
		pearl.setHolder(holder1);
		assertTrue(pearl.verifyLocation());
		
		PearlHolder holder2 = mock(PearlHolder.class);
		when(holder2.validate(any(ExilePearl.class))).thenReturn(HolderVerifyResult.DEFAULT);
		
		PearlHolder holder3 = mock(PearlHolder.class);
		when(holder3.validate(any(ExilePearl.class))).thenReturn(HolderVerifyResult.DEFAULT);
		
		PearlHolder holder4 = mock(PearlHolder.class);
		when(holder4.validate(any(ExilePearl.class))).thenReturn(HolderVerifyResult.DEFAULT);
		
		PearlHolder holder5 = mock(PearlHolder.class);
		when(holder5.validate(any(ExilePearl.class))).thenReturn(HolderVerifyResult.DEFAULT);
		
		PearlHolder holder6 = mock(PearlHolder.class);
		when(holder6.validate(any(ExilePearl.class))).thenReturn(HolderVerifyResult.DEFAULT);
		
		pearl.setHolder(holder2);
		assertTrue(pearl.verifyLocation());
		
		pearl.setHolder(holder3);
		assertTrue(pearl.verifyLocation());
		
		pearl.setHolder(holder4);
		assertTrue(pearl.verifyLocation());
		
		pearl.setHolder(holder5);
		assertTrue(pearl.verifyLocation());
		
		pearl.setHolder(holder6);
		assertFalse(pearl.verifyLocation());
	}
}
