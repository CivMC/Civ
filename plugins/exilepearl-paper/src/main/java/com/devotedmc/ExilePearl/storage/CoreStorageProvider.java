package com.devotedmc.ExilePearl.storage;

import java.util.logging.Level;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.StorageProvider;

import vg.civcraft.mc.civmodcore.util.Guard;

public class CoreStorageProvider implements StorageProvider {

	private final ExilePearlApi pearlApi;
	private final PearlFactory pearlFactory;
	
	private PluginStorage storage;
	
	public CoreStorageProvider(final ExilePearlApi pearlApi, final PearlFactory pearlFactory) {
		Guard.ArgumentNotNull(pearlApi, "pearlApi");
		Guard.ArgumentNotNull(pearlFactory, "pearlFactory");
		
		this.pearlApi = pearlApi;
		this.pearlFactory = pearlFactory;
	}
	
	public PluginStorage createStorage() {
		if (storage != null) {
			throw new RuntimeException("Can't re-create the storage instance.");
		}
		
		if (pearlApi.getPearlConfig().getUseDevRamStorage()) {
			pearlApi.log(Level.WARNING, "Using RAM storage instance. Data will not be saved.");
			storage = new RamStorage();
		} else {
			storage = new AsyncStorageWriter(new MySqlStorage(pearlFactory, pearlApi, pearlApi.getPearlConfig()), pearlApi);
		}
		return storage;
	}

	@Override
	public PluginStorage getStorage() {
		return storage;
	}
}
