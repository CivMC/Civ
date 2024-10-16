package com.github.civcraft.donum.misc;

import com.github.civcraft.donum.Donum;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;


/**
 * Collection of static utility methods to serialize/deserialize ItemMaps, so
 * they can be stored conveniently
 *
 */
public class ItemMapBlobHandling {

	/**
	 * Serializes a given ItemMap into YAML using bukkits built in itemstack
	 * serialization, turns the whole yaml into a string, compacts this string
	 * with gzip and returns the result as byte array
	 * 
	 * @param im
	 *            ItemMap to serialize and compact
	 * @return Compacted and serialized ItemMap
	 */
	public static byte[] turnItemMapIntoBlob(ItemMap im) {
		YamlConfiguration yaml = new YamlConfiguration();
		int count = 0;
		for (Entry<ItemStack, Integer> entry : im.getEntrySet()) {
			ItemStack is = entry.getKey().clone();
			is.setAmount(entry.getValue());
			//yaml doesnt allow int as keys, so we have to add a string
			yaml.set("bla" + String.valueOf(count), is);
			count++;
		}
		return compress(yaml.saveToString());
	}

	/**
	 * Decompresses the given ItemMap to a string using gzip, uses that string
	 * to construct a YAML configuration and then fills an ItemMap with the
	 * items serialized in the configuration
	 * 
	 * @param data
	 *            Blob to decompress and deserialize
	 * @return Resulting ItemMap
	 */
	public static ItemMap turnBlobIntoItemMap(byte[] data) {
		YamlConfiguration yaml = new YamlConfiguration();
		try {
			yaml.loadFromString(decompress(data));
		} catch (InvalidConfigurationException e) {
			Donum.getInstance().warning("Tried to turn invalid blob into configuration section ; " + e);
		}
		ItemMap im = new ItemMap();
		for (String key : yaml.getKeys(false)) {
			ItemStack is = yaml.getItemStack(key);
			im.addItemStack(is);
		}
		return im;
	}

	/**
	 * Compresses a given String to a byte array using gzip
	 * 
	 * @param str
	 *            String to compress
	 * @return Compressed string
	 */
	public static byte[] compress(String str) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(str.length());
		byte[] compressed = null;
		try {
			GZIPOutputStream gzip = new GZIPOutputStream(bos);
			gzip.write(str.getBytes());
			gzip.close();
			compressed = bos.toByteArray();
			bos.close();
		} catch (IOException e) {
			Donum.getInstance().warning("Failed to compact string " + str + " ; " + e);
		}
		return compressed;
	}

	/**
	 * Decompresses a given byte array to a string using gzip
	 * 
	 * @param data
	 *            Data to decompress
	 * @return Resulting string
	 */
	public static String decompress(byte[] data) {
		StringBuilder sb = new StringBuilder();
		try (ByteArrayInputStream bis = new ByteArrayInputStream(data)) {
			GZIPInputStream gis = new GZIPInputStream(bis);
			BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();
			gis.close();
			bis.close();
		} catch (IOException e) {
			Donum.getInstance().warning("Failed to decompact data ; " + e);
		}
		return sb.toString();
	}

	/**
	 * Turns a given inventory into an equivalent ItemMap
	 * 
	 * @param i
	 *            Inventory to base ItemMap on *
	 * @return Constructed ItemMap
	 */
	public static ItemMap constructItemMapFromInventory(Inventory i) {
		ItemMap im = new ItemMap();
		for (ItemStack is : i.getStorageContents()) {
			im.addItemStack(is);
		}
		if (i instanceof PlayerInventory) {
			PlayerInventory playerInv = (PlayerInventory) i;
			for (ItemStack is : playerInv.getArmorContents()) {
				im.addItemStack(is);
			}
			for (ItemStack is : playerInv.getExtraContents()) {
				im.addItemStack(is);
			}
		}
		return im;
	}
}
