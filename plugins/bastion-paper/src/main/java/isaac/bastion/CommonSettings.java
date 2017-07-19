/**
 * Created by Aleksey on 16.07.2017.
 */

package isaac.bastion;

import org.bukkit.configuration.ConfigurationSection;

public class CommonSettings {
	private boolean cancelReinforcementModeInBastionField;

	public boolean isCancelReinforcementModeInBastionField() {
		return this.cancelReinforcementModeInBastionField;
	}

	public static CommonSettings load(ConfigurationSection config) {
		CommonSettings settings = new CommonSettings();
		settings.cancelReinforcementModeInBastionField = config.getBoolean("cancelReinforcementModeInBastionField", false);

		return settings;
	}

}
