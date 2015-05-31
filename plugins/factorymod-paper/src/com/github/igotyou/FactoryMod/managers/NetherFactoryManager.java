package com.github.igotyou.FactoryMod.managers;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.NetherFactory;
import com.github.igotyou.FactoryMod.interfaces.Manager;
import com.github.igotyou.FactoryMod.persistence.FactoryDao;
import com.github.igotyou.FactoryMod.persistence.FileBackup;
import com.github.igotyou.FactoryMod.persistence.PersistenceFactory;
import com.github.igotyou.FactoryMod.properties.NetherFactoryProperties;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;
import com.github.igotyou.FactoryMod.utility.StringUtils;
import com.google.common.collect.Lists;

//original file:
/**
* Manager.java
* Purpose: Interface for Manager objects for basic manager functionality
*
* @author MrTwiggy
* @version 0.1 1/08/13
*/
//edited version:
/**
* Manager.java	 
* Purpose: Interface for Manager objects for basic manager functionality
* @author igotyou
*
*/

public class NetherFactoryManager implements Manager<NetherFactory>
{
	private ReinforcementManager rm = Citadel.getReinforcementManager();
	private FactoryModPlugin plugin;
	private List<NetherFactory> netherFactorys;
	private FactoryDao<NetherFactory> mDao;
	private File mSaveFile;
	private long repairTime;
	private boolean isLogging = true;
	
	public NetherFactoryManager(FactoryModPlugin plugin)
	{
		this.plugin = plugin;
		mSaveFile = new File(plugin.getDataFolder(), "netherSaves.txt");
		netherFactorys = Lists.newArrayList();
		//Set maintenance clock to 0
		updateFactorys();
		mDao = PersistenceFactory.getFactoryDao(this, mSaveFile, "txt");
	}
	
	@Override
	public void save()
	{
		updateRepair(System.currentTimeMillis() - repairTime);
		repairTime = System.currentTimeMillis();

		FileBackup.backup(mSaveFile);
		mDao.writeFactories(netherFactorys);
	}

	@Override
	public void load()
	{
		isLogging = false;
		repairTime = System.currentTimeMillis();
		for(NetherFactory factory : mDao.readFactories()) {
			addFactory(factory);
		}
		isLogging = true;
	}

