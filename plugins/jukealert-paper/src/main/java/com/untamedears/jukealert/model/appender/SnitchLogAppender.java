package com.untamedears.jukealert.model.appender;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.database.JukeAlertDAO;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggableAction;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerAction;
import com.untamedears.jukealert.model.actions.abstr.SnitchAction;
import com.untamedears.jukealert.model.appender.config.LimitedActionTriggerConfig;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nonnull;
import org.bukkit.configuration.ConfigurationSection;
import vg.civcraft.mc.civmodcore.util.CivLogger;

public class SnitchLogAppender extends ConfigurableSnitchAppender<LimitedActionTriggerConfig> {

	public static final String ID = "log";
	private static final CivLogger LOGGER = CivLogger.getLogger(SnitchLogAppender.class);
	private static final Comparator<LoggableAction> ACTION_COMPARATOR = Comparator.comparingLong(
			(action) -> ((SnitchAction) action).getTime());

	private final Object2IntMap<LoggableAction> pendingActions;

	public SnitchLogAppender(final Snitch snitch,
							 final ConfigurationSection config) {
		super(snitch, config);
		this.pendingActions = new Object2IntRBTreeMap<>(ACTION_COMPARATOR);
		this.pendingActions.defaultReturnValue(-1);
	}

	/**
	 * @return Returns a list of actions this snitch has recorded, with certain caveats.
	 *
	 * @see JukeAlertDAO#loadLogs(Snitch, long, int)
	 */
	@Nonnull
	public List<LoggableAction> getFullLogs() {
		if (getSnitch().getId() == -1) {
			final var actions = new ArrayList<>(this.pendingActions.keySet());
			actions.sort(ACTION_COMPARATOR);
			Collections.reverse(actions);
			return actions;
		}
		return JukeAlert.getInstance().getDAO().loadLogs(getSnitch(),
				getMaximumActionAge(), this.config.getHardCap());
	}

	/**
	 * Deletes <b>ALL</b> logs for this snitch.
	 */
	public void deleteLogs() {
		if (getSnitch().getId() != -1) {
			JukeAlert.getInstance().getDAO().deleteAllLogsForSnitch(getSnitch());
		}
		this.pendingActions.clear();
	}

	/**
	 * @return Returns the maximum age (as a UNIX timestamp) for actions.
	 */
	private long getMaximumActionAge() {
		return System.currentTimeMillis() - this.config.getActionLifespan();
	}

	/** {@inheritDoc} */
	@Override
	public void acceptAction(final SnitchAction action) {
		if (action.isLifeCycleEvent() || !action.hasPlayer()) {
			return;
		}
		if (!this.config.isTrigger(action.getIdentifier())) {
			return;
		}
		final LoggablePlayerAction log = (LoggablePlayerAction) action;
		if (this.snitch.hasPermission(log.getPlayer(), JukeAlertPermissionHandler.getSnitchImmune())) {
			return;
		}
		final int internalActionID = JukeAlert.getInstance().getLoggedActionFactory().getInternalID(action.getIdentifier());
		if (internalActionID == -1) {
			LOGGER.warning("Snitch action [" + action + "] returned an invalid internal-action id "
					+ "for snitch [" + this.snitch + "]");
			return;
		}
		if (this.snitch.getId() == -1) {
			this.pendingActions.put(log, internalActionID);
			return;
		}
		JukeAlert.getInstance().getDAO().insertLogAsync(internalActionID, getSnitch(), log);
	}

	/** {@inheritDoc} */
	@Override
	public void persist() {
		final JukeAlertDAO dao = JukeAlert.getInstance().getDAO();
		this.pendingActions.forEach((action, actionID) ->
				dao.insertLog(actionID, getSnitch(), action.getPersistence()));
		this.pendingActions.clear();
		dao.deleteOldLogsForSnitch(getSnitch(), getMaximumActionAge());
	}

	/** {@inheritDoc} */
	@Override
	public boolean runWhenSnitchInactive() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Class<LimitedActionTriggerConfig> getConfigClass() {
		return LimitedActionTriggerConfig.class;
	}

}
