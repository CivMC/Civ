package vg.civcraft.mc.civmodcore.api;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.BeaconInventory;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.CartographyInventory;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.LecternInventory;
import org.bukkit.inventory.LoomInventory;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.StonecutterInventory;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class InventoryAPI {

	private InventoryAPI() { }

	/**
	 * Get the players viewing an inventory.
	 *
	 * @param inventory The inventory to get the viewers of.
	 * @return Returns a list of players. Returns an empty list if the inventory is null.
	 * */
	public static List<Player> getViewingPlayers(Inventory inventory) {
		if (inventory == null) {
			return new ArrayList<>();
		}
		return inventory
				.getViewers()
				.stream()
				.map((entity) -> EntityAPI.isPlayer(entity) ? (Player) entity : null)
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private static boolean checkInventory(Inventory inventory, InventoryType... types) {
		if (inventory == null) {
			return false;
		}
		if (types == null) {
			return false;
		}
		for (InventoryType type : types) {
			if (inventory.getType() == type) {
				return true;
			}
		}
		return false;
	}

	public static Inventory getChestInventory(Inventory inventory) {
		if (!checkInventory(inventory, InventoryType.CHEST)) {
			return null;
		}
		return inventory;
	}

	public static Inventory getDispenserInventory(Inventory inventory) {
		if (!checkInventory(inventory, InventoryType.DISPENSER)) {
			return null;
		}
		return inventory;
	}

	public static Inventory getDropperInventory(Inventory inventory) {
		if (!checkInventory(inventory, InventoryType.DROPPER)) {
			return null;
		}
		return inventory;
	}

	public static FurnaceInventory getFurnaceInventory(Inventory inventory) {
		if (!checkInventory(inventory, InventoryType.FURNACE, InventoryType.BLAST_FURNACE, InventoryType.SMOKER)) {
			return null;
		}
		if (!(inventory instanceof FurnaceInventory)) {
			return null;
		}
		return (FurnaceInventory) inventory;
	}

	public static Inventory getWorkbenchInventory(Inventory inventory) {
		if (!checkInventory(inventory, InventoryType.WORKBENCH)) {
			return null;
		}
		return inventory;
	}

	public static CraftingInventory getCraftingInventory(Inventory inventory) {
		if (!checkInventory(inventory, InventoryType.CRAFTING)) {
			return null;
		}
		if (!(inventory instanceof CraftingInventory)) {
			return null;
		}
		return (CraftingInventory) inventory;
	}

	public static EnchantingInventory getEnchantingInventory(Inventory inventory) {
		if (!checkInventory(inventory, InventoryType.ENCHANTING)) {
			return null;
		}
		if (!(inventory instanceof EnchantingInventory)) {
			return null;
		}
		return (EnchantingInventory) inventory;
	}

	public static BrewerInventory getBrewingInventory(Inventory inventory) {
		if (!checkInventory(inventory, InventoryType.BREWING)) {
			return null;
		}
		if (!(inventory instanceof BrewerInventory)) {
			return null;
		}
		return (BrewerInventory) inventory;
	}

	public static PlayerInventory getPlayerInventory(Inventory inventory) {
		if (!checkInventory(inventory, InventoryType.PLAYER)) {
			return null;
		}
		if (!(inventory instanceof PlayerInventory)) {
			return null;
		}
		return (PlayerInventory) inventory;
	}

	// TODO: Figure out which inventory class instance is being returned with a creative inventory
	public static Inventory getCreativeInventory(Inventory inventory) {
		if (!checkInventory(inventory, InventoryType.CREATIVE)) {
			return null;
		}
		return inventory;
	}

	public static MerchantInventory getMerchantInventory(Inventory inventory) {
		if (!checkInventory(inventory, InventoryType.MERCHANT)) {
			return null;
		}
		if (!(inventory instanceof MerchantInventory)) {
			return null;
		}
		return (MerchantInventory) inventory;
	}

	public static Inventory getEnderChestInventory(Inventory inventory) {
		if (!checkInventory(inventory, InventoryType.ENDER_CHEST)) {
			return null;
		}
		return inventory;
	}

	public static AnvilInventory getAnvilInventory(Inventory inventory) {
		if (!checkInventory(inventory, InventoryType.ANVIL)) {
			return null;
		}
		if (!(inventory instanceof AnvilInventory)) {
			return null;
		}
		return (AnvilInventory) inventory;
	}

	public static BeaconInventory getBeaconInventory(Inventory inventory) {
		if (!checkInventory(inventory, InventoryType.BEACON)) {
			return null;
		}
		if (!(inventory instanceof BeaconInventory)) {
			return null;
		}
		return (BeaconInventory) inventory;
	}

	public static Inventory getHopperInventory(Inventory inventory) {
		if (!checkInventory(inventory, InventoryType.HOPPER)) {
			return null;
		}
		return inventory;
	}

	public static Inventory getShulkerBoxInventory(Inventory inventory) {
		if (!checkInventory(inventory, InventoryType.SHULKER_BOX)) {
			return null;
		}
		return inventory;
	}

	public static Inventory getBarrelInventory(Inventory inventory) {
		if (!checkInventory(inventory, InventoryType.BARREL)) {
			return null;
		}
		return inventory;
	}

	public static FurnaceInventory getBlastFurnaceInventory(Inventory inventory) {
		return getFurnaceInventory(inventory);
	}

	public static LecternInventory getLecternInventory(Inventory inventory) {
		if (!checkInventory(inventory, InventoryType.LECTERN)) {
			return null;
		}
		if (!(inventory instanceof LecternInventory)) {
			return null;
		}
		return (LecternInventory) inventory;
	}

	public static FurnaceInventory getSmokerInventory(Inventory inventory) {
		return getFurnaceInventory(inventory);
	}

	public static LoomInventory getLoomInventory(Inventory inventory) {
		if (!checkInventory(inventory, InventoryType.LOOM)) {
			return null;
		}
		if (!(inventory instanceof LoomInventory)) {
			return null;
		}
		return (LoomInventory) inventory;
	}

	public static CartographyInventory getCartographyInventory(Inventory inventory) {
		if (!checkInventory(inventory, InventoryType.CARTOGRAPHY)) {
			return null;
		}
		if (!(inventory instanceof CartographyInventory)) {
			return null;
		}
		return (CartographyInventory) inventory;
	}

	public static GrindstoneInventory getGrindstoneInventory(Inventory inventory) {
		if (!checkInventory(inventory, InventoryType.GRINDSTONE)) {
			return null;
		}
		if (!(inventory instanceof GrindstoneInventory)) {
			return null;
		}
		return (GrindstoneInventory) inventory;
	}

	public static StonecutterInventory getStoneCutterInventory(Inventory inventory) {
		if (!checkInventory(inventory, InventoryType.STONECUTTER)) {
			return null;
		}
		if (!(inventory instanceof StonecutterInventory)) {
			return null;
		}
		return (StonecutterInventory) inventory;
	}

}
