package com.untamedears.jukealert.model.appender;

import org.bukkit.configuration.ConfigurationSection;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerAction;
import com.untamedears.jukealert.model.actions.abstr.SnitchAction;
import com.untamedears.jukealert.model.actions.internal.DestroySnitchAction;
import com.untamedears.jukealert.model.actions.internal.DestroySnitchAction.Cause;
import com.untamedears.jukealert.model.appender.config.DormantCullingConfig;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;

import vg.civcraft.mc.civmodcore.util.BukkitComparators;
import vg.civcraft.mc.civmodcore.util.progress.ProgressTrackable;

public class DormantCullingAppender extends ConfigurableSnitchAppender<DormantCullingConfig>
		implements ProgressTrackable {
	
	public static final String ID = "dormantcull";

	private long lastRefresh;
	private long nextUpdate;

	public DormantCullingAppender(Snitch snitch, ConfigurationSection config) {
		super(snitch, config);
		if (snitch.getId() == -1) {
			// snitch was just created
			lastRefresh = System.currentTimeMillis();
		} else {
			lastRefresh = JukeAlert.getInstance().getDAO().getRefreshTimer(snitch.getId());
		}
	}
	
	@Override
	public void postSetup() {
		if (lastRefresh == -1) {
			// no data in db due to recent config change, let's use the current time and
			// mark it for saving later
			refreshTimer();
		}
		nextUpdate = calcFutureUpdate();
		JukeAlert.getInstance().getSnitchCullManager().addCulling(this);
		updateState();
	}

	@Override
	public boolean runWhenSnitchInactive() {
		return true;
	}

	@Override
	public void acceptAction(SnitchAction action) {
		if (action.isLifeCycleEvent()) {
			if (action instanceof DestroySnitchAction) {
				JukeAlert.getInstance().getSnitchCullManager().removeCulling(this);
			}
			return;
		}
		if (!action.hasPlayer()) {
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
	
	/**
	 * @return Is the snitch currently dormant, meaning no longer active, but not entirely culled yet
	 */
	public boolean isDormant() {
		long elapsed = getTimeSinceLastRefresh();
		if (elapsed >= config.getTotalLifeTime()) {
			return false;
		}
		return elapsed >= config.getLifetime();
	}
	
	public long getTimeUntilCulling() {
		return config.getTotalLifeTime() - getTimeSinceLastRefresh();
	}
	
	public long getTimeUntilDormant() {
		return config.getLifetime() - getTimeSinceLastRefresh();
	}
	
	/**
	 * @return Is the snitch currently active, meaning neither culled nor dormant
	 */
	public boolean isActive() {
		long elapsed = getTimeSinceLastRefresh();
		if (elapsed >= config.getTotalLifeTime()) {
			return false;
		}
		return elapsed < config.getLifetime();
	}

	public void refreshTimer() {
		this.lastRefresh = System.currentTimeMillis();
		snitch.setDirty();
		updateState();
		JukeAlert.getInstance().getSnitchCullManager().updateCulling(this, calcFutureUpdate());
	}

	public long getTimeSinceLastRefresh() {
		return System.currentTimeMillis() - lastRefresh;
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

	@Override
	public int compareTo(ProgressTrackable o) {
		return BukkitComparators.getLocation().compare(((AbstractSnitchAppender) o).getSnitch().getLocation(),
				snitch.getLocation());
	}

	@Override
	public void updateInternalProgressTime(long update) {
		this.nextUpdate = update;
	}

	private long calcFutureUpdate() {
		long elapsed = getTimeSinceLastRefresh();
		if (elapsed >= config.getTotalLifeTime()) {
			return Long.MAX_VALUE;

		}
		if (elapsed >= config.getLifetime()) {
			return lastRefresh + config.getTotalLifeTime();
		} else {
			return lastRefresh + config.getLifetime();
		}
	}

	@Override
	public void updateState() {
		long elapsed = getTimeSinceLastRefresh();
		if (elapsed >= config.getTotalLifeTime()) {
			JukeAlert.getInstance().getSnitchManager().removeSnitch(snitch);
			snitch.processAction(new DestroySnitchAction(System.currentTimeMillis(), snitch, null, Cause.CULL));
			return;
		}
		if (elapsed >= config.getLifetime()) {
			snitch.setActiveStatus(false);
		}
	}

	@Override
	public long getNextUpdate() {
		return nextUpdate;
	}

}
