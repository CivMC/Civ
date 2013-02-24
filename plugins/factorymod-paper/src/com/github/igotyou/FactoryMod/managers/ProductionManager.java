package com.github.igotyou.FactoryMod.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.FactoryObject.SubFactoryType;
import com.github.igotyou.FactoryMod.Factorys.Production;
import com.github.igotyou.FactoryMod.properties.ProductionProperties;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;
import com.github.igotyou.interfaces.Factory;
import com.github.igotyou.interfaces.Manager;

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

	public InteractionResponse createFactory(Location factoryLocation, Location inventoryLocation, Location powerLocation) 
	{
		if (!factoryExistsAt(factoryLocation))
		{
			HashMap<SubFactoryType, ProductionProperties> properties = plugin.Production_Properties;
			Chest chest = (Chest)inventoryLocation.getBlock();
			Inventory inventory = chest.getInventory();
			SubFactoryType subFactoryType = null;
			boolean hasMaterials = true;
			for (Map.Entry<SubFactoryType, ProductionProperties> entry : properties.entrySet())
			{
				HashMap<Integer, Material> buildMaterial = entry.getValue().getBuildMaterial();
				HashMap<Integer, Integer> buildAmount = entry.getValue().getBuildAmount();
				for (int i = 0; i < buildMaterial.size(); i ++)
				{
					if(!inventory.contains(buildMaterial.get(i), buildAmount.get(i)))
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
				Production production = new Production(factoryLocation, inventoryLocation, powerLocation,subFactoryType);
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
		// TODO Auto-generated method stub
		return null;
	}

	public Factory getFactory(Location factoryLocation) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean factoryExistsAt(Location factoryLocation) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void removeFactory(Factory factory) 
	{
		// TODO Auto-generated method stub
		
	}

	public String getSavesFileName() 
	{
		// TODO Auto-generated method stub
		return null;
	}

}
