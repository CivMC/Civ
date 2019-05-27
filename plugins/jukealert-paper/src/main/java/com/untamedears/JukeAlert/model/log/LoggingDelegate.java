package com.untamedears.JukeAlert.model.log;

import java.util.List;

import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.actions.LoggedSnitchAction;

public abstract class LoggingDelegate {
	
	protected Snitch snitch;
	
	public void setSnitch(Snitch snitch) {
		if (this.snitch != null) {
			throw new IllegalStateException();
		}
		this.snitch = snitch;
	}
	
	public abstract void addAction(LoggedSnitchAction action);
	
	public abstract List<LoggedSnitchAction> getFullLogs();
	
	public abstract void deleteAllLogs();

}
