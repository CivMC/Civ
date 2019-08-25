package vg.civcraft.mc.citadel;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import vg.civcraft.mc.citadel.events.ReinforcementCreationEvent;
import vg.civcraft.mc.citadel.listener.BlockListener;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.namelayer.group.Group;

public class ReinforcementLogic {

	public static Reinforcement createReinforcement(Player player, Block block, ReinforcementType type, Group group) {
		Reinforcement rein = new Reinforcement(block.getLocation(), type, group);
		Citadel.getInstance().getChunkMetaManager().put(rein);
		if (type.getCreationEffect() != null) {
			type.getCreationEffect().playEffect(rein);
		}
		Bukkit.getPluginManager().callEvent(new ReinforcementCreationEvent(player, rein));
		return rein;
	}

	public static void damageReinforcement(Reinforcement rein, double damage) {
		rein.setHealth(rein.getHealth() - damage);
		if (rein.isBroken()) {
			Citadel.getInstance().getChunkMetaManager().remove(rein);
			if (rein.getType().getDestructionEffect() != null) {
				rein.getType().getDestructionEffect().playEffect(rein);
			}
		} else {
			if (rein.getType().getDamageEffect() != null) {
				rein.getType().getDamageEffect().playEffect(rein);
			}
			rein.setDirty(true);
		}
	}

	public static double getDamageApplied(Reinforcement reinforcement) {
		double damageAmount = 1.0;
		if (!reinforcement.isMature()) {
			double timeExisted = System.currentTimeMillis() - reinforcement.getCreationTime();
			double progress = timeExisted / reinforcement.getType().getMaturationTime();
			damageAmount /= (1.0 - progress);
			damageAmount *= reinforcement.getType().getMaturationScale();
		}
		long lastRefresh = reinforcement.getGroup().getActivityTimeStamp();
		damageAmount *= reinforcement.getType().getDecayDamageMultipler(lastRefresh);
		return damageAmount;
	}

	public static Reinforcement getReinforcementAt(Location loc) {
		return Citadel.getInstance().getChunkMetaManager().get(loc);
	}

	public static Reinforcement getReinforcementProtecting(Block b) {
		Reinforcement directReinforcement = Citadel.getInstance().getChunkMetaManager().get(b.getLocation());
		if (directReinforcement != null) {
			return directReinforcement;
		}
		Block actual = getResponsibleBlock(b);
		return resolveDoubleChestReinforcement(actual);
	}

	/**
	 * Some blocks, crops in particular, can not be reinforced but instead have
	 * their reinforcement behavior tied to a source block. This method will get
	 * that source block, which may be the given block itself. It does not look at
	 * reinforcement data at all, it merely applies logic based on block type and
	 * physics checks
	 * 
	 * @param block Block to get responsible block for
	 * @return Block whichs reinforcement would protect the given block
	 */
	public static Block getResponsibleBlock(Block block) {
		switch (block.getType()) {
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
			return block.getRelative(BlockFace.DOWN);
		case SUGAR_CANE:
		case CACTUS:
		case SUNFLOWER:
		case LILAC:
		case PEONY:
			// scan downwards for first different block
			Block below = block.getRelative(BlockFace.DOWN);
			while (below.getType() == block.getType()) {
				below = below.getRelative(BlockFace.DOWN);
			}
			return below;
		case ACACIA_DOOR:
		case BIRCH_DOOR:
		case DARK_OAK_DOOR:
		case IRON_DOOR:
		case SPRUCE_DOOR:
		case JUNGLE_DOOR:
		case OAK_DOOR:
			if (block.getRelative(BlockFace.UP).getType() != block.getType()) {
				// block is upper half of a door
				return block.getRelative(BlockFace.DOWN);
			}
		case BLACK_BED:
		case BLUE_BED:
		case BROWN_BED:
		case CYAN_BED:
		case GRAY_BED:
		case GREEN_BED:
		case MAGENTA_BED:
		case LIME_BED:
		case ORANGE_BED:
		case PURPLE_BED:
		case PINK_BED:
		case WHITE_BED:
		case LIGHT_GRAY_BED:
		case LIGHT_BLUE_BED:
		case RED_BED:
		case YELLOW_BED:
			Bed bed = (Bed) block.getBlockData();
			if (bed.getPart() == Bed.Part.HEAD) {
				return block.getRelative(((Bed) block.getState().getData()).getFacing().getOppositeFace());
			}
		default:
			return block;
		}
	}

	/**
	 * Checks if at the given block is a container, which is not insecure and which
	 * the player can not access due to missing perms
	 * 
	 * @param block Block to check for
	 * @return True if the player can not do something like placing an adjacent
	 *         chest or comparator, false otherwise
	 */
	public static boolean isPreventingBlockAccess(Player player, Block block) {
		if (block == null) {
			return false;
		}
		if (block.getState() instanceof InventoryHolder) {
			Reinforcement rein = ReinforcementLogic.resolveDoubleChestReinforcement(block);
			if (rein == null) {
				return false;
			}
			if (rein.isInsecure()) {
				return false;
			}
			return !rein.hasPermission(player, Citadel.chestPerm);
		}
		return false;
	}

	public static Reinforcement resolveDoubleChestReinforcement(Block block) {
		Material mat = block.getType();
		Reinforcement rein = Citadel.getInstance().getChunkMetaManager().get(block);
		if (rein != null || (mat != Material.CHEST && mat != Material.TRAPPED_CHEST)) {
			return rein;
		}
		for (BlockFace face : BlockListener.PLANAR_SIDES) {
			Block relative = block.getRelative(face);
			if (relative.getType() != mat) {
				continue;
			}
			rein = Citadel.getInstance().getChunkMetaManager().get(relative);
			if (rein != null) {
				return rein;
			}
		}
		return null;
	}
}
