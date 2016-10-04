package com.devotedmc.ExilePearl.holder;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.devotedmc.ExilePearl.PlayerProvider;
import com.devotedmc.ExilePearl.core.MockPearl;

public class BlockHolderTest {
	
	private Block b;
	private Location loc;
	private BlockHolder holder;

	@Before
	public void setUp() throws Exception {
		
		World w = mock(World.class);
		when(w.getName()).thenReturn("world");
		
		loc = new Location(w, 0, 1, 2);
		
		b = mock(Block.class);
		when(b.getLocation()).thenReturn(loc);
		when(b.getType()).thenReturn(Material.CHEST);
		
		holder = new BlockHolder(b);
	}

	@Test
	public void testBlockHolder() {
		// Null arguments throw exceptions
		Throwable e = null;
		try { new BlockHolder(null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testGetName() {
		when(b.getType()).thenReturn(Material.CHEST);
		assertEquals(holder.getName(), "a chest");

		when(b.getType()).thenReturn(Material.TRAPPED_CHEST);
		assertEquals(holder.getName(), "a chest");
		
		when(b.getType()).thenReturn(Material.ENDER_CHEST);
		assertEquals(holder.getName(), "a chest");

		when(b.getType()).thenReturn(Material.FURNACE);
		assertEquals(holder.getName(), "a furnace");

		when(b.getType()).thenReturn(Material.BREWING_STAND);
		assertEquals(holder.getName(), "a brewing stand");

		when(b.getType()).thenReturn(Material.DISPENSER);
		assertEquals(holder.getName(), "a dispenser");

		when(b.getType()).thenReturn(Material.ITEM_FRAME);
		assertEquals(holder.getName(), "a wall frame");

		when(b.getType()).thenReturn(Material.DROPPER);
		assertEquals(holder.getName(), "a dropper");
		
		when(b.getType()).thenReturn(Material.HOPPER);
		assertEquals(holder.getName(), "a hopper");
		
		when(b.getType()).thenReturn(Material.ENCHANTMENT_TABLE);
		assertEquals(holder.getName(), "an enchantment table");
		
		when(b.getType()).thenReturn(Material.STONE);
		assertEquals(holder.getName(), "a block");
	}

	@Test
	public void testGetLocation() {
		assertEquals(holder.getLocation(), loc);
	}

	@Test
	public void testValidate() {
		MockPearl pearl = new MockPearl(mock(PlayerProvider.class), UUID.randomUUID(), UUID.randomUUID(), 1, loc);
		
		assertEquals(holder.validate(pearl), HolderVerifyResult.BLOCK_STATE_NULL);
		
		BlockState bs = mock(BlockState.class);
		when(bs.getType()).thenReturn(Material.STONE);
		when(bs.getLocation()).thenReturn(loc);
		
		when(b.getState()).thenReturn(bs);
		assertEquals(holder.validate(pearl), HolderVerifyResult.NOT_BLOCK_INVENTORY);
		
		MockInventoryHolder invHolder = mock(MockInventoryHolder.class);
		when(b.getState()).thenReturn(invHolder);
		
		Inventory inv = mock(Inventory.class);
		when(invHolder.getInventory()).thenReturn(inv);

		assertEquals(holder.validate(pearl), HolderVerifyResult.DEFAULT);
		
		// Put the pearl itemstack in the fake inventory
		HashMap<Integer, ItemStack> invItems = new HashMap<Integer, ItemStack>();
		ItemStack pearlItem = pearl.createItemStack();
		invItems.put(0, pearlItem);
		
		when(inv.all(Material.ENDER_PEARL)).thenAnswer(new Answer<HashMap<Integer, ItemStack>>() {

			@Override
			public HashMap<Integer, ItemStack> answer(InvocationOnMock invocation) throws Throwable {
				return invItems;
			}
		});
		
		// It should be found in the chest now
		assertEquals(holder.validate(pearl), HolderVerifyResult.IN_CHEST);
		
		// mock someone holding the pearl in their hand
		HumanEntity viwer = mock(HumanEntity.class);
		when(viwer.getItemOnCursor()).thenReturn(pearlItem);
		ArrayList<HumanEntity> viewers = new ArrayList<HumanEntity>();
		viewers.add(viwer);
		when(inv.getViewers()).thenReturn(viewers);
		
		// It should be found in the player hand now
		assertEquals(holder.validate(pearl), HolderVerifyResult.IN_VIEWER_HAND);
		
	}
}
