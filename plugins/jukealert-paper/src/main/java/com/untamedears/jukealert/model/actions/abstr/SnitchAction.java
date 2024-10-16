package com.untamedears.jukealert.model.actions.abstr;

import com.untamedears.jukealert.model.Snitch;

public abstract class SnitchAction {

	protected final long time;
	protected final Snitch snitch;

	public SnitchAction(long time, Snitch snitch) {
		this.time = time;
		this.snitch = snitch;
	}

	/**
	 * @return UNIX timestamp of when the action happened
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Every action is owned by exactly one snitch, which can be retrieved with this
	 * method
	 * 
	 * @return Snitch owning this instance
	 */
	public Snitch getSnitch() {
		return snitch;
	}

	/**
	 * @return Unique identifier for this type of action, one per class
	 */
	public abstract String getIdentifier();

	public abstract boolean hasPlayer();

	public abstract boolean isLifeCycleEvent();
	
	/**
	 * Called when this action is executed and happening right now with the snitch it is happening for
	 * @param s Snitch the action is being applied to
	 */
	public void accept(Snitch s) {}

}
