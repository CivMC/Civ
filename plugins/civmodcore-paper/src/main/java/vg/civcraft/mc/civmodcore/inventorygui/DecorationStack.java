package vg.civcraft.mc.civmodcore.inventorygui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

public class DecorationStack extends Clickable {

	public DecorationStack(ItemStack item) {
		super(item);
	}
	
	public DecorationStack(Material mat) {
		this(new ItemStack(mat));
	}
	
	public DecorationStack(Material mat, String name) {
		this(mat);
		ItemUtils.setDisplayName(this.item, name);
	}
	
	public DecorationStack(Material mat, String name, String ...lore) {
		this(mat, name);
		ItemUtils.addLore(this.item, lore);
	}

	@Override
	public void clicked(Player p) {
		// dont do anything here, it's supposed to be just decoration
	}

}
