package com.untamedears.JukeAlert.model.appender;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.actions.SnitchAction;

public class LeverToggleAppender extends AbstractSnitchAppender {
	
	private boolean shouldToggle;

	public LeverToggleAppender(Snitch snitch) {
		super(snitch);
		if (snitch.getId() != -1) {
			this.shouldToggle = JukeAlert.getInstance().getDAO().getToggleLever(snitch.getId());
		}
	}

	@Override
	public boolean runWhenSnitchInactive() {
		return false;
	}

	@Override
	public void acceptAction(SnitchAction action) {
		//TODO Trigger levers
	}
	
	public boolean shouldToggle() {
		return shouldToggle;
	}
	
	public void switchState() {
		shouldToggle = !shouldToggle;
		snitch.setDirty();
	}

}
