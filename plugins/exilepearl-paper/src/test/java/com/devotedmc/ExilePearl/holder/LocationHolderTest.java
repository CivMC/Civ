package com.devotedmc.ExilePearl.holder;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.Test;

import com.devotedmc.ExilePearl.PlayerProvider;
import com.devotedmc.ExilePearl.core.MockPearl;

public class LocationHolderTest {
	
	private Location loc;
	private LocationHolder holder;

	@Before
	public void setUp() throws Exception {
		
		World w = mock(World.class);
		when(w.getName()).thenReturn("world");
		
		loc = new Location(w, 0, 1, 2);
		
		holder = new LocationHolder(loc);
	}

	@Test
	public void testBlockHolder() {
		// Null arguments throw exceptions
		Throwable e = null;
		try { new LocationHolder(null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testGetName() {
		assertEquals(holder.getName(), "nobody");
	}

	@Test
	public void testGetLocation() {
		assertEquals(holder.getLocation(), loc);
	}

	@Test
	public void testValidate() {
		MockPearl pearl = new MockPearl(mock(PlayerProvider.class), UUID.randomUUID(), UUID.randomUUID(), loc);
		final ItemStack pearlStack = pearl.createItemStack();
		StringBuilder sb = new StringBuilder();

		Chunk chunk = mock(Chunk.class);
		when(loc.getChunk()).thenReturn(chunk);
		
		Entity[] entities = new Entity[0];
		when(chunk.getEntities()).thenReturn(entities);
		
		// Pearl shouldn't be found
		assertEquals(holder.validate(pearl, sb), HolderVerifyResult.ENTITY_NOT_IN_CHUNK);
		
		entities = new Entity[1];
		Item item = mock(Item.class);
		when(item.getLocation()).thenReturn(loc);
		entities[0] = item;
		when(chunk.getEntities()).thenReturn(entities);
		when(item.getItemStack()).thenReturn(pearlStack);
		
		// Pearl should now be found
		assertEquals(holder.validate(pearl, sb), HolderVerifyResult.ON_GROUND);
	}
}
