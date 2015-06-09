package com.github.igotyou.FactoryMod.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.IFactory;
import com.github.igotyou.FactoryMod.Factorys.NetherFactory;
import com.github.igotyou.FactoryMod.persistence.FactoryDao;
import com.github.igotyou.FactoryMod.persistence.FileBackup;
import com.github.igotyou.FactoryMod.persistence.PersistenceFactory;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;
import com.github.igotyou.FactoryMod.utility.StringUtils;
import com.github.igotyou.FactoryMod.utility.InteractionResponse.InteractionResult;

public abstract class AManager<T extends IFactory> implements IManager<T>{

	protected FactoryModPlugin plugin;
	ArrayList<T> factories = new ArrayList<T>();

	protected FactoryDao<T> mDao;
	protected long repairTime;
	protected boolean isLogging = true;
	
	public AManager(FactoryModPlugin plugin)
	{
		this.plugin = plugin;
		//mSaveFile = new File(plugin.getDataFolder(), "productionSaves.txt");
		updateFactorys();
		mDao = PersistenceFactory.getFactoryDao(this);
	}
	
	public String getFactoryName(){
		return getFactoryType().getName();
		//return this.getClass().getName().replace("Manager", "");
	}

	@Override
	public FactoryModPlugin getPlugin() {
		return plugin;
	}


	@Override
	public void updateFactorys() 
	{
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
		{
			@Override
			public void run()
			{
				for (T factory: factories)
				{
					factory.update();
				}
			}
		}, 0L, FactoryModPlugin.PRODUCER_UPDATE_CYCLE);
	}
	
	public void save() 
	{
		//Takes difference between last repair update and current one and scales repair accordingly
		updateRepair(System.currentTimeMillis() - repairTime);
		repairTime = System.currentTimeMillis();
		
		mDao.writeFactories(factories);		
	}

	public void load()
	{
		isLogging = false;
		repairTime = System.currentTimeMillis();
		for(T press : mDao.readFactories()) {
			addFactory(press);
		}
		isLogging = true;
	}
	public void updateRepair(long time)
	{
		for (T factory : factories)
		{
			factory.updateRepair(time / ((double)FactoryModPlugin.REPAIR_PERIOD));
		}
		long currentTime = System.currentTimeMillis();
		Iterator<T> itr = factories.iterator();
		while(itr.hasNext())
		{
			T factory = itr.next();
			if(currentTime > (factory.getTimeDisrepair() + FactoryModPlugin.DISREPAIR_PERIOD))
			{
				FactoryModPlugin.sendConsoleMessage(new StringBuilder(getFactoryName()+" removed due to disrepair: ")
				.append(factory.getProperties().getName())
				.append(" at ")
				.append(StringUtils.formatCoords(factory.getCenterLocation()))
				.toString());
				
				itr.remove();
			}
		}
	}

	@Override
	public InteractionResponse addFactory(T factory) 
	{
		if (isClear(factory))
		{
			factories.add(factory);
			if (isLogging) { FactoryModPlugin.sendConsoleMessage(getFactoryName()+" created: " + factory.getProperties().getName()); }
			return new InteractionResponse(InteractionResult.SUCCESS, "");
		}
		else
		{
			FactoryModPlugin.sendConsoleMessage(getFactoryName()+" failed to create: " + factory.getProperties().getName());
			return new InteractionResponse(InteractionResult.FAILURE, "");
		}
	}

	@Override
	public boolean factoryExistsAt(Location factoryLocation) 
	{
		return getFactory(factoryLocation) != null;
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
	public void removeFactory(T factory) 
	{		
		FactoryModPlugin.sendConsoleMessage(new StringBuilder(getFactoryName() +" removed: ")
			.append(factory.getProperties().getName())
			.append(" at ")
			.append(StringUtils.formatCoords(factory.getCenterLocation()))
			.toString());
		
		factories.remove(factory);
		
	}
}
