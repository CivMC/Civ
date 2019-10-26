package vg.civcraft.mc.civmodcore.api;

import org.bukkit.Material;

public final class ToolAPI {

	private ToolAPI() {
	}

	public static boolean isAxe(Material mat) {
		switch (mat) {
		case DIAMOND_AXE:
		case GOLDEN_AXE:
		case WOODEN_AXE:
		case STONE_AXE:
		case IRON_AXE:
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isPickaxe(Material mat) {
		switch (mat) {
		case DIAMOND_PICKAXE:
		case GOLDEN_PICKAXE:
		case WOODEN_PICKAXE:
		case STONE_PICKAXE:
		case IRON_PICKAXE:
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isShovel(Material mat) {
		switch (mat) {
		case DIAMOND_SHOVEL:
		case GOLDEN_SHOVEL:
		case WOODEN_SHOVEL:
		case STONE_SHOVEL:
		case IRON_SHOVEL:
			return true;
		default:
			return false;
		}
	}

}
