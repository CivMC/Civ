package com.devotedmc.ExilePearl.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.Before;
import org.junit.Test;

import com.devotedmc.ExilePearl.ExilePearl;

public class CoreLoreGeneratorTest {
	
	private final UUID playerId = UUID.randomUUID();
	private final int pearlId = 1234;
	
	private CoreLoreGenerator dut;
	private ExilePearl pearl;

	@Before
	public void setUp() throws Exception {
		dut = new CoreLoreGenerator();
		
		pearl = mock(ExilePearl.class);
		when(pearl.getItemName()).thenReturn("ExilePearl");
		when(pearl.getPlayerId()).thenReturn(playerId);
		when(pearl.getPlayerName()).thenReturn("Player");
		when(pearl.getKillerName()).thenReturn("Killer");
		when(pearl.getHealth()).thenReturn(10);
		when(pearl.getPearledOn()).thenReturn(new Date());
		when(pearl.getPearlId()).thenReturn(pearlId);
	}

	@Test
	public void test() {
		List<String> lore = dut.generateLore(pearl);
		
		ItemStack is = mock(ItemStack.class);
		ItemMeta im = mock(ItemMeta.class);
		when(is.getItemMeta()).thenReturn(im);
		when(im.getLore()).thenReturn(lore);
		when(is.getType()).thenReturn(Material.ENDER_PEARL);
		
		//assertEquals(dut.getPlayerIdFromItemStack(is), playerId);
		assertEquals(dut.getPearlIdFromItemStack(is), pearlId);
	}
}
