package com.github.igotyou.FactoryMod.persistence;

import java.io.File;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.NetherFactory;
import com.github.igotyou.FactoryMod.Factorys.PrintingPress;
import com.github.igotyou.FactoryMod.Factorys.ProductionFactory;
import com.github.igotyou.FactoryMod.interfaces.Factory;
import com.github.igotyou.FactoryMod.interfaces.Manager;
import com.github.igotyou.FactoryMod.managers.NetherFactoryManager;
import com.github.igotyou.FactoryMod.managers.PrintingPressManager;
import com.github.igotyou.FactoryMod.managers.ProductionManager;

public class PersistenceFactory {
	
	//TODO: use type inference for return
	public static <T extends Factory> FactoryDao<T> getFactoryDao(Manager<T> factoryManager, File saveFile, String saveFormat) {
		
		if(saveFormat.toLowerCase().equals("txt")) {
			if(factoryManager instanceof ProductionManager) {
				return (FactoryDao<T>) new FactoryDao<ProductionFactory>(new ProductionCsvReader(factoryManager.getPlugin(), saveFile), new ProductionCsvWriter(saveFile));
			} else if(factoryManager instanceof PrintingPressManager) {
				return (FactoryDao<T>) new FactoryDao<PrintingPress>(new PrintingPressCsvReader(factoryManager.getPlugin(), saveFile), new PrintingPressCsvWriter(saveFile));
			} else if(factoryManager instanceof NetherFactoryManager) {
				return (FactoryDao<T>) new FactoryDao<NetherFactory>(new NetherCsvReader(factoryManager.getPlugin(), saveFile), new NetherCsvWriter(saveFile));
			}
			FactoryModPlugin.sendConsoleMessage("ERROR: Unsupported factory manager: " + factoryManager.getClass().getName());
			return null;
		}
		
		FactoryModPlugin.sendConsoleMessage("ERROR: Unsupported file format: " + saveFormat);
		return null;
	}

}
