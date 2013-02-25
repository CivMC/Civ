package com.github.igotyou.FactoryMod.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.Production;
import com.github.igotyou.FactoryMod.interfaces.Factory;
import com.github.igotyou.FactoryMod.interfaces.Manager;
import com.github.igotyou.FactoryMod.properties.ProductionProperties;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;

public class ProductionManager implements Manager
{
	private FactoryModPlugin plugin;
	private List<Production> producers;
	
	public ProductionManager(FactoryModPlugin plugin)
	{
		this.plugin = plugin;
		producers = new ArrayList<Production>();
	}
	
	public void save(File file) throws IOException 
	{
		// TODO Auto-generated method stub
		
	}

	public void load(File file) throws IOException 
	{
		// TODO Auto-generated method stub
		
	}

	public void updateFactorys() 
	{
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
		{
		    @Override  
		    public void run() 
		    {
		    	for (Production production: producers)
				{
					production.update();
				}
		    }
		}, 0L, FactoryModPlugin.PRODUCER_UPDATE_CYCLE);
	}

	public InteractionResponse createFactory(Location factoryLocation, Location inventoryLocation, Location powerSourceLocation) 
	{
		if (!factoryExistsAt(factoryLocation))
		{
			HashMap<String, ProductionProperties> properties = plugin.production_Properties;
			Block inventoryBlock = inventoryLocation.getBlock();
			Chest chest = (Chest) inventoryBlock.getState();
			Inventory chestInventory = chest.getInventory();
			String subFactoryType = null;
			boolean hasMaterials = true;
			for (Map.Entry<String, ProductionProperties> entry : properties.entrySet())
			{
				HashMap<Integer, Material> buildMaterial = entry.getValue().getBuildMaterial();
				FactoryModPlugin.sendConsoleMessage("build Material is: " + buildMaterial.toString());
				HashMap<Integer, Integer> buildAmount = entry.getValue().getBuildAmount();
				FactoryModPlugin.sendConsoleMessage("build amount is: " + buildAmount.toString());
				for (int i = 1; i <= buildMaterial.size(); i ++)
				{
					if(!chestInventory.contains(buildMaterial.get(i), buildAmount.get(i)))
					{
						hasMaterials = false;
					}
				}
				if (hasMaterials = true)
				{
					subFactoryType = entry.getKey();
				}
			}
			if (hasMaterials == true && subFactoryType != null)
			{
				Production production = new Production(factoryLocation, inventoryLocation, powerSourceLocation,subFactoryType);
				if (production.buildMaterialAvailable(properties.get(subFactoryType)))
				{
					addFactory(production);
					production.removeBuildMaterial(properties.get(subFactoryType));
					return new InteractionResponse(InteractionResult.SUCCESS, "Successfully created " + subFactoryType + " production factory");
				}
			}
			return new InteractionResponse(InteractionResult.FAILURE, "not enough materials in chest!");
		}
		return new InteractionResponse(InteractionResult.FAILURE, "there is already a factory there!");
	}

	public InteractionResponse addFactory(Factory factory) 
	{
		Production production = (Production) factory;
		if (production.getCenterLocation().getBlock().getType().equals(Material.WORKBENCH) && (!factoryExistsAt(production.getCenterLocation()))
				|| !factoryExistsAt(production.getInventoryLocation()) || !factoryExistsAt(production.getPowerSourceLocation()))
		{
			producers.add(production);
			return new InteractionResponse(InteractionResult.SUCCESS, "");
		}
		else
		{
			return new InteractionResponse(InteractionResult.FAILURE, "");
		}
	}

	public Factory r(Location factoryLocation) 
	{
		for (Production production : producers)
		{
			if (production.getCenterLocation().equals(factoryLocation) || production.getInventoryLocation().equals(factoryLocation)
					|| production.getPowerSourceLocation().equals(factoryLocation))
				return production;
		}
		return null;
	}

	public boolean factoryExistsAt(Location factoryLocation) 
	{
		Location westLocation = factoryLocation.clone();
		Location eastLocation = factoryLocation.clone();
		Location northLocation = factoryLocation.clone();
		Location southLocation = factoryLocation.clone();
		
		westLocation.add(-1,0,0);
		eastLocation.add(1,0,0);
		northLocation.add(0,0,-1);
		southLocation.add(0,0,1);
		
		boolean returnValue = false;
		if (getFactory(factoryLocation) != null)
		{
			returnValue = true;
		}

		if (getFactory(westLocation) != null)
		{
			returnValue = true;
		}
		if (getFactory(eastLocation) != null)
		{
			returnValue = true;
		}
		if (getFactory(northLocation) != null)
		{
			returnValue = true;
		}
		if (getFactory(southLocation) != null)
		{
			returnValue = true;
		}
		return returnValue;
	}

	public void removeFactory(Factory factory) 
	{
		producers.remove((Production)factory);
	}

	public String getSavesFileName() 
	{
		return FactoryModPlugin.PRODUCTION_SAVES_FILE;
	}

}
