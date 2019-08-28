package vg.civcraft.mc.citadel;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

/**
 * Just a useful class with general and misplaced methods that can be called
 * from anywhere.
 *
 */
public class CitadelUtility {
	
	private CitadelUtility() {
	}

	/**
	 * Overload for dropItemAtLocation(Location l, ItemStack is) that accepts a
	 * block parameter.
	 * 
	 * @param b  The block to drop it at
	 * @param is The item to drop
	 *
	 * @author GordonFreemanQ
	 */
	public static void dropItemAtLocation(Block b, ItemStack is) {
		if (b == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING, "Utility dropItemAtLocation block called with null");
			return;
		}
		dropItemAtLocation(b.getLocation(), is);
	}

	/**
	 * A better version of dropNaturally that mimics normal drop behavior.
	 *
	 * The built-in version of Bukkit's dropItem() method places the item at the
	 * block vertex which can make the item jump around. This method places the item
	 * in the middle of the block location with a slight vertical velocity to mimic
	 * how normal broken blocks appear.
	 * 
	 * @param l  The location to drop the item
	 * @param is The item to drop
	 *
	 * @author GordonFreemanQ
	 */
	public static void dropItemAtLocation(final Location l, final ItemStack is) {
		// Schedule the item to drop 1 tick later
		Bukkit.getScheduler().scheduleSyncDelayedTask(Citadel.getInstance(), () -> {
			try {
				l.getWorld().dropItem(l.add(0.5, 0.5, 0.5), is).setVelocity(new Vector(0, 0.05, 0));
			} catch (Exception e) {
				Citadel.getInstance().getLogger().log(Level.WARNING, "Utility dropItemAtLocation called but errored: ",
						e);
			}
		}, 1);
	}

	public static boolean isPlant(Block plant) {
		switch (plant.getType()) {
		case DANDELION:
		case POPPY:
		case BLUE_ORCHID:
		case ALLIUM:
		case AZURE_BLUET:
		case ORANGE_TULIP:
		case RED_TULIP:
		case PINK_TULIP:
		case WHITE_TULIP:
		case OXEYE_DAISY:
		case ACACIA_SAPLING:
		case BIRCH_SAPLING:
		case DARK_OAK_SAPLING:
		case JUNGLE_SAPLING:
		case OAK_SAPLING:
		case SPRUCE_SAPLING:
		case WHEAT:
		case CARROTS:
		case POTATOES:
		case BEETROOTS:
		case MELON_STEM:
		case PUMPKIN_STEM:
		case ATTACHED_MELON_STEM:
		case ATTACHED_PUMPKIN_STEM:
		case NETHER_WART_BLOCK:
		case SUGAR_CANE:
		case CACTUS:
		case SUNFLOWER:
		case LILAC:
		case PEONY:
			return true;
		default:
			return false;
		}
	}

	public static void sendAndLog(CommandSender receiver, ChatColor color, String message) {
		receiver.sendMessage(color + message);
		if (Citadel.getInstance().getConfigManager().logMessages()) {
			Citadel.getInstance().getLogger().log(Level.INFO, "Sent {0} reply {1}",
					new Object[] { receiver.getName(), message });
		}
	}

	public static void debugLog(String msg) {
		if (Citadel.getInstance().getConfigManager().isDebugEnabled()) {
			Citadel.getInstance().getLogger().info(msg);
		}
	}

	public static boolean consumeReinforcementItems(Player player, ReinforcementType type) {
		ItemMap toRemove = new ItemMap(type.getItem());
		if (!toRemove.removeSafelyFrom(player.getInventory())) {
			CitadelUtility.sendAndLog(player, ChatColor.RED,
					"Failed to remove items needed for " + type.getName() + " reinforcement from your inventory");
			Citadel.getInstance().getStateManager().setState(player, null);
			return false;
		}
		return true;
	}

	public static boolean attemptReinforcementCreation(Block block, ReinforcementType type, Group group,
			Player player) {
		// check if group still exists
		if (!group.isValid()) {
			CitadelUtility.sendAndLog(player, ChatColor.RED,
					"The group " + group.getName() + " seems to have been deleted in the mean time");
			Citadel.getInstance().getStateManager().setState(player, null);
			return true;
		}
		// check if player still has permission
		if (!NameAPI.getGroupManager().hasAccess(group, player.getUniqueId(),
				PermissionType.getPermission(Citadel.reinforcePerm))) {
			CitadelUtility.sendAndLog(player, ChatColor.RED,
					"You seem to have lost permission to reinforce on " + group.getName());
			Citadel.getInstance().getStateManager().setState(player, null);
			return true;
		}
		// check if reinforcement already exists
		Reinforcement rein = Citadel.getInstance().getReinforcementManager().getReinforcement(block);
		if (rein != null) {
			// something like a slab, we just ignore this
			return false;
		}
		// check if reinforcement can reinforce that block
		if (!type.canBeReinforced(block.getType())) {
			CitadelUtility.sendAndLog(player, ChatColor.RED, type.getName() + " can not reinforce " + block.getType());
			return true;
		}
		ItemMap playerItems = new ItemMap(player.getInventory());
		// check inventory
		int available = playerItems.getAmount(type.getItem());
		if (available == 0) {
			Citadel.getInstance().getStateManager().setState(player, null);
			CitadelUtility.sendAndLog(player, ChatColor.RED, "You have no items left to reinforce with " + type.getName());
			return true;
		}
		// remove from inventory
		if (!CitadelUtility.consumeReinforcementItems(player, type)) {
			return true;
		}
		// create reinforcement
		if (Citadel.getInstance().getConfigManager().logCreation()) {
			Citadel.getInstance().getLogger().info(player.getName() + " created reinforcement with " + type.getName()
					+ " for " + block.getType().toString() + " at " + block.getLocation().toString());
		}
		ReinforcementLogic.createReinforcement(player, block, type, group);
		return false;
	}
}
