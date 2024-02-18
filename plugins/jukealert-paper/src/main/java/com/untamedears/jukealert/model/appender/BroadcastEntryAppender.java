package com.untamedears.jukealert.model.appender;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerAction;
import com.untamedears.jukealert.model.actions.abstr.SnitchAction;
import com.untamedears.jukealert.model.appender.config.LimitedActionTriggerConfig;
import com.untamedears.jukealert.util.JASettingsManager;
import com.untamedears.jukealert.util.JAUtility;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import java.util.UUID;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class BroadcastEntryAppender extends ConfigurableSnitchAppender<LimitedActionTriggerConfig> {

	public static final String ID = "broadcast";

	public BroadcastEntryAppender(Snitch snitch, ConfigurationSection config) {
		super(snitch, config);
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
		if (!config.isTrigger(action.getIdentifier())) {
			return;
		}
		for (UUID uuid : snitch.getGroup().getAllMembers()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) {
				continue;
			}
			JASettingsManager settings = JukeAlert.getInstance().getSettingsManager();
			if (settings.doesIgnoreAllAlerts(uuid)) {
				continue;
			}
			if (settings.doesIgnoreAlert(snitch.getGroup().getName(), uuid)) {
				continue;
			}
			if (snitch.hasPermission(uuid, JukeAlertPermissionHandler.getSnitchAlerts())) {
				TextComponent comp = log.getChatRepresentation(player.getLocation(), true, false);

				if (settings.shouldShowDirections(uuid)) {
					comp.addExtra(String.format("  %s", JAUtility.genDirections(snitch, player)));
				}
				if (settings.monocolorAlerts(uuid)) {
					String raw = comp.toPlainText();
					raw = ChatColor.stripColor(raw);
					player.sendMessage(ChatColor.AQUA + raw);
				}
				else {
					player.spigot().sendMessage(comp);
				}
			}
		}
	}

	@Override
	public boolean runWhenSnitchInactive() {
		return false;
	}

	@Override
	public Class<LimitedActionTriggerConfig> getConfigClass() {
		return LimitedActionTriggerConfig.class;
	}

}
