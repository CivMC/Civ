package com.untamedears.jukealert.model.appender;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.SnitchAction;

/**
 * Adds some kind of functionality to a snitch
 *
 */
public abstract class AbstractSnitchAppender {

	protected final Snitch snitch;

	public AbstractSnitchAppender(Snitch snitch) {
		this.snitch = snitch;
	}

	/**
	 * @return Snitch instance to which this instance is adding functionality
	 */
	public Snitch getSnitch() {
		return snitch;
	}

	/**
	 * Called when the snitch is unloaded from memory to allow the appender to save
	 * its own data, may be overwritten by sub classes if needed
	 */
	public void persist() {

	}

	/**
	 * Should this appender still run when its snitch is inactive?
	 * 
	 * @return Whether appender runs when snitch is inactive
	 */
	public abstract boolean runWhenSnitchInactive();

	/**
	 * Called for anything happening within the snitchs coverage area
	 * 
	 * @param action Action which happened
	 */
	public abstract void acceptAction(SnitchAction action);
	
	/**
	 * Called after the instance is created and was inserted into its surrounding cache structure
	 */
	public void postSetup() {}

}
