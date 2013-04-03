package com.github.igotyou.FactoryMod.managers;

import java.io.File;
import java.io.IOException;

import org.bukkit.Location;

import com.github.igotyou.FactoryMod.interfaces.Factory;
import com.github.igotyou.FactoryMod.interfaces.Manager;
import com.github.igotyou.FactoryMod.utility.InteractionResponse;

public class PowerManager implements Manager
{
	
	public void save(File file) throws IOException 
	{
		
	}

	public void load(File file) throws IOException 
	{

	}

	public void updateFactorys() 
	{
		
	}

	public InteractionResponse createFactory(Location factoryLocation, Location inventoryLocation, Location powerLocation) 
	{
		return null;
	}

	@Override
	public InteractionResponse addFactory(Factory factory) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Factory getFactory(Location factoryLocation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean factoryExistsAt(Location factoryLocation) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean factoryWholeAt(Location factoryLocation) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void removeFactory(Factory factory) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getSavesFileName() {
		// TODO Auto-generated method stub
		return null;
	}

}
