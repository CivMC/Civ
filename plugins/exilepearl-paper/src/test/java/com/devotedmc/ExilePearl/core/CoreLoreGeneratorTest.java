package com.devotedmc.ExilePearl.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.Before;
import org.junit.Test;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlConfig;

public class CoreLoreGeneratorTest {
	
	private final UUID playerId = UUID.randomUUID();
	private final int pearlId = 1234;
	
	private PearlConfig config;
	private CoreLoreGenerator dut;
	private ExilePearl pearl;

	@Before
	public void setUp() throws Exception {
		
		config = mock(PearlConfig.class);
		when(config.getPearlHealthMaxValue()).thenReturn(100);
		
		dut = new CoreLoreGenerator(config);
		
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
		
		ItemMeta im = mock(ItemMeta.class);
		when(im.getLore()).thenReturn(lore);
		when(im.hasEnchants()).thenReturn(true);
		when(im.hasItemFlag(ItemFlag.HIDE_ENCHANTS)).thenReturn(true);
		
		ItemStack is = mock(ItemStack.class);
		when(is.getItemMeta()).thenReturn(im);
		when(is.getType()).thenReturn(Material.ENDER_PEARL);
		
		assertEquals(pearlId, dut.getPearlIdFromItemStack(is));
	}
	
	@Test
	public void testLegacyPearl() {
		UUID legacyId = UUID.randomUUID();
		String legacyName = "Killer";
		
		List<String> lore = new LinkedList<String>();
		lore.add(ChatColor.GOLD + "Player" + ChatColor.RESET + " is held in this pearl");
		lore.add(legacyId.toString());
		lore.add(ChatColor.RESET + "Killed by " + ChatColor.GOLD + legacyName);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		lore.add(ChatColor.BLUE + "Imprisoned " + sdf.format(new Date()));
		lore.add(ChatColor.DARK_GREEN + "Unique: " + "12345");

		ItemMeta im = mock(ItemMeta.class);
		when(im.getLore()).thenReturn(lore);
		when(im.hasEnchants()).thenReturn(true);
		when(im.hasItemFlag(ItemFlag.HIDE_ENCHANTS)).thenReturn(true);
		
		ItemStack is = mock(ItemStack.class);
		when(is.getItemMeta()).thenReturn(im);
		when(is.getType()).thenReturn(Material.ENDER_PEARL);
		
		assertEquals(legacyId, dut.getPlayerIdFromLegacyPearl(is));
		assertEquals(legacyName, dut.getKillerNameFromLegacyPearl(is));
	}
}
