package vg.civcraft.mc.namelayer.misc.v1_8_R1;

import vg.civcraft.mc.namelayer.misc.MaterialInterface;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MaterialMap implements MaterialInterface {
	@Override
	public Material getMaterial(MaterialInterface.Specific specific) {
		switch (specific) {
			case GREEN:
				return Material.INK_SACK;
			case RED:
				return Material.INK_SACK;
			case BACK:
				return Material.WOOD_DOOR;
			case MOD:
				return Material.GOLD_CHESTPLATE;
			case BLACKLIST:
				return Material.LEASH;
			case PERMS:
				return Material.FENCE_GATE;
			case MERGE:
				return Material.STORAGE_MINECART;
			case DEFAULT:
				return Material.BRICK;
		}
		return null;
	}

	@Override
	public ItemStack getItemStack(MaterialInterface.Specific specific) {
		switch (specific) {
			case GREEN:
				return new ItemStack(Material.INK_SACK, 1, (short) 10);
			case RED:
				return new ItemStack(Material.INK_SACK, 1, (short) 1);
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
