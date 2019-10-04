package com.untamedears.JukeAlert.model.actions;

import com.untamedears.JukeAlert.model.Snitch;

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

}
