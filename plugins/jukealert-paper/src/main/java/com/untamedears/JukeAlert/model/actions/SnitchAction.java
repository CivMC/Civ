package com.untamedears.JukeAlert.model.actions;

public abstract class SnitchAction {
	
	protected final long time;
	
	public SnitchAction(long time) {
		this.time = time;
	}
	
	/**
	 * @return UNIX timestamp of when the action happened
	 */
	public long getTime() {
		return time;
	}
	
	/**
	 * @return Unique identifier for this type of action, one per class
	 */
	public abstract String getIdentifier();
	
	public abstract boolean hasPlayer();
	
	public abstract boolean isLifeCycleEvent();

}
