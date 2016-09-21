package com.devotedmc.ExilePearl.database;

import java.util.Collection;
import java.util.Date;

import com.devotedmc.ExilePearl.ExilePearl;

public interface PearlStorage {
	public Collection<ExilePearl> pearlGetall();
	public void pearlInsert(ExilePearl pearl);
	public void pearlUpdate(ExilePearl pearl);
	public void pearlUpdateSummoned(ExilePearl pearl);
	public void pearlUpdateReturnLocation(ExilePearl pearl);
	public void pearlRemove(ExilePearl pearl);

	public void updateLastFeedTime(Date now);
	public Date getLastFeedTime();
}
