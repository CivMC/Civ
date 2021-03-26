package vg.civcraft.mc.citadel;

import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

/**
 * Just a useful class with general and misplaced methods that can be called
 * from anywhere.
 *
 */
public class CitadelUtility {
	
	private CitadelUtility() {
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
			case NETHER_WART:
			case SUGAR_CANE:
			case CACTUS:
			case SUNFLOWER:
			case LILAC:
			case PEONY:
			case TALL_GRASS:
			case GRASS:
			case TWISTING_VINES:
			case TWISTING_VINES_PLANT:
			case WEEPING_VINES:
			case WEEPING_VINES_PLANT:
			case KELP:
			case KELP_PLANT:
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

	public static boolean consumeReinforcementItems(Player player, ReinforcementType type, boolean consumeExtra) {
		ItemMap toRemove = new ItemMap(type.getItem());
		if (consumeExtra) {
			toRemove.addItemAmount(type.getItem(), 1);
		}
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
				CitadelPermissionHandler.getReinforce())) {
			CitadelUtility.sendAndLog(player, ChatColor.RED,
					"You seem to have lost permission to reinforce on " + group.getName());
			Citadel.getInstance().getStateManager().setState(player, null);
			return true;
		}
		block = ReinforcementLogic.getResponsibleBlock(block);
		// check if reinforcement already exists
		Reinforcement existingRein = Citadel.getInstance().getReinforcementManager().getReinforcement(block);
		if (existingRein != null) {
			// something like a slab, we just ignore this
			return false;
		}
		// check if reinforcement can reinforce that block
		if (!type.canBeReinforced(block.getType())) {
			CitadelUtility.sendAndLog(player, ChatColor.RED, type.getName() + " can not reinforce " + block.getType());
			return true;
		}
		ItemMap playerItems = new ItemMap(player.getInventory());
		if (player.getInventory().getItemInOffHand() != null) {
			playerItems.addItemStack(player.getInventory().getItemInOffHand());
		}
		// check inventory
		int available = playerItems.getAmount(type.getItem());
		int required = type.getItem().getAmount();
		boolean consumeExtra = block.getType() == type.getItem().getType();
		if (consumeExtra) {
			//make sure they're not trying to reinforce with the single block thats also being placed
			required++;
		}
		if (available < required) {
			Citadel.getInstance().getStateManager().setState(player, null);
			CitadelUtility.sendAndLog(player, ChatColor.RED, "You have no items left to reinforce with " + type.getName());
			return true;
		}
		Reinforcement newRein = ReinforcementLogic.callReinforcementCreationEvent(player, block, type, group);
		if (newRein == null) {
			return true;
		}
		// remove from inventory
		if (!CitadelUtility.consumeReinforcementItems(player, type, consumeExtra)) {
			return true;
		}
		// create reinforcement
		if (Citadel.getInstance().getConfigManager().logCreation()) {
			Citadel.getInstance().getLogger().info(player.getName() + " created reinforcement with " + type.getName()
					+ " for " + block.getType().toString() + " at " + block.getLocation().toString());
		}
		ReinforcementLogic.createReinforcement(newRein);
		return false;
	}
}
