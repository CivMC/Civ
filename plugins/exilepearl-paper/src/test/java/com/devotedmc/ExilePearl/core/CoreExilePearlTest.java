package com.devotedmc.ExilePearl.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.PearlPlayer;
import com.devotedmc.ExilePearl.PlayerNameProvider;
import com.devotedmc.ExilePearl.command.CmdExilePearl;
import com.devotedmc.ExilePearl.holder.HolderVerifyResult;
import com.devotedmc.ExilePearl.holder.PearlHolder;
import com.devotedmc.ExilePearl.holder.PlayerHolder;
import com.devotedmc.ExilePearl.storage.PearlUpdateStorage;
import com.devotedmc.ExilePearl.util.PearlLoreUtil;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Bukkit.class)
public class CoreExilePearlTest {
	
	private CoreExilePearl pearl;
	private PearlUpdateStorage storage;
	private final String playerName = "Player";
	private final UUID playerId = UUID.randomUUID();
	private Player player;
	
	private final String killerName = "Killer";
	private final UUID killerId = UUID.randomUUID();
	private Player killer;
	private PearlHolder holder;
	
	private ExilePearlApi pearlApi;
	private PlayerNameProvider nameProvider;
	

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
		
		nameProvider = mock(PlayerNameProvider.class);
		when(nameProvider.getName(player.getUniqueId())).thenReturn(playerName);
		when(nameProvider.getName(killer.getUniqueId())).thenReturn(killerName);
		when(nameProvider.getUniqueId(playerName)).thenReturn(playerId);
		when(nameProvider.getUniqueId(killerName)).thenReturn(killerId);
		
		when(pearlApi.getPearlPlayer(playerName)).thenReturn(new CorePearlPlayer(player, nameProvider));
		when(pearlApi.getPearlPlayer(playerId)).thenReturn(new CorePearlPlayer(player, nameProvider));
		when(pearlApi.getPearlPlayer(killerName)).thenReturn(new CorePearlPlayer(killer, nameProvider));
		when(pearlApi.getPearlPlayer(killerId)).thenReturn(new CorePearlPlayer(killer, nameProvider));
		
		holder = new PlayerHolder(killer);
		
