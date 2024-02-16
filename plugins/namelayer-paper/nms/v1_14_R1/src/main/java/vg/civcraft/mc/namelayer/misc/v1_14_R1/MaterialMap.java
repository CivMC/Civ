package vg.civcraft.mc.namelayer.misc.v1_14_R1;

import vg.civcraft.mc.namelayer.misc.MaterialInterface;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MaterialMap implements MaterialInterface {
	@Override
	public Material getMaterial(MaterialInterface.Specific specific) {
		switch (specific) {
			case GREEN:
				return Material.LIME_DYE;
			case RED:
				return Material.RED_DYE;
			case BACK:
				return Material.OAK_DOOR;
			case MOD:
				return Material.GOLDEN_CHESTPLATE;
			case BLACKLIST:
				return Material.LEAD;
			case PERMS:
				return Material.OAK_FENCE_GATE;
			case MERGE:
				return Material.CHEST_MINECART;
			case DEFAULT:
				return Material.BRICKS;
		}
		return null;
	}

	@Override
	public ItemStack getItemStack(MaterialInterface.Specific specific) {
		switch (specific) {
			case GREEN:
			case RED:
			case BACK:
			case MOD:
			case BLACKLIST:
			case PERMS:
			case MERGE:
			case DEFAULT:
				return new ItemStack(getMaterial(specific));
		}
		return null;
	}
}
