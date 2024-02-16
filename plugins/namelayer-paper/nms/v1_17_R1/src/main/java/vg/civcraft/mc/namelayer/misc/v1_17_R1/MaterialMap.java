package vg.civcraft.mc.namelayer.misc.v1_17_R1;

import vg.civcraft.mc.namelayer.misc.MaterialInterface;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MaterialMap implements MaterialInterface {

	@Override
	public Material getMaterial(final MaterialInterface.Specific specific) {
		return switch (specific) {
			case GREEN -> Material.LIME_DYE;
			case RED -> Material.RED_DYE;
			case BACK -> Material.OAK_DOOR;
			case MOD -> Material.GOLDEN_CHESTPLATE;
			case BLACKLIST -> Material.LEAD;
			case PERMS -> Material.OAK_FENCE_GATE;
			case MERGE -> Material.CHEST_MINECART;
			case DEFAULT -> Material.BRICKS;
		};
	}

	@Override
	public ItemStack getItemStack(final MaterialInterface.Specific specific) {
		return switch (specific) {
			case GREEN, RED, BACK, MOD, BLACKLIST, PERMS, MERGE, DEFAULT -> new ItemStack(getMaterial(specific));
		};
	}

}