		pearl = new CoreExilePearl(pearlApi, storage, player.getUniqueId(), killer.getUniqueId(), holder, 10);
	}

	@Test
	public void testCoreExilePearl() {
		// Null arguments throw exceptions
		Throwable e = null;
		try { new CoreExilePearl(null, storage, player.getUniqueId(), killer.getUniqueId(), new PlayerHolder(killer), 0); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { new CoreExilePearl(pearlApi, null, player.getUniqueId(), killer.getUniqueId(), new PlayerHolder(killer), 0); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { new CoreExilePearl(pearlApi, storage, null, killer.getUniqueId(), new PlayerHolder(killer), 0); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { new CoreExilePearl(pearlApi, storage, player.getUniqueId(), null, new PlayerHolder(killer), 0); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { new CoreExilePearl(pearlApi, storage, player.getUniqueId(), killer.getUniqueId(), null, 0); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testGetUniqueId() {
		assertEquals(pearl.getUniqueId(), player.getUniqueId());
	}

	@Test
	public void testGetPlayer() {		
		assertEquals(pearl.getPlayer().getName(), player.getName());
		assertEquals(pearl.getPlayer().getUniqueId(), player.getUniqueId());
	}

	@Test
	public void testGetSetPearledOn() {
		Date now = new Date();
		pearl.setPearledOn(now);
		assertEquals(pearl.getPearledOn(), now);
		
		// Null arg throws exception
		Throwable e = null;
		try { pearl.setPearledOn(null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testGetPlayerName() {		
		assertEquals(pearl.getPlayerName(), player.getName());
	}

	@Test
	public void testGetSetHolder() {
		assertEquals(pearl.getHolder(), holder);

		PearlPlayer pPlayer = new CorePearlPlayer(player, nameProvider);
		pearl.setHolder(pPlayer);
		assertEquals(pearl.getHolder().getName(), pPlayer.getName());
	}

	@Test
	public void testSetHolderBlockLocation() {
		Location l1 = mock(Location.class);
		Location l2 = mock(Location.class);
		Block b = mock(Block.class);
		when(b.getLocation()).thenReturn(l2);
		when(b.getType()).thenReturn(Material.CHEST);
		
		pearl.setHolder(l1);
		assertEquals(pearl.getLocation(), l1);
		assertEquals(pearl.getHolder().getLocation(), l1);
		verify(storage, times(0)).pearlUpdateLocation(pearl);
		pearl.enableStorage();
		
		pearl.setHolder(b);
		assertEquals(pearl.getLocation(), l2);
		assertEquals(pearl.getHolder().getLocation(), l2);
		verify(storage).pearlUpdateLocation(pearl);
		
		// Null arg throws exception
		Throwable e = null;
		try { pearl.setHolder((PearlHolder)null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { pearl.setHolder((PlayerHolder)null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { pearl.setHolder((Block)null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { pearl.setHolder((Location)null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testGetSetHealth() {
		assertEquals(pearl.getHealth(), 10, 0);
		
		pearl.setHealth(0);
		assertEquals(pearl.getHealth(), 0, 0);
		verify(storage, times(0)).pearlUpdateHealth(pearl);
		
		pearl.enableStorage();
		pearl.setHealth(0);
		verify(storage, times(1)).pearlUpdateHealth(pearl);
		
		pearl.setHealth(-10);
		assertEquals(pearl.getHealth(), 0, 0);
		verify(storage, times(2)).pearlUpdateHealth(pearl);
		
		pearl.setHealth(90.5);
		assertEquals(pearl.getHealth(), 90.5, 0);
		verify(storage, times(3)).pearlUpdateHealth(pearl);
		
		pearl.setHealth(100);
		assertEquals(pearl.getHealth(), 100, 0);
		verify(storage, times(4)).pearlUpdateHealth(pearl);
		
		pearl.setHealth(110);
		assertEquals(pearl.getHealth(), 100, 0);
		verify(storage, times(5)).pearlUpdateHealth(pearl);
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
		
		pearl.setHolder(b);
		assertEquals(pearl.getLocationDescription(), "held by a chest at world 1 2 3");
		
		pearl.setHolder(l);
		assertEquals(pearl.getLocationDescription(), "held by nobody at world 1 2 3");
	}

	@Test
	public void testGetSetFreedOffline() {
		assertFalse(pearl.getFreedOffline());
		
		pearl.setFreedOffline(true);
		assertTrue(pearl.getFreedOffline());
		verify(storage, times(0)).pearlUpdateFreedOffline(pearl);
		
		pearl.enableStorage();
		pearl.setFreedOffline(true);
		assertTrue(pearl.getFreedOffline());
		verify(storage).pearlUpdateFreedOffline(pearl);

		pearl.setFreedOffline(false);
		assertFalse(pearl.getFreedOffline());
	}

	@Test
	public void testItemStack() {
		new CmdExilePearl(mock(ExilePearlPlugin.class));
		
	    PowerMockito.mockStatic(Bukkit.class);
	    ItemFactory itemFactory = mock(ItemFactory.class);
	    ItemMeta im = mock(ItemMeta.class);
	    when(itemFactory.getItemMeta(Material.ENDER_PEARL)).thenReturn(im);
	    when(Bukkit.getItemFactory()).thenReturn(itemFactory);
		
		ItemStack is = spy(pearl.createItemStack());
		assertEquals(is.getItemMeta(), im);
		List<String> lore = PearlLoreUtil.generateLore(pearl);
		verify(im).setLore(lore);
		
		
		// Now validate the item stack
		when(im.getLore()).thenReturn(lore);
		when(im.getDisplayName()).thenReturn(playerName);
		
		// Positive test
		assertTrue(pearl.validateItemStack(is));
		
		// Duplicate object
		ExilePearl pearl2 = mock(ExilePearl.class);
		when(pearl2.getItemName()).thenReturn(pearl.getItemName());
		when(pearl2.getPlayerName()).thenReturn(playerName);
		when(pearl2.getUniqueId()).thenReturn(pearl.getUniqueId());
		when(pearl2.getHealth()).thenReturn(pearl.getHealth());
		when(pearl2.getKillerName()).thenReturn(killerName);
		when(pearl2.getPearledOn()).thenReturn(pearl.getPearledOn());

		List<String> lore2 = PearlLoreUtil.generateLore(pearl2);
		assertEquals(lore, lore2);
		when(im.getLore()).thenReturn(lore2);
		assertTrue(pearl.validateItemStack(is));
		
		// Now change the ID and negative test
		when(pearl2.getUniqueId()).thenReturn(UUID.randomUUID());
		lore2 = PearlLoreUtil.generateLore(pearl2);
		when(im.getLore()).thenReturn(lore2);
		assertFalse(pearl.validateItemStack(is));
		
		// Change lore back and validate true
		when(im.getLore()).thenReturn(lore);
		assertTrue(pearl.validateItemStack(is));
		
		// Negative test material
		when(is.getType()).thenReturn(Material.STONE);
		assertFalse(pearl.validateItemStack(is));
		when(is.getType()).thenReturn(Material.ENDER_PEARL);
		assertTrue(pearl.validateItemStack(is));
		
		// Negative test item meta
		when(is.getItemMeta()).thenReturn(null);
		assertFalse(pearl.validateItemStack(is));
		when(is.getItemMeta()).thenReturn(im);
		assertTrue(pearl.validateItemStack(is));
		
		// Null arg throws exception
		Throwable e = null;
		try { pearl.validateItemStack(null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testVerifyLocation() {
		PearlHolder holder1 = mock(PearlHolder.class);
		when(holder1.validate(any(ExilePearl.class), any(StringBuilder.class))).thenReturn(HolderVerifyResult.IN_CHEST);
		
		pearl.setHolder(holder1);
		assertTrue(pearl.verifyLocation());
		
		pearl.setHolder(holder1);
		assertTrue(pearl.verifyLocation());
		
		pearl.setHolder(holder1);
		assertTrue(pearl.verifyLocation());
		
		pearl.setHolder(holder1);
		assertTrue(pearl.verifyLocation());
		
		pearl.setHolder(holder1);
		assertTrue(pearl.verifyLocation());
		

		PearlHolder holder2 = mock(PearlHolder.class);
		when(holder2.validate(any(ExilePearl.class), any(StringBuilder.class))).thenReturn(HolderVerifyResult.DEFAULT);
		
		pearl.setHolder(holder2);
		assertTrue(pearl.verifyLocation());
		
		pearl.setHolder(holder2);
		assertTrue(pearl.verifyLocation());
		
		pearl.setHolder(holder2);
		assertTrue(pearl.verifyLocation());
		
		pearl.setHolder(holder2);
		assertTrue(pearl.verifyLocation());
		
		pearl.setHolder(holder2);
		assertFalse(pearl.verifyLocation());
	}

	@Test
	public void testGetItemFromInventory() {
		// Null arg throws exception
		Throwable e = null;
		try { pearl.getItemFromInventory(null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		Inventory inv = mock(Inventory.class);
		
		new CmdExilePearl(mock(ExilePearlPlugin.class));
		
	    PowerMockito.mockStatic(Bukkit.class);
	    ItemFactory itemFactory = mock(ItemFactory.class);
	    ItemMeta im = mock(ItemMeta.class);
	    when(itemFactory.getItemMeta(Material.ENDER_PEARL)).thenReturn(im);
	    when(Bukkit.getItemFactory()).thenReturn(itemFactory);
		List<String> lore = PearlLoreUtil.generateLore(pearl);
		when(im.getLore()).thenReturn(lore);
		when(im.getDisplayName()).thenReturn(playerName);
		
		ItemStack is = spy(pearl.createItemStack());
		final HashMap<Integer, ItemStack> itemMap = new HashMap<Integer, ItemStack>();
		itemMap.put(0, is);
		
		// Negative test
		assertEquals(pearl.getItemFromInventory(inv), null);
		
		when(inv.all(Material.ENDER_PEARL)).thenAnswer(new Answer<HashMap<Integer, ? extends ItemStack>>() {

			@Override
			public HashMap<Integer, ? extends ItemStack> answer(InvocationOnMock invocation) throws Throwable {
				return itemMap;
			}
		});
		
		// Positive test
		assertEquals(pearl.getItemFromInventory(inv), is);
	}
}
