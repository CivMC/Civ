package vg.civcraft.mc.civmodcore.inventorygui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DecorationStack extends Clickable {

	public DecorationStack(ItemStack item) {
		super(item);
	}

	public void clicked(Player p) {
		//dont do anything here, it's supposed to be just decoration
	}
}
