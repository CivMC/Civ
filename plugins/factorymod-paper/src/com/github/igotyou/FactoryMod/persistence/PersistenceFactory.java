package com.github.igotyou.FactoryMod.persistence;

import java.io.File;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.Factorys.ProductionFactory;
import com.github.igotyou.FactoryMod.interfaces.Manager;
import com.github.igotyou.FactoryMod.managers.NetherFactoryManager;
import com.github.igotyou.FactoryMod.managers.PrintingPressManager;
import com.github.igotyou.FactoryMod.managers.ProductionManager;

public class PersistenceFactory {
	
	//TODO: use type inference for return
	public static FactoryDao<?> getFactoryDao(Manager factoryManager, File saveFile, String saveFormat) {
		
		if(saveFormat.toLowerCase().equals("txt")) {
			if(factoryManager instanceof ProductionManager) {
				return new FactoryDao<ProductionFactory>(new ProductionCsvReader(factoryManager.getPlugin(), saveFile), new ProductionCsvWriter(saveFile));
			} else if(factoryManager instanceof PrintingPressManager) {
				//TODO: add printing press support
			} else if(factoryManager instanceof NetherFactoryManager) {
				//TODO: add nether factory support
			}
			FactoryModPlugin.sendConsoleMessage("ERROR: Unsupported factory manager: " + factoryManager.getClass().getName());
			return null;
		}
		
		FactoryModPlugin.sendConsoleMessage("ERROR: Unsupported file format: " + saveFormat);
		return null;
	}

}
