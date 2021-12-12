package vg.civcraft.mc.civmodcore.inventory.gui.components.impl;

import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import vg.civcraft.mc.civmodcore.inventory.gui.LClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.components.ComponableSection;
import vg.civcraft.mc.civmodcore.inventory.gui.components.SlotPredicates;
import vg.civcraft.mc.civmodcore.inventory.gui.components.StaticDisplaySection;

@UtilityClass
public class CommonGUIs {

	public static ComponableSection genConfirmationGUI(int rows, int columns, Runnable yesFunc, String yesText, Runnable noFunc,
			String noText) {
		ComponableSection section = new ComponableSection(rows * columns);
		StaticDisplaySection content = new StaticDisplaySection(5);
		content.set(new LClickable(Material.GREEN_DYE, yesText, p -> yesFunc.run()), 0);
		content.set(new LClickable(Material.RED_DYE, noText, p -> noFunc.run()), 4);
		section.addComponent(content, SlotPredicates.offsetRectangle(1, 5, rows / 2, 2));
		return section;
	}

}
