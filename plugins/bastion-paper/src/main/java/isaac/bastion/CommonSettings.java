/**
 * Created by Aleksey on 16.07.2017.
 */

package isaac.bastion;

import org.bukkit.configuration.ConfigurationSection;

public class CommonSettings {
	private boolean cancelReinInBastionField;

	public boolean isCancelReinInBastionField() {
		return this.cancelReinInBastionField;
	}

	public static CommonSettings load(ConfigurationSection config) {
		CommonSettings settings = new CommonSettings();
		settings.cancelReinInBastionField = config.getBoolean("cancelReinInBastionField", false);

		return settings;
	}

}
