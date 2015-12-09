package com.github.igotyou.FactoryMod.persistence;

import java.io.File;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.IFactory;
import com.github.igotyou.FactoryMod.classicTriblockFactory.Compactor;
import com.github.igotyou.FactoryMod.classicTriblockFactory.NetherFactory;
import com.github.igotyou.FactoryMod.classicTriblockFactory.PrintingPress;
import com.github.igotyou.FactoryMod.classicTriblockFactory.ProductionFactory;
import com.github.igotyou.FactoryMod.classicTriblockFactory.RepairFactory;
import com.github.igotyou.FactoryMod.managers.CompactorManager;
import com.github.igotyou.FactoryMod.managers.IManager;
import com.github.igotyou.FactoryMod.managers.NetherFactoryManager;
import com.github.igotyou.FactoryMod.managers.PrintingPressManager;
import com.github.igotyou.FactoryMod.managers.ProductionFactoryManager;
import com.github.igotyou.FactoryMod.managers.RepairFactoryManager;

public class PersistenceFactory {
	
	//TODO: use type inference for return
	@SuppressWarnings("unchecked")
	public static <T extends IFactory> FactoryDao<T> getFactoryDao(IManager<T> factoryManager) {
		
		if(FactoryModPlugin.PERSISTENCE_FORMAT.toLowerCase().equals("txt")) {
			if(factoryManager instanceof ProductionFactoryManager) {
				File saveFile = new File(factoryManager.getPlugin().getDataFolder(), FactoryModPlugin.PRODUCTION_FACTORY_SAVE_FILE+".txt");
				return (FactoryDao<T>) new FactoryDao<ProductionFactory>(new ProductionCsvReader(factoryManager.getPlugin(), saveFile), new ProductionCsvWriter(saveFile));
			} else if(factoryManager instanceof PrintingPressManager) {
				File saveFile = new File(factoryManager.getPlugin().getDataFolder(), FactoryModPlugin.PRINTING_PRESSES_SAVE_FILE+".txt");
				return (FactoryDao<T>) new FactoryDao<PrintingPress>(new PrintingPressCsvReader(factoryManager.getPlugin(), saveFile), new PrintingPressCsvWriter(saveFile));
			} else if(factoryManager instanceof NetherFactoryManager) {
				File saveFile = new File(factoryManager.getPlugin().getDataFolder(), FactoryModPlugin.NETHER_FACTORY_SAVE_FILE+".txt");
				return (FactoryDao<T>) new FactoryDao<NetherFactory>(new NetherCsvReader(factoryManager.getPlugin(), saveFile), new NetherCsvWriter(saveFile));
			} else if(factoryManager instanceof RepairFactoryManager) {
				File saveFile = new File(factoryManager.getPlugin().getDataFolder(), FactoryModPlugin.REPAIR_FACTORY_SAVE_FILE+".txt");
				return (FactoryDao<T>) new FactoryDao<RepairFactory>(new RepairCsvReader(factoryManager.getPlugin(), saveFile), new RepairCsvWriter(saveFile));
			} else if(factoryManager instanceof CompactorManager) {
				File saveFile = new File(factoryManager.getPlugin().getDataFolder(), FactoryModPlugin.COMPACTOR_SAVE_FILE+".txt");
				return (FactoryDao<T>) new FactoryDao<Compactor>(new CompactorCsvReader(factoryManager.getPlugin(), saveFile), new CompactorCsvWriter(saveFile));
			}
			FactoryModPlugin.sendConsoleMessage("ERROR: Unsupported factory manager: " + factoryManager.getClass().getName());
			return null;
		}
		
		FactoryModPlugin.sendConsoleMessage("ERROR: Unsupported file format: " + FactoryModPlugin.PERSISTENCE_FORMAT);
		return null;
	}

}
