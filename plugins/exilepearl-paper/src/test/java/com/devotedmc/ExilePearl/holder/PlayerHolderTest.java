package com.devotedmc.ExilePearl.holder;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.devotedmc.ExilePearl.PlayerProvider;
import com.devotedmc.ExilePearl.core.MockPearl;

public class PlayerHolderTest {

	private Player player;
	private Location loc;
	private PlayerHolder holder;

	@Before
	public void setUp() throws Exception {

		World w = mock(World.class);
		when(w.getName()).thenReturn("world");

		loc = new Location(w, 0, 1, 2);
		player = mock(Player.class);
		when(player.getLocation()).thenReturn(loc);
		when(player.getName()).thenReturn("Player");

		holder = new PlayerHolder(player);
	}

	@Test
	public void testBlockHolder() {
		// Null arguments throw exceptions
		Throwable e = null;
		try { new PlayerHolder(null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testGetName() {
		assertEquals(holder.getName(), "Player");
	}

	@Test
	public void testGetLocation() {
		assertEquals(holder.getLocation(), loc);
	}

	@Test
	public void testValidate() {
		MockPearl pearl = new MockPearl(mock(PlayerProvider.class), UUID.randomUUID(), UUID.randomUUID(), 1, loc);
		final ItemStack pearlStack = pearl.createItemStack();

		assertEquals(holder.validate(pearl), HolderVerifyResult.PLAYER_NOT_ONLINE);

		when(player.isOnline()).thenReturn(true);
		ItemStack cursorItem = mock(ItemStack.class);
		when(player.getItemOnCursor()).thenReturn(cursorItem);

		PlayerInventory inv = mock(PlayerInventory.class);
		when(player.getInventory()).thenReturn(inv);

		PlayerInventory craftInv = mock(PlayerInventory.class);
		InventoryView inView = mock(InventoryView.class);
		when(inView.getTopInventory()).thenReturn(craftInv);
		when(player.getOpenInventory()).thenReturn(inView);		

		assertEquals(holder.validate(pearl), HolderVerifyResult.DEFAULT);

		HashMap<Integer, ItemStack> invItems = new HashMap<Integer, ItemStack>();
		invItems.put(0, pearlStack);
		when(inv.all(Material.ENDER_PEARL)).thenAnswer(new Answer<HashMap<Integer, ItemStack>>() {

			@Override
			public HashMap<Integer, ItemStack> answer(InvocationOnMock invocation) throws Throwable {
				return invItems;
			}
		});

		assertEquals(holder.validate(pearl), HolderVerifyResult.IN_PLAYER_INVENTORY);

		invItems.clear();
		assertEquals(holder.validate(pearl), HolderVerifyResult.DEFAULT);

		// Check crafting inventory
		HashMap<Integer, ItemStack> craftItems = new HashMap<Integer, ItemStack>();
		craftItems.put(0, pearlStack);
		when(craftInv.all(Material.ENDER_PEARL)).thenAnswer(new Answer<HashMap<Integer, ItemStack>>() {

			@Override
			public HashMap<Integer, ItemStack> answer(InvocationOnMock invocation) throws Throwable {
				return craftItems;
			}
		});

		assertEquals(holder.validate(pearl), HolderVerifyResult.IN_PLAYER_INVENTORY_VIEW);

		when(player.getItemOnCursor()).thenReturn(pearlStack);
		assertEquals(holder.validate(pearl), HolderVerifyResult.IN_HAND);
	}
}
