package vg.civcraft.mc.civmodcore.api;

import org.bukkit.Material;

public final class ToolAPI {

	private ToolAPI() { }

	public static boolean isSword(Material material) {
		switch (material) {
			case WOODEN_SWORD:
			case STONE_SWORD:
			case IRON_SWORD:
			case GOLDEN_SWORD:
			case DIAMOND_SWORD:
				return true;
			default:
				return false;
		}
	}

	public static boolean isShovel(Material material) {
		switch (material) {
			case WOODEN_SHOVEL:
			case STONE_SHOVEL:
			case IRON_SHOVEL:
			case GOLDEN_SHOVEL:
			case DIAMOND_SHOVEL:
				return true;
			default:
				return false;
		}
	}

	public static boolean isPickaxe(Material material) {
		switch (material) {
			case WOODEN_PICKAXE:
			case STONE_PICKAXE:
			case IRON_PICKAXE:
			case GOLDEN_PICKAXE:
			case DIAMOND_PICKAXE:
				return true;
			default:
				return false;
		}
	}

	public static boolean isAxe(Material material) {
		switch (material) {
			case WOODEN_AXE:
			case STONE_AXE:
			case IRON_AXE:
			case GOLDEN_AXE:
			case DIAMOND_AXE:
				return true;
			default:
				return false;
		}
	}

	public static boolean isHoe(Material material) {
		switch (material) {
			case WOODEN_HOE:
			case STONE_HOE:
			case IRON_HOE:
			case GOLDEN_HOE:
			case DIAMOND_HOE:
				return true;
			default:
				return false;
		}
	}

}
