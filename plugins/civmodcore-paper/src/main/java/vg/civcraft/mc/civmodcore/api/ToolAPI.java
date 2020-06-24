package vg.civcraft.mc.civmodcore.api;

import org.bukkit.Material;

public final class ToolAPI {

	/**
	 * @deprecated Use {@link MaterialAPI#isSword(Material)} instead.
	 */
	@Deprecated
	public static boolean isSword(Material material) {
		return MaterialAPI.isSword(material);
	}

	/**
	 * @deprecated Use {@link MaterialAPI#isShovel(Material)} instead.
	 */
	@Deprecated
	public static boolean isShovel(Material material) {
		return MaterialAPI.isShovel(material);
	}

	/**
	 * @deprecated Use {@link MaterialAPI#isPickaxe(Material)} instead.
	 */
	@Deprecated
	public static boolean isPickaxe(Material material) {
		return MaterialAPI.isPickaxe(material);
	}

	/**
	 * @deprecated Use {@link MaterialAPI#isAxe(Material)} instead.
	 */
	@Deprecated
	public static boolean isAxe(Material material) {
		return MaterialAPI.isAxe(material);
	}

	/**
	 * @deprecated Use {@link MaterialAPI#isHoe(Material)} instead.
	 */
	@Deprecated
	public static boolean isHoe(Material material) {
		return MaterialAPI.isHoe(material);
	}

}
