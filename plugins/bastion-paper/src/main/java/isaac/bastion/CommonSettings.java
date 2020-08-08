/**
 * Created by Aleksey on 16.07.2017.
 */

package isaac.bastion;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommonSettings {
	private boolean cancelReinforcementModeInBastionField;
	private int listBastionTimeout;
	private Set<Material> cancelPlacementAndDamage;

	public boolean isCancelReinforcementModeInBastionField() {
		return this.cancelReinforcementModeInBastionField;
	}

	public int getListBastionTimeout() {
		return this.listBastionTimeout;
	}

	public Set<Material> getCancelPlacementAndDamageMaterials() {
		return this.cancelPlacementAndDamage;
	}

	public static CommonSettings load(ConfigurationSection config) {
		CommonSettings settings = new CommonSettings();
		settings.cancelReinforcementModeInBastionField = config.getBoolean("cancelReinforcementModeInBastionField", false);
		settings.listBastionTimeout = config.getInt("listBastionTimeout", 2000);
		List<String> materialNames = config.getStringList("cancelPlacementAndDamage");
		settings.cancelPlacementAndDamage = new HashSet<>();
		for (String name : materialNames) {
			Material m = Material.matchMaterial(name);
			if (m != null) {
				settings.cancelPlacementAndDamage.add(m);
			}
		}
		return settings;
	}
}
