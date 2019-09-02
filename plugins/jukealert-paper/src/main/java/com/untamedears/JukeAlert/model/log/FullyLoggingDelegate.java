package com.untamedears.JukeAlert.model.log;

import java.util.LinkedList;
import java.util.List;

import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.actions.LoggedSnitchAction;

public class FullyLoggingDelegate extends BroadCastingOnlyDelegate {
	
	private List<LoggedSnitchAction> actions;
	private boolean hasLoadedAll;

	public FullyLoggingDelegate() {
		this.actions = new LinkedList<>();
		this.hasLoadedAll = false;
	}
	

	@Override
	public void addAction(LoggedSnitchAction action) {
		super.addAction(action);
		actions.add(action);
	}

	@Override
	public List<LoggedSnitchAction> getFullLogs() {
		return actions;
	}

	@Override
	public void deleteAllLogs() {
		actions.clear();
	}
	
	private void loadLogs() {
		
	}
	
	@Override
	public void setSnitch(Snitch snitch) {
		super.setSnitch(snitch);
		if (snitch.getId() != -1) {
			loadLogs();
		}
	}
	
	

}
