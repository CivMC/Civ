package com.github.igotyou.FactoryMod.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.classicTriblockFactory.ProductionFactory;
import com.github.igotyou.FactoryMod.properties.ProductionProperties;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

public class ProductionCsvReader implements IFactoryReader<ProductionFactory>{
	
	/**
	 * The plugin instance
	 */
	FactoryModPlugin mPlugin;
	
	/**
	 * The CSV file being read
	 */
	File mFile;
	
	public ProductionCsvReader(FactoryModPlugin plugin, File file) {
		mPlugin = plugin;
		mFile = file;
	}
	
	private enum LineTokens {
		SUBTYPE,
		RECIPE_LIST,
		WORLD,
		CENTER_X,
		CENTER_Y,
		CENTER_Z,
		INV_X,
		INV_Y,
		INV_Z,
		POWER_X,
		POWER_Y,
		POWER_Z,
		IS_ACTIVE,
		PROD_TIMER,
		ENERGY_TIMER,
		RECIPE_NUMBER,
		REPAIR,
		DISREPAIR_TIME,
		MAX
	}

	@Override
	public synchronized List<ProductionFactory> read() {
		
		List<ProductionFactory> factories = Lists.newArrayList();
		
		if(!mFile.exists() || mFile.isDirectory()) {
			FactoryModPlugin.sendConsoleMessage(new StringBuilder("ERROR: ")
				.append(mFile.getName()).append(" is not a valid file!").toString());
			return factories;
		}
		
		BufferedReader reader;
		
		try {
			reader = Files.newReader(mFile, Charset.defaultCharset());
		} catch (FileNotFoundException e) {
			FactoryModPlugin.sendConsoleMessage(new StringBuilder("ERROR: Could not open file ")
				.append(mFile.getName()).append("for reading: ").append(e.getMessage()).toString());
			return factories;
		}
		
		int lineNum = 1;
		
		try {
			for (String line; (line = reader.readLine()) != null; ++lineNum)
			{
				try {
					factories.add(read(line));
				} catch(Exception e) {
					FactoryModPlugin.logFileError(mFile.getName(), lineNum, "Factory parse error: " + e.getMessage());
					break;
				}
			}
		} catch (IOException e) {
			FactoryModPlugin.logFileError(mFile.getName(), lineNum, "Could not read file, aborting");
		}
		
		try {
			reader.close();
		} catch (IOException e) {
			FactoryModPlugin.sendConsoleMessage(new StringBuilder("ERROR: Could not close stream reading from ")
				.append(mFile.getName()).append(": ").append(e.getMessage()).toString());
		}
		//TODO: ensure everything is closed
		return factories;
	}
	
	//TODO: split into common csv file handler and factory type parser
	public ProductionFactory read(String decode) throws Exception {
		String[] tokens = decode.split(" ");
		
		if(tokens.length != LineTokens.MAX.ordinal()) {
			throw new Exception("Unexpected number of tokens: " + tokens.length);
		}

		try {
			String subFactoryType = tokens[LineTokens.SUBTYPE.ordinal()];
			String recipeNames[] = tokens[LineTokens.RECIPE_LIST.ordinal()].split(",");

			World world = mPlugin.getServer().getWorld(tokens[LineTokens.WORLD.ordinal()]);
			Location centerLocation = new Location(world, 
					Integer.parseInt(tokens[LineTokens.CENTER_X.ordinal()]), 
					Integer.parseInt(tokens[LineTokens.CENTER_Y.ordinal()]), 
					Integer.parseInt(tokens[LineTokens.CENTER_Z.ordinal()]));
			Location inventoryLocation = new Location(world, 
					Integer.parseInt(tokens[LineTokens.INV_X.ordinal()]), 
					Integer.parseInt(tokens[LineTokens.INV_Y.ordinal()]), 
					Integer.parseInt(tokens[LineTokens.INV_Z.ordinal()]));
			Location powerLocation = new Location(world, 
					Integer.parseInt(tokens[LineTokens.POWER_X.ordinal()]), 
					Integer.parseInt(tokens[LineTokens.POWER_Y.ordinal()]), 
					Integer.parseInt(tokens[LineTokens.POWER_Z.ordinal()]));
			
			boolean active = Boolean.parseBoolean(tokens[LineTokens.IS_ACTIVE.ordinal()]);
			int productionTimer = Integer.parseInt(tokens[LineTokens.PROD_TIMER.ordinal()]);
			int energyTimer = Integer.parseInt(tokens[LineTokens.ENERGY_TIMER.ordinal()]);
			int currentRecipeNumber = Integer.parseInt(tokens[LineTokens.RECIPE_NUMBER.ordinal()]);
			double currentRepair = Double.parseDouble(tokens[LineTokens.REPAIR.ordinal()]);
			long timeDisrepair  =  Long.parseLong(tokens[LineTokens.DISREPAIR_TIME.ordinal()]);
			
			if(FactoryModPlugin.productionProperties.containsKey(subFactoryType))
			{
				Set<ProductionRecipe> recipes = Sets.newHashSet();
				
				// TODO: Give default recipes for subfactory type
				ProductionProperties properties = FactoryModPlugin.productionProperties.get(subFactoryType);
				recipes.addAll(properties.getRecipes());
				
				for(String name : recipeNames)
				{
					if(FactoryModPlugin.productionRecipes.containsKey(name))
					{
						recipes.add(FactoryModPlugin.productionRecipes.get(name));
					}
				}

				return new ProductionFactory(
						centerLocation, inventoryLocation, powerLocation, 
						subFactoryType, active, productionTimer, energyTimer, 
						new ArrayList<ProductionRecipe>(recipes), 
						currentRecipeNumber, currentRepair, timeDisrepair);
			} else {
				throw new Exception("Unexpected factory type: " + subFactoryType);
			}
		} catch (NumberFormatException e) {
			throw new Exception("Expected token was not an integer");
		}
		
	}

}
