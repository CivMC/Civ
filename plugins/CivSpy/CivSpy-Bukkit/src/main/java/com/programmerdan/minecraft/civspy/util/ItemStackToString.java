package com.programmerdan.minecraft.civspy.util

public class ItemStackToString {

	private ItemStackToString(){}

	/**
	 * For 1.10.2 ItemStacks.
	 */
	public static String toString(ItemStack itemStack) {
		StringBuilder toString = new StringBuilder();

		Material material = itemStack.getType();
		MaterialData data = itemStack.getData();
		ItemMeta meta = itemStack.getItemMeta();
		int amount = itemStack.getAmount();
		short durability = itemStack.getDurability();
		Map<Enchantment, Integer> enchants = itemStack.getEnchantments();
		
		if (data instanceof Directional) {
			toString.append(((Directional) data).getFacing().name()).append("_");
		}

		if (data instanceof Redstone) {
			toString.append(((Redstone) data).isPowered() ? "Powered_" : "Unpowered_");
		}

		if (data instanceof Colorable) {
			toString.append(((Colorable) data).getColor().name()).append("_");
		}

		if (data instanceof Openable) {
			toString.append(((Openable) data).isOpen() ? "Open_" : "Closed_");
		}


		if (data instanceof Banner) {
			Banner banner = (Banner) data;
			if (banner.isWallBanner()) {
				toString.append("Wall");
			}
			toString.append("Banner");
		} else if (data instanceof Bed) {
			toString.append("Bed");
		} else if (data instanceof Cake) {
			Cake cake = (Cake) data;
			toString.append(cake.getSlicesEaten()).append("/").append(cake.getSlicesRemaining()).append("_Cake");
		} else if (data instanceof Cauldron) {
			Cauldron cauldron = (Cauldron) data;
			if (cauldron.isEmpty()) {
				toString.append("Empty_");
			} else if (cauldron.isFull()) {
				toString.append("Full_");
			}
			toString.append("Cauldron");
		} else if (data instanceof Coal) {
			toString.append(((Coal) data).getType().name());
		} else if (data instanceof CocoaPlant) {
			toString.append((CocoaPlant) data).getSize().name());
			toString.append("_CocoaPlant");
		} else if (data instanceof Command) {
			toString.append("CommandBlock");
		} else if (data instanceof Comparator) {
			if (((Comparator) data).isSubtractionMode()) {
				toString.append("Subtracting_");
			}
			toString.append("Comparator");
		} else if (data instanceof Crops) {
			toString.append(((Crops)data).getState().name());
			toString.append("_").append(material.toString());
		} else if (data instanceof Diode) {
			toString.append(((Diode)data).getDelay()).append("t_Delay");
			toString.append("_Diode");
		} else if (data instanceof Door) {
			Door door = (Door) data;
			if (door.getHinge()) {
				toString.append("Right_");
			} else {
				toString.append("Left_");
			}
			toString.append("_").append(material.toString());
		} else if (data instanceof Dye) {
			toString.append("Dye_").append(material.toString());
		} else if (data instanceof Gate) {
			toString.append(material.toString());
		} else if (data instanceof Hopper) {
			toString.append(((Hopper) data).isActive() ? "Active_" : "Inactive_").append("Hopper");
		} else if (data instanceof LongGrass) {
			toString.append(((LongGrass) data).getSpecies().name()).append("_LongGrass");
		} else if (data instanceof Mushroom) {
			toString.append(((Mushroom) data).getBlockTexture().name()).append("_GiantMushroom");
		} else if (data instanceof NetherWarts) {
			toString.append(((NetherWarts) data).getState().name()).append("_NetherWarts");
		} else if (data instanceof PistonBaseMaterial) {
			toString.append(((PistonBaseMaterial) data).isStick() ? "StickyPiston" : "Piston");
		}
	}
}
