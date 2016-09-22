package com.devotedmc.ExilePearl.storage;

import java.util.Collection;
import java.util.Date;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.util.Guard;

public class MySqlStorageAsync implements PluginStorage, Runnable {

	private final PluginStorage storage;
	
	public MySqlStorageAsync(final PluginStorage storage) {
		Guard.ArgumentNotNull(storage, "storage");
		
		this.storage = storage;
	}
	
	@Override
	public Collection<ExilePearl> loadAllPearls() {
		return storage.loadAllPearls();
	}

	@Override
	public void pearlInsert(ExilePearl pearl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pearlRemove(ExilePearl pearl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pearlUpdateLocation(ExilePearl pearl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateLastFeedTime(Date now) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Date getLastFeedTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pearlUpdateStrength(ExilePearl strength) {
		// TODO Auto-generated method stub
		
	}

}
