package com.untamedears.JukeAlert.model.actions.impl;

import java.util.UUID;

import com.untamedears.JukeAlert.model.actions.PlayerAction;

public class DestroySnitchAction extends PlayerAction {
	
	public static final String ID = "DESTROY_SNITCH";

	public DestroySnitchAction(long time, UUID player) {
		super(time, player);
	}

	@Override
	public String getIdentifier() {
		return ID;
	}
	
	@Override
	public boolean isLifeCycleEvent() {
		return true;
	}

}
