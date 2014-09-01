package com.github.igotyou.FactoryMod.properties;


import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;


public class NetherFactoryProperties
{
	private ItemList<NamedItemStack> constructionMaterials;
	private ItemList<NamedItemStack> fuel;
	private ItemList<NamedItemStack> repairMaterials;
	private int energyTime;
	private String name;
	private int repair;
	private double repairTime;
	private int scalingMode;
	private int scalingRadius;
	private int costScalingRadius;
	private boolean useFuelOnTeleport;
	
	public NetherFactoryProperties(ItemList<NamedItemStack> constructionMaterials,	ItemList<NamedItemStack> fuel, ItemList<NamedItemStack> repairMaterials,
			int energyTime, String name,int repair, double repairTime, int scalingRadius, boolean useFuelOnTeleport, int costScalingRadius)
	{
		this.constructionMaterials = constructionMaterials;
		this.fuel = fuel;
		this.repairMaterials = repairMaterials;
		this.energyTime = energyTime;
		this.name = name;
		this.repair=repair;
		this.repairTime=repairTime;
		this.scalingRadius = scalingRadius;
		this.costScalingRadius = costScalingRadius;
		this.useFuelOnTeleport = useFuelOnTeleport;
	}

	public int getRepair()
	{
		return repair;
	}
	
	public int getScalingRadius()
	{
		return scalingRadius;
	}
	
	public int getCostScalingRadius()
	{
		return costScalingRadius;
	}
	
	//0 == no scaling, 1==linear scaling, 2==exponential scaling
	public int getScalingMode()
	{
		return scalingMode;
	}

	public ItemList<NamedItemStack> getConstructionMaterials() 
	{
		return constructionMaterials;
	}
	
	public ItemList<NamedItemStack> getFuel()
	{
		return fuel;
	}
	
	public ItemList<NamedItemStack> getRepairMaterials()
	{
		return repairMaterials;
	}
	
	public int getEnergyTime()
	{
		return energyTime;
	}
	
	public String getName()
	{
		return name;
	}

	public static NetherFactoryProperties fromConfig(FactoryModPlugin plugin, ConfigurationSection configNetherFactory) 
	{
		ItemList<NamedItemStack> nfFuel=plugin.getItems(configNetherFactory.getConfigurationSection("fuel"));
		if(nfFuel.isEmpty())
		{
			nfFuel=new ItemList<NamedItemStack>();
			nfFuel.add(new NamedItemStack(Material.getMaterial("COAL"),1,(short)1,"Charcoal"));
		}
		ConfigurationSection costs = configNetherFactory.getConfigurationSection("costs");
		ItemList<NamedItemStack> nfConstructionCost=plugin.getItems(costs.getConfigurationSection("construction"));
		ItemList<NamedItemStack> nfRepairCost=plugin.getItems(costs.getConfigurationSection("repair"));
		int nfEnergyTime = configNetherFactory.getInt("fuel_time", 10);
		int nfRepair = costs.getInt("repair_multiple",1);
		String nfName = configNetherFactory.getString("name", "Nether Factory");
		int repairTime = configNetherFactory.getInt("repair_time",12);
		int nfScalingRadius = configNetherFactory.getInt("scaling_radius", 5000);
		int costScalingRadius = configNetherFactory.getInt("scaling_radius", 5000);
		boolean nfUseFuelOnTeleport = configNetherFactory.getBoolean("use_fuel_on_teleport", false);
		return new NetherFactoryProperties(nfConstructionCost, nfFuel, nfRepairCost, nfEnergyTime, nfName, nfRepair, repairTime, nfScalingRadius,nfUseFuelOnTeleport, costScalingRadius);

	}

	public double getRepairTime() 
	{
		return repairTime;
	}

	public boolean getUseFuelOnTeleport() 
	{
		return useFuelOnTeleport;
	}

}
