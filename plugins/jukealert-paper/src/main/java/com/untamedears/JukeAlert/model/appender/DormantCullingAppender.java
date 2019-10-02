package com.untamedears.JukeAlert.model.appender;

import org.bukkit.configuration.ConfigurationSection;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.actions.LoggablePlayerAction;
import com.untamedears.JukeAlert.model.actions.SnitchAction;
import com.untamedears.JukeAlert.model.appender.config.DormantCullingConfig;
import com.untamedears.JukeAlert.util.JukeAlertPermissionHandler;

public class DormantCullingAppender extends ConfigurableSnitchAppender<DormantCullingConfig> {

	private long lastRefresh;

	public DormantCullingAppender(Snitch snitch, ConfigurationSection config) {
		super(snitch, config);
		if (snitch.getId() == -1) {
			// snitch was just created
			lastRefresh = System.currentTimeMillis();
		} else {
			lastRefresh = JukeAlert.getInstance().getDAO().getRefreshTimer(snitch.getId());
			if (lastRefresh == -1) {
				// no data in db due to recent config change, let's use the current time and
				// mark it for saving later
				refreshTimer();
			}
		}
	}

	@Override
	public boolean runWhenSnitchInactive() {
		return true;
	}

	@Override
	public void acceptAction(SnitchAction action) {
		if (action.isLifeCycleEvent() || !action.hasPlayer()) {
			return;
		}
		LoggablePlayerAction playerAction = (LoggablePlayerAction) action;
		if (snitch.hasPermission(playerAction.getPlayer(), JukeAlertPermissionHandler.getListSnitches())) {
			refreshTimer();
		}
	}
	
	public long getLastRefresh() {
		return lastRefresh;
	}
	
	public void refreshTimer() {
		this.lastRefresh = System.currentTimeMillis();
		snitch.setDirty();
	}

	@Override
	public Class<DormantCullingConfig> getConfigClass() {
		return DormantCullingConfig.class;
	}
	
	@Override
	public void persist() {
		if (snitch.getId() != -1) {
			JukeAlert.getInstance().getDAO().setRefreshTimer(snitch.getId(), lastRefresh);
		}
	}

}
