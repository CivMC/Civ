package com.untamedears.JukeAlert.model;

/**
 * Enum that represents a type of action that a snitch can record, and the value
 * that goes into the database for said action
 * 
 *
 */
public enum LoggedAction {
	
	// ONCE THIS GOES INTO PRODUCTION, _NEVER_ CHANGE THESE, only mark some as not used and add more with larger values!
	KILL(0),
	BLOCK_PLACE(1),
	BLOCK_BREAK(2),
	BUCKET_FILL(3),
	BUCKET_EMPTY(4),
	ENTRY(5),
	USED(6), 
	IGNITED(7);
	
	private int value;
	
	// constructor, has to be private
	private LoggedAction(int value) {
		this.value = value;
	}
	
	public int getLoggedActionId() {
		return this.value;
	}

}
