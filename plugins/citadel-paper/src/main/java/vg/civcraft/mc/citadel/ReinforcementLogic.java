package vg.civcraft.mc.citadel;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Bed;

import vg.civcraft.mc.citadel.listener.BlockListener;
import vg.civcraft.mc.citadel.model.GlobalReinforcementManager;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.namelayer.group.Group;

public class ReinforcementLogic {

	public static void createReinforcement(Block block, ReinforcementType type, Group group) {
		GlobalReinforcementManager worldManager = Citadel.getInstance().getReinforcementManager();
		worldManager.insertReinforcement(new Reinforcement(block.getLocation(), type, group));
	}

	public static void damageReinforcement(Reinforcement rein, double damage) {
		rein.setHealth(rein.getHealth() - damage);
		rein.getType().getReinforcementEffect().playEffect(rein.getLocation().clone().add(0.5, 0.5, 0.5));
	}

	public static double getDamageApplied(Player player, Reinforcement reinforcement) {
		double damageAmount = 1.0;
		if (!reinforcement.isMature()) {
			double timeExisted = (double) (System.currentTimeMillis() - reinforcement.getCreationTime());
			double progress = timeExisted / (double) reinforcement.getType().getMaturationTime();
			damageAmount /= (1.0 - progress);
			damageAmount *= reinforcement.getType().getMaturationScale();
		}
		return damageAmount;
	}

	public static Reinforcement getReinforcementProtecting(Block b) {
		Reinforcement directReinforcement = Citadel.getInstance().getReinforcementManager().getReinforcement(b);
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
		case NETHER_WARTS:
		case YELLOW_FLOWER:
		case SAPLING:
		case WHEAT:
		case CARROT:
		case POTATO:
		case CROPS:
		case BEETROOT_BLOCK:
		case MELON_STEM:
		case PUMPKIN_STEM:
			return block.getRelative(BlockFace.DOWN);
		case RED_ROSE:
		case SUGAR_CANE_BLOCK:
		case CACTUS:
			// scan downwards for first different block
			Block below = block.getRelative(BlockFace.DOWN);
			while (below.getType() == block.getType()) {
				below = below.getRelative(BlockFace.DOWN);
			}
			return below;
		case ACACIA_DOOR:
		case BIRCH_DOOR:
		case DARK_OAK_DOOR:
		case IRON_DOOR_BLOCK:
		case SPRUCE_DOOR:
		case JUNGLE_DOOR:
		case WOODEN_DOOR:
		case WOOD_DOOR:
			if (block.getRelative(BlockFace.UP).getType() != block.getType()) {
				// block is upper half of a door
				return block.getRelative(BlockFace.DOWN);
			}
		case BED_BLOCK:
			if (((Bed) block.getState().getData()).isHeadOfBed()) {
				return block.getRelative(((Bed) block.getState().getData()).getFacing().getOppositeFace());
			}
		}
		return block;
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

	public static Reinforcement resolveDoubleChestReinforcement(Block b) {
		Material mat = b.getType();
		GlobalReinforcementManager reinMan = Citadel.getInstance().getReinforcementManager();
		Reinforcement rein = reinMan.getReinforcement(b);
		if (rein != null || (mat != Material.CHEST && mat != Material.TRAPPED_CHEST)) {
			return rein;
		}
		for (BlockFace face : BlockListener.planar_sides) {
			Block rel = b.getRelative(face);
			if (rel.getType() != mat) {
				continue;
			}
			rein = reinMan.getReinforcement(rel);
			if (rein != null) {
				return rein;
			}
		}
		return null;
	}
}
