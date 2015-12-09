package com.github.igotyou.FactoryMod.persistence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.bukkit.Location;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.classicTriblockFactory.ProductionFactory;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import com.google.common.io.Files;

public class ProductionCsvWriter implements IFactoryWriter<ProductionFactory> {
	
	File mFile;
	
	public ProductionCsvWriter(File file) {
		mFile = file;
	}

	@Override
	public synchronized void write(List<ProductionFactory> factories) {
		FileBackup.backup(mFile);
		
		if(!mFile.exists()) {
			try {
				mFile.createNewFile();
			} catch (IOException e) {
				FactoryModPlugin.sendConsoleMessage(new StringBuilder("ERROR: Could not create file ")
					.append(mFile.getName()).append(": ").append(e.getMessage()).toString());
				return;				
			}
		}
		
		BufferedWriter writer;
		
		try {
			writer = Files.newWriter(mFile, Charset.defaultCharset());
		} catch (FileNotFoundException e) {
			FactoryModPlugin.sendConsoleMessage(new StringBuilder("ERROR: Could not open file ")
				.append(mFile.getName()).append("for writing: ").append(e.getMessage()).toString());
			return;
		}
		
		for (ProductionFactory factory : factories)
		{
			Location centerlocation = factory.getCenterLocation();
			Location inventoryLoctation = factory.getInventoryLocation();
			Location powerLocation = factory.getPowerSourceLocation();
			
			try {
				writer.append(factory.getSubFactoryType());
				writer.append(" ");

				for (ProductionRecipe recipe : factory.getRecipes())
				{
					writer.append(String.valueOf(recipe.getTitle()));
					writer.append(",");
				}
				writer.append(" ");
				
				writer.append(centerlocation.getWorld().getName());
				writer.append(" ");
				writer.append(Integer.toString(centerlocation.getBlockX()));
				writer.append(" ");
				writer.append(Integer.toString(centerlocation.getBlockY()));
				writer.append(" ");
				writer.append(Integer.toString(centerlocation.getBlockZ()));
				writer.append(" ");
				
				writer.append(Integer.toString(inventoryLoctation.getBlockX()));
				writer.append(" ");
				writer.append(Integer.toString(inventoryLoctation.getBlockY()));
				writer.append(" ");
				writer.append(Integer.toString(inventoryLoctation.getBlockZ()));
				writer.append(" ");
				
				writer.append(Integer.toString(powerLocation.getBlockX()));
				writer.append(" ");
				writer.append(Integer.toString(powerLocation.getBlockY()));
				writer.append(" ");
				writer.append(Integer.toString(powerLocation.getBlockZ()));
				writer.append(" ");
				
				writer.append(Boolean.toString(factory.getActive()));
				writer.append(" ");
				writer.append(Integer.toString(factory.getProductionTimer()));
				writer.append(" ");
				writer.append(Integer.toString(factory.getEnergyTimer()));
				writer.append(" ");
				writer.append(Integer.toString(factory.getCurrentRecipeNumber()));
				writer.append(" ");
				writer.append(Double.toString(factory.getCurrentRepair()));
				writer.append(" ");
				writer.append(String.valueOf(factory.getTimeDisrepair()));
				writer.newLine();
			} catch (IOException e) {
				FactoryModPlugin.sendConsoleMessage(new StringBuilder("ERROR: Could not write to ")
					.append(mFile.getName()).append(" for factory ").append(factories.indexOf(factory)).append(": ").append(e.getMessage()).toString());
			}
		}
		
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			FactoryModPlugin.sendConsoleMessage(new StringBuilder("ERROR: Could not complete write to ")
			.append(mFile.getName()).append(": ").append(e.getMessage()).toString());
		}
		
	}
}
