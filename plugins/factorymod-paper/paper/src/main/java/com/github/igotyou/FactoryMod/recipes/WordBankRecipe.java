package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import com.github.igotyou.FactoryMod.utility.MultiInventoryWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

public class WordBankRecipe extends InputRecipe {

	private String key;
	private MessageDigest digest;
	private List<String> validWords;

	private List<ChatColor> colors;
	private int words;
	private SecureRandom preview;

	public WordBankRecipe(String identifier, String name, int productionTime, String key, List<String> words,
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
	public boolean applyEffect(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
		MultiInventoryWrapper combo = new MultiInventoryWrapper(inputInv, outputInv);
		ItemStack toApply = inputInv.getItem(0);
		if (!ItemUtils.isValidItem(toApply)) {
			return false;
		}
		if (!ItemUtils.getDisplayName(toApply).isEmpty()) {
			return false;
		}
		ItemMap input = new ItemMap();
		for (int i = 1; i < inputInv.getSize(); i++) {
			ItemStack is = inputInv.getItem(i);
			if (!ItemUtils.isValidItem(is)) {
				continue;
			}
			input.addItemStack(is);
			inputInv.setItem(i, null);
		}
		//tell player what the recipe consumed
		StringBuilder sb = new StringBuilder();
		sb.append(ChatColor.GOLD);
		sb.append("Wordbank recipe complete and turned ");
		for (Entry<ItemStack, Integer> entry : input.getEntrySet()) {
			sb.append(entry.getValue());
			sb.append(" ");
			sb.append(ItemUtils.getItemName(entry.getKey()));
			sb.append(", ");
		}
		String result = sb.substring(0, sb.length() - 2);
		String name = getHash(input);
		ItemUtils.setDisplayName(toApply, name);
		if (fccf.getActivator() != null) {
			Player player = Bukkit.getPlayer(fccf.getActivator());
			if (player != null) {
				player.sendMessage(result + " into " + name);
			}
		}
		//always return false to turn the factory off
		return false;
	}

	@Override
	public String getTypeIdentifier() {
		return "WORDBANK";
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		ItemStack is = new ItemStack(Material.RED_WOOL);
		ItemUtils.addLore(is, ChatColor.GOLD + "A tool or piece of armor and any other random amount of items");
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
		ItemUtils.setDisplayName(is, output.toString());
		return Collections.singletonList(is);
	}

	@Override
	public Material getRecipeRepresentationMaterial() {
		return Material.WRITABLE_BOOK;
	}

	@Override
	public boolean enoughMaterialAvailable(Inventory inputInv) {
		ItemStack toApply = inputInv.getItem(0);
		if (!ItemUtils.isValidItem(toApply)) {
			return false;
		}
		if (!ItemUtils.getDisplayName(toApply).isEmpty()) {
			return false;
		}
		for (int i = 1; i < inputInv.getSize(); i++) {
			ItemStack is = inputInv.getItem(i);
			if (!ItemUtils.isValidItem(is)) {
				continue;
			}
			return true;
		}
		return false;
	}

	private synchronized String getHash(ItemMap items) {
		digest.update(key.getBytes());
		List<Entry<ItemStack, Integer>> entries = new ArrayList<>(items.getEntrySet());
		//sort because hashmaps dont guarantee iteration order
		Collections.sort(entries,
				(a, b) -> a.getKey().getType().getKey().getKey().compareTo(b.getKey().getType().getKey().getKey()));
		for (Entry<ItemStack, Integer> entry : entries) {
			digest.update(entry.getKey().getType().getKey().getKey().getBytes());
			digest.update(toBuffer(entry.getValue()));
		}
		byte[] result = digest.digest();
		ByteBuffer buffer = ByteBuffer.allocate(result.length);
		buffer.put(result, 0, result.length);
		StringBuilder output = new StringBuilder();
		output.append(colors.get(pickIndex(buffer.getInt(0), colors.size())));
		int currentLength = new Random(buffer.getLong(1)).nextInt(words) + 1;
		for (int i = 1; i <= currentLength; i++) {
			//offset by 4 to avoid overlap with first two longs
			int intKey = buffer.getInt(i + 4);
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

	private static byte[] toBuffer(int hash) {
		ByteBuffer b = ByteBuffer.allocate(4);
		b.putInt(hash);
		return b.array();
	}

	@Override
	public List<String> getTextualInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		return Arrays.asList("An item to be renamed and an item to be consumed as key");
	}

	@Override
	public List<String> getTextualOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		return Arrays.asList("The item input with a random colored name applied");
	}

}
