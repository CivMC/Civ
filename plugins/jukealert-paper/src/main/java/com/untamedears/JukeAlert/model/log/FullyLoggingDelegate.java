package com.untamedears.JukeAlert.model.log;

import java.util.LinkedList;
import java.util.List;

import com.untamedears.JukeAlert.JukeAlert;
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
		if (!hasLoadedAll) {
			synchronized (actions) {
				while (!hasLoadedAll) {
					try {
						actions.wait();
					} catch (InterruptedException e) {
						// welp
						break;
					}
				}
			}
		}
		return actions;
	}

	@Override
	public void deleteAllLogs() {
		actions.clear();
	}

	private void loadLogs() {
		new Thread(() -> {
			synchronized (actions) {
				actions.addAll(JukeAlert.getInstance().getDAO().loadLogs(snitch));
				hasLoadedAll = true;
				actions.notifyAll();
			}
		}).start();
	}

	@Override
	public void setSnitch(Snitch snitch) {
		super.setSnitch(snitch);
		if (snitch.getId() != -1) {
			loadLogs();
		} else {
			hasLoadedAll = true;
		}
	}

	@Override
	public void persist() {
		for (LoggedSnitchAction action : actions) {
			switch (action.getCacheState()) {
			case NEW:
				
				continue;
			case DELETED:
				
				continue;
			case NORMAL:
				continue;
			}
		}
	}

}
