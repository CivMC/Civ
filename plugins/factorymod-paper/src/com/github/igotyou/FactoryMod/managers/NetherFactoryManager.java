package com.github.igotyou.FactoryMod.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.NetherFactory;
import com.github.igotyou.FactoryMod.Factorys.NetherFactory.NetherOperationMode;
import com.github.igotyou.FactoryMod.interfaces.Factory;
import com.github.igotyou.FactoryMod.interfaces.Manager;
import com.github.igotyou.FactoryMod.properties.NetherFactoryProperties;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;
import com.github.igotyou.FactoryMod.utility.StringUtils;

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

public class NetherFactoryManager implements Manager
{
	private ReinforcementManager rm = Citadel.getReinforcementManager();
	private FactoryModPlugin plugin;
	private List<NetherFactory> netherFactorys;
	private long repairTime;
	private boolean isLogging = true;
	
	public NetherFactoryManager(FactoryModPlugin plugin)
	{
		this.plugin = plugin;
		netherFactorys = new ArrayList<NetherFactory>();
		//Set maintenance clock to 0
		updateFactorys();
	}
	
	public void save(File file) throws IOException 
	{
		//Takes difference between last repair update and current one and scales repair accordingly
		updateRepair(System.currentTimeMillis()-repairTime);
		repairTime=System.currentTimeMillis();
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
		int version = 1;
		oos.writeInt(version);
		oos.writeInt(netherFactorys.size());
		for (NetherFactory factory : netherFactorys)
		{
			Location centerlocation = factory.getCenterLocation();
			Location inventoryLocation = factory.getInventoryLocation();
			Location powerLocation = factory.getPowerSourceLocation();
			Location netherTeleportPlatformLocation = factory.getNetherTeleportPlatform();
			Location overworldTeleportPlatformLocation = factory.getOverworldTeleportPlatform();
			
			oos.writeUTF(centerlocation.getWorld().getName());
			
			oos.writeInt(centerlocation.getBlockX());
			oos.writeInt(centerlocation.getBlockY());
			oos.writeInt(centerlocation.getBlockZ());

			oos.writeInt(inventoryLocation.getBlockX());
			oos.writeInt(inventoryLocation.getBlockY());
			oos.writeInt(inventoryLocation.getBlockZ());

			oos.writeInt(powerLocation.getBlockX());
			oos.writeInt(powerLocation.getBlockY());
			oos.writeInt(powerLocation.getBlockZ());
			
			oos.writeInt(overworldTeleportPlatformLocation.getBlockX());
			oos.writeInt(overworldTeleportPlatformLocation.getBlockY());
			oos.writeInt(overworldTeleportPlatformLocation.getBlockZ());
			
			oos.writeUTF(netherTeleportPlatformLocation.getWorld().getName());
			oos.writeInt(netherTeleportPlatformLocation.getBlockX());
			oos.writeInt(netherTeleportPlatformLocation.getBlockY());
			oos.writeInt(netherTeleportPlatformLocation.getBlockZ());
			
			oos.writeBoolean(factory.getActive());
			oos.writeInt(factory.getMode().getId());
			oos.writeDouble(factory.getCurrentRepair());
			oos.writeLong(factory.getTimeDisrepair());

		}
		oos.flush();
		fileOutputStream.close();
	}

	public void load(File file) throws IOException 
	{
		isLogging = false;
		try {
			repairTime=System.currentTimeMillis();
			FileInputStream fileInputStream = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fileInputStream);
			int version = ois.readInt();
			assert(version == 1);
			int count = ois.readInt();
			int i = 0;
			for (i = 0; i < count; i++)
			{
				String worldName = ois.readUTF();
				World world = plugin.getServer().getWorld(worldName);

				Location centerLocation = new Location(world, ois.readInt(), ois.readInt(), ois.readInt());
				Location inventoryLocation = new Location(world, ois.readInt(), ois.readInt(), ois.readInt());
				Location powerLocation = new Location(world, ois.readInt(), ois.readInt(), ois.readInt());
				Location overworldTeleportPlatformLocation = new Location(world, ois.readInt(), ois.readInt(), ois.readInt());
				
				String worldName2 = ois.readUTF();
				World world2 = plugin.getServer().getWorld(worldName2);
				
				Location netherTeleportPlatformLocation = new Location(world2, ois.readInt(), ois.readInt(), ois.readInt());
				
				boolean active = ois.readBoolean();
				NetherOperationMode mode = NetherFactory.NetherOperationMode.byId(ois.readInt());
				double currentRepair = ois.readDouble();
				long timeDisrepair  = ois.readLong();
				
				NetherFactory factory = new NetherFactory(centerLocation, inventoryLocation, powerLocation, netherTeleportPlatformLocation, overworldTeleportPlatformLocation,
						active, currentRepair, timeDisrepair,
						mode,
						plugin.getNetherFactoryProperties(), this);
				addFactory(factory);
			}
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		isLogging = true;
	}

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

	public InteractionResponse addFactory(Factory factory) 
	{
		NetherFactory netherFactory = (NetherFactory) factory;
		if (netherFactory.getCenterLocation().getBlock().getType().equals(FactoryModPlugin.CENTRAL_BLOCK_MATERIAL) && 
				(!factoryExistsAt(netherFactory.getCenterLocation())
				|| !factoryExistsAt(netherFactory.getInventoryLocation()) 
				|| !factoryExistsAt(netherFactory.getPowerSourceLocation())
				|| !factoryExistsAt(netherFactory.getNetherTeleportPlatform())
				|| !factoryExistsAt(netherFactory.getOverworldTeleportPlatform()) ))
		{
			netherFactorys.add(netherFactory);
			if (isLogging) { FactoryModPlugin.sendConsoleMessage("Nether factory created: " + netherFactory.getProperties().getName()); }
			return new InteractionResponse(InteractionResult.SUCCESS, "");
		}
		else
		{
			FactoryModPlugin.sendConsoleMessage("Nether factory failed to create: " + netherFactory.getProperties().getName());
			return new InteractionResponse(InteractionResult.FAILURE, "");
		}
	}

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
	
	public boolean factoryExistsAt(Location factoryLocation) 
	{
		boolean returnValue = false;
		if (getFactory(factoryLocation) != null)
		{
			returnValue = true;
		}
		return returnValue;
	}
	
	public boolean factoryWholeAt(Location factoryLocation) 
	{
		boolean returnValue = false;
		if (getFactory(factoryLocation) != null)
		{
			returnValue = getFactory(factoryLocation).isWhole(false);
		}
		return returnValue;
	}

	public void removeFactory(Factory factory) 
	{
		if(!(factory instanceof NetherFactory)) {
			FactoryModPlugin.sendConsoleMessage("Could not remove unexpected factory type: " + factory.getClass().getName());
			return;
		}
		
		NetherFactory netherFactory = (NetherFactory)factory;
		
		FactoryModPlugin.sendConsoleMessage(new StringBuilder("Nether factory removed: ")
			.append(netherFactory.getProperties().getName())
			.append(" at ")
			.append(StringUtils.formatCoords(netherFactory.getCenterLocation()))
			.toString());
		
		netherFactorys.remove(netherFactory);
		
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
						scalingFactor = scalingFactor * ( Math.exp(1/(distance/properties.getCostScalingRadius())) - Math.exp(1.0) + 1.0 );
					}
				}
			}
		}
		return scalingFactor;
	}		

}
