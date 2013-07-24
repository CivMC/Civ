/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.untamedears.ItemExchange;

import com.untamedears.ItemExchange.listeners.ItemExchangeListener;
import com.untamedears.ItemExchange.command.CommandHandler;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 
 * @author Brian Landry
 */
public class ItemExchangePlugin extends JavaPlugin {
	public static ItemExchangePlugin instance;
	
	private static final CommandHandler commandHandler = new CommandHandler();
	// Blocks that can be used as exchanges, any block with an inventory
	// *should* works
	public static final List<Material> ACCEPTABLE_BLOCKS = Arrays.asList(Material.CHEST, Material.DISPENSER, Material.TRAPPED_CHEST);
	public static final List<Material> ENCHANTABLE_ITEMS = Arrays.asList(Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS, 
			Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS, 
			Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS, 
			Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, 
			Material.DIAMOND_SWORD, Material.GOLD_SWORD, Material.STONE_SWORD, Material.WOOD_SWORD, 
			Material.DIAMOND_AXE, Material.GOLD_AXE, Material.STONE_AXE, Material.WOOD_AXE, 
			Material.DIAMOND_PICKAXE, Material.GOLD_PICKAXE, Material.STONE_PICKAXE, Material.WOOD_PICKAXE, 
			Material.DIAMOND_SPADE, Material.GOLD_SPADE, Material.STONE_SPADE, Material.WOOD_SPADE, 
			Material.DIAMOND_HOE, Material.GOLD_HOE, Material.STONE_HOE, Material.WOOD_HOE, 
			Material.BOW, Material.SHEARS, Material.FISHING_ROD, Material.FLINT_AND_STEEL, Material.CARROT_STICK);
	public static final boolean CITADEL_ENABLED = false;
	// Maps that provide a 1:1 relationship between commonly used/displayed item
	// and enchantment names
	// and their bukkit counterparts. The itemstack mapping may be incomplete,
	// however the enchantment
	// mapping needs to be complete
	public static final Map<ItemStack, String> MATERIAL_NAME = new HashMap<ItemStack, String>();
	public static final Map<String, ItemStack> NAME_MATERIAL = new HashMap<String, ItemStack>();
	public static final Map<String, String> ENCHANTMENT_ABBRV = new HashMap<String, String>();
	public static final Map<String, String> ABBRV_ENCHANTMENT = new HashMap<String, String>();
	public static final Map<String, String> ENCHANTMENT_NAME = new HashMap<String, String>();
	// Specifics of appeareance of ItemExchange Rules
	public static final ItemStack ITEM_RULE_ITEMSTACK = new ItemStack(Material.STONE_BUTTON, 1);

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return commandHandler.dispatch(sender, label, args);
	}

	public void onEnable() {
		instance = this;
		
		// load the config.yml
		initConfig();
		// Import CSVs
		importCSVs();
		// register the events(this should be moved...)
		registerEvents();
		commandHandler.registerCommands();
	}

	public void onDisable() {

	}

	public void registerEvents() {
		try {
			getServer().getPluginManager().registerEvents(new ItemExchangeListener(), this);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initConfig() {
	}

	public void importCSVs() {

		this.saveResource("materials.csv", true);
		// Read Items
		try {
			BufferedReader CSVFile = new BufferedReader(new FileReader("plugins/ItemExchange/materials.csv"));
			String dataRow = CSVFile.readLine();
			while (dataRow != null) {
				String[] dataArray = dataRow.split(",");
				ItemExchangePlugin.NAME_MATERIAL.put(dataArray[0].toLowerCase().replace(" ", ""), new ItemStack(Material.getMaterial(dataArray[1]), 1, Short.valueOf(dataArray[3])));
				ItemExchangePlugin.MATERIAL_NAME.put(new ItemStack(Material.getMaterial(dataArray[1]), 1, Short.valueOf(dataArray[3])), dataArray[0]);
				dataRow = CSVFile.readLine();
			}
			CSVFile.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		// Read enchantments
		this.saveResource("enchantments.csv", true);
		try {
			BufferedReader CSVFile = new BufferedReader(new FileReader("plugins/ItemExchange/enchantments.csv"));
			String dataRow = CSVFile.readLine();
			while (dataRow != null) {
				String[] dataArray = dataRow.split(",");
				ItemExchangePlugin.ABBRV_ENCHANTMENT.put(dataArray[0], dataArray[1]);
				ItemExchangePlugin.ENCHANTMENT_ABBRV.put(dataArray[1], dataArray[0]);
				ItemExchangePlugin.ENCHANTMENT_NAME.put(dataArray[1], dataArray[2]);
				dataRow = CSVFile.readLine();
			}
			CSVFile.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void sendConsoleMessage(String message) {
		Bukkit.getLogger().info("ItemExchange: " + message);
	}
}
