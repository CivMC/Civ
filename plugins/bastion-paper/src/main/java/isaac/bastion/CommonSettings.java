/**
 * Created by Aleksey on 16.07.2017.
 */

package isaac.bastion;

import org.bukkit.configuration.ConfigurationSection;

public class CommonSettings {
	private boolean cancelReinforcementModeInBastionField;
	private int listBastionTimeout;

	public boolean isCancelReinforcementModeInBastionField() {
		return this.cancelReinforcementModeInBastionField;
	}

	public int getListBastionTimeout() {
		return this.listBastionTimeout;
	}

	public static CommonSettings load(ConfigurationSection config) {
		CommonSettings settings = new CommonSettings();
		settings.cancelReinforcementModeInBastionField = config.getBoolean("cancelReinforcementModeInBastionField", false);
		settings.listBastionTimeout = config.getInt("listBastionTimeout", 2000);

		return settings;
	}

}
