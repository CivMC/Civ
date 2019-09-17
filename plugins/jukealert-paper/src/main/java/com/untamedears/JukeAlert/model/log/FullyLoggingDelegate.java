package com.untamedears.JukeAlert.model.log;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.database.JukeAlertDAO;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.actions.CacheState;
import com.untamedears.JukeAlert.model.actions.LoggedActionFactory;
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
		snitch.setDirty();
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
		JukeAlertDAO dao = JukeAlert.getInstance().getDAO();
		LoggedActionFactory fac = JukeAlert.getInstance().getLoggedActionFactory();
		Iterator<LoggedSnitchAction> iter = actions.iterator();
		while (iter.hasNext()) {
			LoggedSnitchAction action = iter.next();
			switch (action.getCacheState()) {
			case NEW:
				int id = fac.getInternalID(action.getIdentifier());
				if (id != -1) {
					dao.insertLog(id, snitch, action.getPersistence());
					action.setCacheState(CacheState.NORMAL);
				}
				continue;
			case DELETED:
				dao.deleteLog(action);
				iter.remove();
				continue;
			case NORMAL:
				continue;
			}
		}
	}

}
