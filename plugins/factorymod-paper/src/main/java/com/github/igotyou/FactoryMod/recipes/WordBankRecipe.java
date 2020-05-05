package com.github.igotyou.FactoryMod.recipes;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class WordBankRecipe extends InputRecipe {

	private long key;
	private MessageDigest digest;
	private List<String> validWords;

	private List<ChatColor> colors;
	private int words;
	private SecureRandom preview;

	public WordBankRecipe(String identifier, String name, int productionTime, long key, List<String> words,
			List<ChatColor> colors, int wordCount) {
		super(identifier, name, productionTime, new ItemMap());
		try {
			this.digest = MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e) {
			FactoryMod.getInstance().getLogger().severe("Failed to instanciate SHA-512:" + e.getMessage());
		}
		this.key = key;
		this.words = wordCount;
		this.validWords = words;
		this.colors = colors;
		this.preview = new SecureRandom();
	}

	@Override
	public void applyEffect(Inventory inventory, FurnCraftChestFactory factory) {
		ItemStack toApply = inventory.getItem(0);
		if (!ItemAPI.isValidItem(toApply)) {
			return;
		}
		ItemMap input = new ItemMap();
		for (int i = 1; i < inventory.getSize(); i++) {
			ItemStack is = inventory.getItem(i);
			if (!ItemAPI.isValidItem(is)) {
				continue;
			}
			input.addItemStack(is);
			inventory.setItem(i, null);
		}
		ItemAPI.setDisplayName(toApply, getHash(input));
	}

	@Override
	public String getTypeIdentifier() {
		return "WORDBANK";
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		ItemStack is = new ItemStack(Material.RED_WOOL);
		ItemAPI.addLore(is, ChatColor.GOLD + "A tool or piece of armor and any other random amount of items");
		return Collections.singletonList(is);
	}

	@Override
	public List<ItemStack> getOutputRepresentation(Inventory inv, FurnCraftChestFactory fccf) {
		ItemStack is = new ItemStack(Material.DIAMOND_SWORD);
		StringBuilder output = new StringBuilder();
		output.append(colors.get(preview.nextInt(colors.size())));
		for (int i = 0; i < words; i++) {
			String word = validWords.get(preview.nextInt(validWords.size()));
			if (i > 0) {
				output.append(" ");
			}
			output.append(word);
		}
		ItemAPI.setDisplayName(is, output.toString());
		return Collections.singletonList(is);
	}

	@Override
	public Material getRecipeRepresentationMaterial() {
		return Material.PAINTING;
	}
	
	

	@Override
	public boolean enoughMaterialAvailable(Inventory inventory) {
		ItemStack toApply = inventory.getItem(0);
		if (!ItemAPI.isValidItem(toApply)) {
			return false;
		}
		for (int i = 1; i < inventory.getSize(); i++) {
			ItemStack is = inventory.getItem(i);
			if (!ItemAPI.isValidItem(is)) {
				continue;
			}
			return true;
		}
		return false;
	}

	private synchronized String getHash(ItemMap items) {
		ByteBuffer keyBuffer = ByteBuffer.allocate(Long.BYTES);
		keyBuffer.putLong(key);
		digest.digest(keyBuffer.array());
		for (Entry<ItemStack, Integer> entry : items.getEntrySet()) {
			digestItem(entry.getKey(), digest);
			digest.digest(toBuffer(entry.getValue()));
		}
		byte[] result = digest.digest();
		ByteBuffer buffer = ByteBuffer.allocate(result.length);
		buffer.put(result, 0, result.length);
		buffer.flip();
		StringBuilder output = new StringBuilder();
		output.append(colors.get(pickIndex(buffer.getInt(0), colors.size())));
		for (int i = 1; i <= words; i++) {
			int intKey = buffer.getInt(i);
			String word = validWords.get(pickIndex(intKey, validWords.size()));
			if (i > 1) {
				output.append(" ");
			}
			output.append(word);
		}
		return output.toString();
	}

	private static int pickIndex(int hash, int length) {
		int index = hash % length;
		if (index < 0) {
			index += length;
		}
		return index;
	}

	private static void digestItem(ItemStack is, MessageDigest digest) {
		digest.digest(is.getType().getKey().getKey().getBytes());
		if (is.hasItemMeta()) {
			ItemMeta im = is.getItemMeta();
			if (im.hasLore()) {
				for (String lore : im.getLore()) {
					digest.digest(lore.getBytes());
				}
			}
			if (im.hasDisplayName()) {
				digest.digest(im.getDisplayName().getBytes());
			}
		}
	}

	private static byte[] toBuffer(int hash) {
		ByteBuffer b = ByteBuffer.allocate(4);
		b.putInt(hash);
		return b.array();
	}

	public static long bytesToLong(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.put(bytes);
		buffer.flip();// need flip
		return buffer.getLong();
	}

	@Override
	public List<String> getTextualOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		return Arrays.asList("The item input with a random colored name applied");
	}

}