	@Override
	public void updateFactorys() 
	{
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				for (NetherFactory factory: netherFactorys)
				{
					factory.update();
				}
			}
		}, 0L, FactoryModPlugin.PRODUCER_UPDATE_CYCLE);
	}

	@Override
	public InteractionResponse createFactory(Location factoryLocation, Location inventoryLocation, Location powerSourceLocation) 
	{
		NetherFactoryProperties netherFactoryProperties = plugin.getNetherFactoryProperties();
		Block inventoryBlock = inventoryLocation.getBlock();
		Chest chest = (Chest) inventoryBlock.getState();
		Inventory chestInventory = chest.getInventory();
		ItemList<NamedItemStack> constructionMaterials = netherFactoryProperties.getConstructionMaterials();
		if(constructionMaterials.oneIn(chestInventory))
		{
			if (factoryLocation.getWorld().getName().equalsIgnoreCase(FactoryModPlugin.WORLD_NAME))
			{
				if (factoryLocation.getBlock().getType().equals(FactoryModPlugin.CENTRAL_BLOCK_MATERIAL))
				{
					if (!factoryExistsAt(factoryLocation))
					{
						double scalingFactor = getScalingFactor(factoryLocation);
						if (scalingFactor < 500)
						{
							constructionMaterials = constructionMaterials.getMultiple(scalingFactor);
							boolean hasMaterials = constructionMaterials.allIn(chestInventory);
							if (hasMaterials)
							{
								boolean markerFound = false;
								Location markerLocation = factoryLocation.clone();
								int blockY = markerLocation.getBlockY();
								for (int centerY = blockY - FactoryModPlugin.NETHER_MARKER_MAX_DISTANCE; centerY <= blockY + FactoryModPlugin.NETHER_MARKER_MAX_DISTANCE && !markerFound; centerY++)
								{
									markerLocation.setY(centerY);
									Location oneUp = markerLocation.clone();
									oneUp.setY(centerY+1);
									if (markerLocation.getBlock().getType() == FactoryModPlugin.NETHER_FACTORY_MARKER_MATERIAL && oneUp.getBlock().getType() == FactoryModPlugin.NETHER_FACTORY_TELEPORT_PLATFORM_MATERIAL)
									{
										markerFound = true;
									}
								}
								if (markerFound)
								{
									int nether_scale = FactoryModPlugin.NETHER_SCALE;
									boolean locationOk = false;
									int startX = Math.round(factoryLocation.getBlockX()/nether_scale);
									int startY = factoryLocation.getBlockY();
									int startZ = Math.round(factoryLocation.getBlockZ()/nether_scale);
									Location netherLocation = new Location(Bukkit.getWorld(FactoryModPlugin.NETHER_NAME), startX,startY,startZ);
									Location netherLocation1 = new Location(Bukkit.getWorld(FactoryModPlugin.NETHER_NAME), startX,startY+1,startZ);
									Location netherLocation2 = new Location(Bukkit.getWorld(FactoryModPlugin.NETHER_NAME), startX,startY+2,startZ);
									Location netherLocation3 = new Location(Bukkit.getWorld(FactoryModPlugin.NETHER_NAME), startX,startY+3,startZ);				
									if (FactoryModPlugin.CITADEL_ENABLED && (rm.isReinforced(netherLocation) || rm.isReinforced(netherLocation1) || rm.isReinforced(netherLocation2) || rm.isReinforced(netherLocation3)))
									{
										for(int scanX = startX-1; scanX <= startX+1 && !locationOk; scanX++)
										{
											
											for(int scanZ = startZ-1; scanZ <= startZ+1 && !locationOk; scanZ++)
											{
												for(int scanY = startY; scanY <= 250 && !locationOk; scanY++)
												{
													netherLocation = new Location(Bukkit.getWorld(FactoryModPlugin.NETHER_NAME), scanX,scanY,scanZ);
													netherLocation1 = new Location(Bukkit.getWorld(FactoryModPlugin.NETHER_NAME), scanX,scanY+1,scanZ);
													netherLocation2 = new Location(Bukkit.getWorld(FactoryModPlugin.NETHER_NAME), scanX,scanY+2,scanZ);
													netherLocation3 = new Location(Bukkit.getWorld(FactoryModPlugin.NETHER_NAME), scanX,scanY+3,scanZ);
													if(!rm.isReinforced(netherLocation) && !rm.isReinforced(netherLocation1) && !rm.isReinforced(netherLocation2) && !rm.isReinforced(netherLocation3))
													{
														locationOk = true;
														
													}
												}
											}
										}
									}
									if (!factoryExistsAt(netherLocation))
									{
										netherLocation.getBlock().setType(FactoryModPlugin.NETHER_FACTORY_TELEPORT_PLATFORM_MATERIAL);
										netherLocation.getBlock().getState().update(true);
										netherLocation1.getBlock().setType(Material.AIR);
										netherLocation1.getBlock().getState().update(true);
										netherLocation2.getBlock().setType(Material.AIR);
										netherLocation2.getBlock().getState().update(true);
										netherLocation3.getBlock().setType(Material.AIR);
										netherLocation3.getBlock().getState().update(true);
										if(netherLocation.getBlock().getType() != (FactoryModPlugin.NETHER_FACTORY_TELEPORT_PLATFORM_MATERIAL) && 
												netherLocation1.getBlock().getType() != Material.AIR &&
												netherLocation2.getBlock().getType() != Material.AIR &&
												netherLocation3.getBlock().getType() != Material.AIR)
										{
											return new InteractionResponse(InteractionResult.FAILURE, "For some reason the nether side obsidian block did not generate...blame bukkit");
										}
										Location oneUp = markerLocation.clone();
										oneUp.add(0,1,0);
										NetherFactory netherFactory = new NetherFactory(factoryLocation, inventoryLocation, powerSourceLocation, netherLocation, oneUp, plugin.getNetherFactoryProperties(), this);
										if (constructionMaterials.removeFrom(netherFactory.getInventory()))
										{
											addFactory(netherFactory);
											return new InteractionResponse(InteractionResult.SUCCESS, "Successfully created " + netherFactoryProperties.getName());
										}
									}
									else
									{
										return new InteractionResponse(InteractionResult.FAILURE, "There is a other " + netherFactoryProperties.getName() + " too close.");
									}
								}
								else
								{
									return new InteractionResponse(InteractionResult.FAILURE, "No marker found. Place a " + FactoryModPlugin.NETHER_FACTORY_MARKER_MATERIAL + " 1-20 blocks above the center block of the nether factory with a " + FactoryModPlugin.NETHER_FACTORY_TELEPORT_PLATFORM_MATERIAL + " right above.");
								}
							}
							return new InteractionResponse(InteractionResult.FAILURE, "Not enough materials in chest! You need " + constructionMaterials.toString());
						}
						else
						{
							return new InteractionResponse(InteractionResult.FAILURE, "Factory is too close to a other nether factory!");
						}
					}
					return new InteractionResponse(InteractionResult.FAILURE, "There is already a " + netherFactoryProperties.getName() + " there!");
				}
				else
				{
					return new InteractionResponse(InteractionResult.FAILURE, "Wrong center block!");
				}
			}
			else
			{
				return new InteractionResponse(InteractionResult.FAILURE, netherFactoryProperties.getName() + "'s can only be built in the overworld");
			}
		}
		return new InteractionResponse(InteractionResult.FAILURE, "No factory was identified!");
	}

	@Override
	public InteractionResponse addFactory(NetherFactory factory) 
	{
		if (factory.getCenterLocation().getBlock().getType().equals(FactoryModPlugin.CENTRAL_BLOCK_MATERIAL) && 
				(!factoryExistsAt(factory.getCenterLocation())
				|| !factoryExistsAt(factory.getInventoryLocation()) 
				|| !factoryExistsAt(factory.getPowerSourceLocation())
				|| !factoryExistsAt(factory.getNetherTeleportPlatform())
				|| !factoryExistsAt(factory.getOverworldTeleportPlatform()) ))
		{
			netherFactorys.add(factory);
			if (isLogging) { FactoryModPlugin.sendConsoleMessage("Nether factory created: " + factory.getProperties().getName()); }
			return new InteractionResponse(InteractionResult.SUCCESS, "");
		}
		else
		{
			FactoryModPlugin.sendConsoleMessage("Nether factory failed to create: " + factory.getProperties().getName());
			return new InteractionResponse(InteractionResult.FAILURE, "");
		}
	}

	@Override
	public NetherFactory getFactory(Location factoryLocation) 
	{
		for (NetherFactory factory : netherFactorys)
		{
			if (factory.getCenterLocation().equals(factoryLocation) 
					|| factory.getInventoryLocation().equals(factoryLocation)
					|| factory.getPowerSourceLocation().equals(factoryLocation)
					|| factory.getNetherTeleportPlatform().equals(factoryLocation)
					|| factory.getOverworldTeleportPlatform().equals(factoryLocation))
				return factory;
		}
		return null;
	}

	@Override
	public boolean factoryExistsAt(Location factoryLocation) 
	{
		boolean returnValue = false;
		if (getFactory(factoryLocation) != null)
		{
			returnValue = true;
		}
		return returnValue;
	}

	@Override
	public boolean factoryWholeAt(Location factoryLocation) 
	{
		boolean returnValue = false;
		if (getFactory(factoryLocation) != null)
		{
			returnValue = getFactory(factoryLocation).isWhole(false);
		}
		return returnValue;
	}

	@Override
	public void removeFactory(NetherFactory factory) 
	{		
		FactoryModPlugin.sendConsoleMessage(new StringBuilder("Nether factory removed: ")
			.append(factory.getProperties().getName())
			.append(" at ")
			.append(StringUtils.formatCoords(factory.getCenterLocation()))
			.toString());
		
		netherFactorys.remove(factory);
		
	}

	public void updateRepair(long time)
	{
		for (NetherFactory factory : netherFactorys)
		{
			factory.updateRepair(time / ((double)FactoryModPlugin.REPAIR_PERIOD));
		}
		long currentTime = System.currentTimeMillis();
		Iterator<NetherFactory> itr = netherFactorys.iterator();
		while(itr.hasNext())
		{
			NetherFactory factory = itr.next();
			if(currentTime > (factory.getTimeDisrepair() + FactoryModPlugin.DISREPAIR_PERIOD))
			{
				FactoryModPlugin.sendConsoleMessage(new StringBuilder("Nether factory removed due to disrepair: ")
					.append(factory.getProperties().getName())
					.append(" at ")
					.append(StringUtils.formatCoords(factory.getCenterLocation()))
					.toString());
				
				itr.remove();
				
			}
		}
	}

	@Override
	public String getSavesFileName() 
	{
		return FactoryModPlugin.NETHER_FACTORY_SAVE_FILE;
	}

	public double getScalingFactor(Location location)
	{
		double scalingFactor = 1;
		NetherFactoryProperties properties = plugin.getNetherFactoryProperties();
		for (NetherFactory factory : netherFactorys)
		{
			Location factoryLoc = factory.getCenterLocation();
			if(factory.getCenterLocation().equals(location))
			{
				continue;
			}
			//the distance function uses square root, which is quite expensive, let's check if it's even realistic that it's within range first.
			if ((location.getBlockX()-factoryLoc.getBlockX()) < properties.getScalingRadius() || (location.getBlockX()-factoryLoc.getBlockX()) > -(properties.getScalingRadius()))
			{
				if ((location.getBlockZ()-factoryLoc.getBlockZ()) < properties.getScalingRadius() || (location.getBlockZ()-factoryLoc.getBlockZ()) > -(properties.getScalingRadius()))
				{
					double distance = location.distance(factoryLoc);
					if (distance <= properties.getScalingRadius())
					{
						scalingFactor = scalingFactor * Math.exp(1/(distance/properties.getCostScalingRadius()));
					}
				}
			}
		}
		return scalingFactor;
	}

	@Override
	public FactoryModPlugin getPlugin() {
		return plugin;
	}

	@Override
	public Class<NetherFactory> getFactoryType() {
		return NetherFactory.class;
	}		

}
