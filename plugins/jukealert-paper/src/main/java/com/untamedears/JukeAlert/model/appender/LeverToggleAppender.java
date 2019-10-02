package com.untamedears.JukeAlert.model.appender;

import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.actions.SnitchAction;

public class LeverToggleAppender extends AbstractSnitchAppender {
	
	private boolean shouldToggle;

	public LeverToggleAppender(Snitch snitch) {
		super(snitch);
	}

	@Override
	public boolean runWhenSnitchInactive() {
		return false;
	}

	@Override
	public void acceptAction(SnitchAction action) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean shouldToggle() {
		return shouldToggle;
	}
	
	public void switchState() {
		shouldToggle = !shouldToggle;
		snitch.setDirty();
	}

}
