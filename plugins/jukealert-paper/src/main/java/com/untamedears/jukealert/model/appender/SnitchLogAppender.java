package com.untamedears.jukealert.model.appender;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.database.JukeAlertDAO;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.ActionCacheState;
import com.untamedears.jukealert.model.actions.LoggedActionFactory;
import com.untamedears.jukealert.model.actions.abstr.LoggableAction;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerAction;
import com.untamedears.jukealert.model.actions.abstr.SnitchAction;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;

public class SnitchLogAppender extends AbstractSnitchAppender {

	public static final String ID = "log";

	private List<LoggableAction> actions;
	private boolean hasLoadedAll;

	public SnitchLogAppender(Snitch snitch) {
		super(snitch);
		this.actions = new LinkedList<>();
		this.hasLoadedAll = false;
		loadLogs();
	}

	@Override
	public void acceptAction(SnitchAction action) {
		if (action.isLifeCycleEvent() || !action.hasPlayer()) {
			return;
		}
		LoggablePlayerAction log = (LoggablePlayerAction) action;
		if (snitch.hasPermission(log.getPlayer(), JukeAlertPermissionHandler.getSnitchImmune())) {
			return;
		}
		actions.add(log);
		getSnitch().setDirty();
	}

	@Override
	public void persist() {
		JukeAlertDAO dao = JukeAlert.getInstance().getDAO();
		LoggedActionFactory fac = JukeAlert.getInstance().getLoggedActionFactory();
		Iterator<LoggableAction> iter = actions.iterator();
		while (iter.hasNext()) {
			LoggableAction action = iter.next();
			switch (action.getCacheState()) {
			case NEW:
				int id = fac.getInternalID(((SnitchAction)action).getIdentifier());
				if (id != -1) {
					dao.insertLog(id, getSnitch(), action.getPersistence());
					action.setCacheState(ActionCacheState.NORMAL);
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

	private void loadLogs() {
		synchronized (actions) {
			try {
				actions.addAll(JukeAlert.getInstance().getDAO().loadLogs(getSnitch()));
				hasLoadedAll = true;
			} finally {
				actions.notifyAll();
			}
		}
	}
	
	public void deleteLogs() {
		//TODO
	}

	public List<LoggableAction> getFullLogs() {
		if (!hasLoadedAll) {
			synchronized (actions) {
				while (!hasLoadedAll) {
					try {
						actions.wait();
					} catch (InterruptedException e) {
						// welp
					}
				}
			}
		}
		return actions;
	}

	@Override
	public boolean runWhenSnitchInactive() {
		return false;
	}

}
