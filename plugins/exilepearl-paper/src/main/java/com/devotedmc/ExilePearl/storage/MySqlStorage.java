package com.devotedmc.ExilePearl.storage;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.util.Guard;

public class MySqlStorage implements PluginStorage {
	
	private final PearlFactory pearlFactory;
	
	private boolean isConnected = false;
	
	/**
	 * Creates a new MySqlStorage instance
	 * @param pearlFactory The pearl factory
	 */
	public MySqlStorage(PearlFactory pearlFactory) {
		Guard.ArgumentNotNull(pearlFactory, "pearlFactory");
	
		this.pearlFactory = pearlFactory;
	}

	@Override
	public boolean connect() {
		// TODO Auto-generated method stub
		
		isConnected = true;
		return true;
	}

	@Override
	public boolean disconnect() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isConnected() {
		return isConnected;
	}

	@Override
	public Collection<ExilePearl> loadAllPearls() {
		HashSet<ExilePearl> pearls = new HashSet<ExilePearl>();
		
		
		// TODO
		
		return pearls;
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
	public void pearlUpdateHealth(ExilePearl pearl) {
		// TODO Auto-generated method stub
		
	}

}
