package com.untamedears.jukealert.model.appender;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.PlayerAction;
import com.untamedears.jukealert.model.actions.abstr.SnitchAction;
import com.untamedears.jukealert.model.appender.config.LeverToggleConfig;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Powerable;
import org.bukkit.configuration.ConfigurationSection;

public class LeverToggleAppender extends ConfigurableSnitchAppender<LeverToggleConfig> {
	
	public static final String ID = "levertoggle";
	
	private boolean shouldToggle;

	public LeverToggleAppender(Snitch snitch, ConfigurationSection config) {
		super(snitch, config);
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
		if (!shouldToggle) {
			return;
		}
		if (!action.hasPlayer()) {
			return;
		}
		PlayerAction playerAc = (PlayerAction) action;
		if (snitch.hasPermission(playerAc.getPlayer(), JukeAlertPermissionHandler.getSnitchImmune())) {
			return;
		}
		for(LeverToggleConfig.SideEntry entry : config.getEntries(action.getIdentifier())) {
			Block leverBlock = snitch.getLocation().getBlock().getRelative(entry.getFace());
			if (leverBlock.getType() != Material.LEVER) {
				continue;
			}
			Directional dir = (Directional) leverBlock.getBlockData();
			if (dir.getFacing() != entry.getFace()) {
				continue;
			}
			Powerable power = (Powerable) leverBlock.getBlockData();
			power.setPowered(true);
			leverBlock.setBlockData(power);
			Bukkit.getScheduler().scheduleSyncDelayedTask(JukeAlert.getInstance(), () -> {
				power.setPowered(false);
				leverBlock.setBlockData(power);
			}, 20L);
		}
	}
	
	public boolean shouldToggle() {
		return shouldToggle;
	}
	
	public void switchState() {
		shouldToggle = !shouldToggle;
		snitch.setDirty();
	}

	@Override
	public void persist() {
		if (snitch.getId() != 1) {
			JukeAlert.getInstance().getDAO().setToggleLever(snitch.getId(), shouldToggle);
		}
	}

	@Override
	public Class<LeverToggleConfig> getConfigClass() {
		return LeverToggleConfig.class;
	}

}
