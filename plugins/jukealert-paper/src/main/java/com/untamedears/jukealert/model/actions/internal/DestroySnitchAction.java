package com.untamedears.jukealert.model.actions.internal;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.PlayerAction;
import java.util.UUID;

public class DestroySnitchAction extends PlayerAction {
	
	public enum Cause {
		PLAYER, CULL, CLEANUP;
	}
	
	public static final String ID = "DESTROY_SNITCH";

	private Cause cause;
	
	public DestroySnitchAction(long time, Snitch snitch, UUID player, Cause cause) {
		super(time, snitch, player);
		this.cause = cause;
	}
	
	public Cause getCause() {
		return cause;
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
