package com.devotedmc.ExilePearl.database;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import com.devotedmc.ExilePearl.ExilePearl;

public class MySqlStorage implements PearlStorage {
	
	public MySqlStorage() {
		
	}

	@Override
	public Collection<ExilePearl> pearlGetall() {
		HashSet<ExilePearl> pearls = new HashSet<ExilePearl>();
		// TODO Auto-generated method stub
		return pearls;
	}

	@Override
	public void pearlInsert(ExilePearl pearl) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pearlUpdate(ExilePearl pearl) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pearlUpdateSummoned(ExilePearl pearl) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pearlUpdateReturnLocation(ExilePearl pearl) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pearlRemove(ExilePearl pearl) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateLastFeedTime(Date now) {
		// TODO Auto-generated method stub

	}

	@Override
	public Date getLastFeedTime() {
		// TODO Auto-generated method stub
		return new Date();
	}

}
