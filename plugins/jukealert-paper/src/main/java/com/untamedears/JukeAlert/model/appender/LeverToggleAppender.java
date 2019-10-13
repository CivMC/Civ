package com.untamedears.JukeAlert.model.appender;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Powerable;
import org.bukkit.configuration.ConfigurationSection;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.actions.abstr.SnitchAction;
import com.untamedears.JukeAlert.model.appender.config.LeverToggleConfig;

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
	public Class<LeverToggleConfig> getConfigClass() {
		// TODO Auto-generated method stub
		return null;
	}

}
