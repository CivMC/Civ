package com.github.igotyou.FactoryMod.eggs;

import org.bukkit.entity.Player;

import com.github.igotyou.FactoryMod.Factory;
import com.github.igotyou.FactoryMod.multiBlockStructures.MultiBlockStructure;

/**
 * This represents the design pattern "Factory", but because that word was
 * already taken in the context of this plugin I had to come up with another
 * analogy instead, I decided to go with eggs, dont blame me. Any class
 * implementing this interface should be representing a specific combination of
 * managers and MultiBlockStructures and contain any information needed to
 * create a new factory object of the type which the egg is representing.
 *
 */
public interface IFactoryEgg {
	/**
	 * Called whenever a factory is supposed to be created after all the
	 * required checks are already done.
	 * 
	 * @param mbs
	 *            Physical representation of the factory which should be created
	 * @param p
	 *            Player creating the factory
	 * @return The created factory object
	 */
	public Factory hatch(MultiBlockStructure mbs, Player p);

	/**
	 * Each egg has a name which is also carried over to identify the type of
	 * the factory at a later point. There can be many instances of the same egg
	 * with different names, but there should never be multiple instances with
	 * the same name
	 * 
	 * @return name of this egg and the factory it is creating
	 */
	public String getName();

	/**
	 * Java wont let me specify a method here without specifying its parameters
	 * so it's commented out, because parameters may vary for each
	 * implementation, but this is needed to make the whole thing work. This
	 * method is called when reconstructing an factory object with data provided
	 * from the database, so this method should provide the possibility to
	 * create a factory in the same specific state which is represented by the
	 * data pulled from the database, for example in terms of selected recipe
	 * and repair value
	 */
	// public Factory revive();
}
