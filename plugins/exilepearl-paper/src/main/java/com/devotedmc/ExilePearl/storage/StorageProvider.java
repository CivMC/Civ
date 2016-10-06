package com.devotedmc.ExilePearl.storage;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.util.Guard;

public class StorageProvider {

	private final ExilePearlApi pearlApi;
	private final PearlFactory pearlFactory;
	
	public StorageProvider(final ExilePearlApi pearlApi, final PearlFactory pearlFactory) {
		Guard.ArgumentNotNull(pearlApi, "pearlApi");
		Guard.ArgumentNotNull(pearlFactory, "pearlFactory");
		
		this.pearlApi = pearlApi;
		this.pearlFactory = pearlFactory;
	}
	
	public PluginStorage createStorage() {
		if (pearlApi.getPearlConfig().getUseDevRamStorage()) {
			return new RamStorage();
		} else {
			return new AsyncStorageWriter(new MySqlStorage(pearlFactory, pearlApi, pearlApi.getPearlConfig()), pearlApi);
		}
	}
}
