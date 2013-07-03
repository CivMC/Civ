/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.untamedears.ItemExchange;

import com.untamedears.ItemExchange.listeners.ItemExchangeListener;
import com.untamedears.ItemExchange.command.CommandHandler;
import com.untamedears.ItemExchange.utility.InteractionResponse;
import com.untamedears.ItemExchange.utility.ExchangeRule;
import com.untamedears.ItemExchange.utility.ExchangeRule.RuleType;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Brian Landry
 */
public class ItemExchangePlugin extends JavaPlugin{
	
	private static final CommandHandler commandHandler = new CommandHandler();
	//Blocks that can be used as exchanges, any block with an inventory *should* works
	public static final List<Material> ACCEPTABLE_BLOCKS= Arrays.asList(Material.CHEST, Material.DISPENSER, Material.TRAPPED_CHEST);
	public static final boolean CITADEL_ENABLED=false;
	//Maps that provide a 1:1 relationship between commonly used/displayed item and enchantment names
	//and their bukkit counterparts. The itemstack mapping may be incomplete, however the enchantment
	//mapping needs to be complete
	public static final Map<ItemStack,String> MATERIAL_NAME=new HashMap();
	public static final Map<String,ItemStack> NAME_MATERIAL=new HashMap();
	public static final Map<String,String> ENCHANTMENT_ABBRV=new HashMap();
	public static final Map<String,String> ABBRV_ENCHANTMENT=new HashMap();
	//Specifics of appeareance of ItemExchange Rules
	//The Version system could use another look
	public static final String INPUT_NAME="ItemExchange Input";
	public static final String OUTPUT_NAME="ItemExchange Output";
	public static final ItemStack ITEM_RULE_ITEMSTACK=new ItemStack(Material.STONE_BUTTON,1);
	public static final String VERSION="v0.2";
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		return commandHandler.dispatch(sender, label, args);
	}
	public void onEnable(){
		//load the config.yml
		initConfig();
		//Import CSVs
		importCSVs();
		//register the events(this should be moved...)
		registerEvents();
		commandHandler.registerCommands();
	}
	
	public void onDisable(){

	}
	
	public void registerEvents(){
		try{
			getServer().getPluginManager().registerEvents(new ItemExchangeListener(), this);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void initConfig(){
	}
	public void importCSVs(){
		
		this.saveResource("materials.csv", true);
		//Read Items
		try{
			BufferedReader CSVFile = new BufferedReader(new FileReader("plugins/ItemExchange/materials.csv"));
			String dataRow = CSVFile.readLine();
			while (dataRow != null){
				String[] dataArray = dataRow.split(",");
				ItemExchangePlugin.NAME_MATERIAL.put(dataArray[0],new ItemStack(Material.getMaterial(dataArray[1]),Short.valueOf(dataArray[3])));
				ItemExchangePlugin.MATERIAL_NAME.put(new ItemStack(Material.getMaterial(dataArray[1]),Short.valueOf(dataArray[3])),dataArray[0]);
				dataRow = CSVFile.readLine();
			}
			CSVFile.close();
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
		//Read enchantments
		this.saveResource("enchantments.csv", true);
		try{
			BufferedReader CSVFile = new BufferedReader(new FileReader("plugins/ItemExchange/enchantments.csv"));
			String dataRow = CSVFile.readLine();
			while (dataRow != null){
				String[] dataArray = dataRow.split(",");
				ItemExchangePlugin.ABBRV_ENCHANTMENT.put(dataArray[0],dataArray[1]);
				ItemExchangePlugin.ENCHANTMENT_ABBRV.put(dataArray[1],dataArray[0]);
				dataRow = CSVFile.readLine();
			}
			CSVFile.close();
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
	}
	public static void sendConsoleMessage(String message){
		Bukkit.getLogger().info("ItemExchange: " + message);	
	}
	
	//Probably makes sense to move this to the ItemExchange Class...
	public static InteractionResponse createExchange(Location location,Player player){
		//Bail if location doesn't contain an an accpetable inventory block
		if(ItemExchangePlugin.ACCEPTABLE_BLOCKS.contains(location.getBlock().getType())&&location.getBlock().getState() instanceof InventoryHolder){
			Inventory inventory=((InventoryHolder)location.getBlock().getState()).getInventory();
			ItemStack input=null;
			ItemStack output=null;
			//Checks for two different unique types of items in the inventory and sums up their amounts from the individual itemStacks
			for(ItemStack itemStack:inventory){
				if(itemStack!=null){
					if(input==null){
						input=itemStack.clone();
					}
					else if(itemStack.isSimilar(input)){
						input.setAmount(input.getAmount()+itemStack.getAmount());
					}
					else if(output==null){
						output=itemStack.clone();
					}
					else if(output.isSimilar(itemStack)){
						output.setAmount(output.getAmount()+itemStack.getAmount());
					}
					else{
						return new InteractionResponse(InteractionResponse.InteractionResult.FAILURE,"Inventory should only contain two types of items!");
					}
				}
			}
			//If acceptable input and output itemStacks were found create exchange rule blocks for each and place them in the inventory blcok
			if(input!=null&&output!=null){
				ExchangeRule inputRule=ExchangeRule.parseItemStack(input,RuleType.INPUT);
				ExchangeRule outputRule=ExchangeRule.parseItemStack(output,RuleType.OUTPUT);
				//Place input in inventory, if this fails drop it on the ground
				if(inventory.addItem(inputRule.toItemStack()).size()>0){
					player.getWorld().dropItem(player.getLocation(), inputRule.toItemStack());
				}
				//place output in the inventory, if this fails drop it on the ground
				if(inventory.addItem(outputRule.toItemStack()).size()>0)	{
					player.getWorld().dropItem(player.getLocation(), outputRule.toItemStack());
				}
				return new InteractionResponse(InteractionResponse.InteractionResult.SUCCESS,"Created Exchage Succesfully");
			}
			else{
				return new InteractionResponse(InteractionResponse.InteractionResult.FAILURE,"Inventory should have at least two types of items");
			}
		}
		else{
			return new InteractionResponse(InteractionResponse.InteractionResult.FAILURE,"Not a valid exchange block");
		}
	}
}