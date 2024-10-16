package com.devotedmc.ExilePearl.storage;

import com.devotedmc.ExilePearl.ExilePearl;
import java.util.Collection;

public interface PearlStorage extends PearlUpdateStorage {

	/**
	 * Loads all the prison pearls
	 * @return The collection of pearls
	 */
	public Collection<ExilePearl> loadAllPearls();

	/**
	 * Inserts a new prison pearl into storage
	 * @param pearl The pearl to insert
	 */
	public void pearlInsert(ExilePearl pearl);

	/**
	 * Removes a pearl from storage
	 * @param pearl The pearl to remove
	 */
	public void pearlRemove(ExilePearl pearl);
}
