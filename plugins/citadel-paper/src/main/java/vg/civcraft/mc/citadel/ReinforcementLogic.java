package vg.civcraft.mc.citadel;

import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.CoralWallFan;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import vg.civcraft.mc.citadel.events.ReinforcementCreationEvent;
import vg.civcraft.mc.citadel.events.ReinforcementDestructionEvent;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.world.WorldUtils;
import vg.civcraft.mc.namelayer.group.Group;

public final class ReinforcementLogic {

	private ReinforcementLogic() {
	}

	/**
	 * Inserts a new reinforcements into the cache, queues it for persistence and
	 * plays particle effects for creation
	 *
	 * @param rein Reinforcement just created
	 */
	public static void createReinforcement(Reinforcement rein) {
		Citadel.getInstance().getReinforcementManager().putReinforcement(rein);
		if (rein.getType().getCreationEffect() != null) {
			rein.getType().getCreationEffect().playEffect(rein);
		}
	}

	public static Reinforcement callReinforcementCreationEvent(Player player, Block block, ReinforcementType type,
															   Group group) {
		Reinforcement rein = new Reinforcement(block.getLocation(), type, group);
		ReinforcementCreationEvent event = new ReinforcementCreationEvent(player, rein);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return null;
		}
		return rein;
	}

	public static void damageReinforcement(Reinforcement rein, float damage, Entity source) {
		float futureHealth = rein.getHealth() - damage;
		if (futureHealth <= 0) {
			ReinforcementDestructionEvent event = new ReinforcementDestructionEvent(rein, damage, source);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				return;
			}
		}
		futureHealth = Math.min(futureHealth, rein.getType().getHealth());
		rein.setHealth(futureHealth);
		if (rein.isBroken()) {
			if (rein.getType().getDestructionEffect() != null) {
				rein.getType().getDestructionEffect().playEffect(rein);
			}
		} else {
			if (rein.getType().getDamageEffect() != null) {
				rein.getType().getDamageEffect().playEffect(rein);
			}
		}
	}

	public static float getDamageApplied(Reinforcement reinforcement) {
		float damageAmount = 1.0F;
		if (!reinforcement.isMature()) {
			double timeExisted = System.currentTimeMillis() - reinforcement.getCreationTime();
			double progress = timeExisted / reinforcement.getType().getMaturationTime();
			damageAmount /= progress;
			damageAmount *= reinforcement.getType().getMaturationScale();
		}
		damageAmount *= getDecayDamage(reinforcement);
		return damageAmount;
	}

	public static double getDecayDamage(Reinforcement reinforcement) {
		if (reinforcement.getGroup() != null) {
			long lastRefresh = reinforcement.getGroup().getActivityTimeStamp();
			return reinforcement.getType().getDecayDamageMultipler(lastRefresh);
		} else {
			return reinforcement.getType().getDeletedGroupMultiplier();
		}
	}

	public static Reinforcement getReinforcementAt(Location location) {
		return Citadel.getInstance().getReinforcementManager().getReinforcement(location);
	}

	public static Reinforcement getReinforcementProtecting(Block block) {
		if (!WorldUtils.isValidBlock(block)) {
			return null;
		}
		Reinforcement reinforcement = getReinforcementAt(block.getLocation());
		if (reinforcement != null) {
			return reinforcement;
		}
		switch (block.getType()) {
			// Chests are awkward since you can place both sides of a double chest
			// independently, which isn't true for
			// beds, plants, or doors, so this needs to be accounted for and
			// "getResponsibleBlock()" isn't appropriate
			// for the following logic: that both sides protect each other; that if either
			// block is reinforced, then
			// the chest as a whole remains protected.
			case CHEST:
			case TRAPPED_CHEST: {
				Chest chest = (Chest) block.getBlockData();
				BlockFace facing = chest.getFacing();
				switch (chest.getType()) {
					case LEFT: {
						BlockFace face = WorldUtils.turnClockwise(facing);
						return getReinforcementAt(block.getLocation().add(face.getDirection()));
					}
					case RIGHT: {
						BlockFace face = WorldUtils.turnAntiClockwise(facing);
						return getReinforcementAt(block.getLocation().add(face.getDirection()));
					}
					default: {
						return null;
					}
				}
			}
			default: {
				Block responsible = getResponsibleBlock(block);
				if (Objects.equals(block, responsible)) {
					return null;
				}
				return getReinforcementAt(responsible.getLocation());
			}
		}
	}

	/**
	 * Some blocks, crops in particular, can not be reinforced but instead have
	 * their reinforcement behavior tied to a source block. This method will get
	 * that source block, which may be the given block itself. It does not look at
	 * reinforcement data at all, it merely applies logic based on block type and
	 * physics checks
	 *
	 * @param block Block to get responsible block for
	 * @return Block which reinforcement would protect the given block
	 */

	public static Block getResponsibleBlock(Block block) {
		// Do not put [double] chests in here.
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
			case WARPED_FUNGUS:
			case CRIMSON_FUNGUS:
			case BAMBOO_SAPLING:
			case WHEAT:
			case CARROTS:
			case POTATOES:
			case BEETROOTS:
			case SWEET_BERRY_BUSH:
			case MELON_STEM:
			case PUMPKIN_STEM:
			case ATTACHED_MELON_STEM:
			case ATTACHED_PUMPKIN_STEM:
			case WARPED_ROOTS:
			case CRIMSON_ROOTS:
			case NETHER_SPROUTS:
			case WITHER_ROSE:
			case LILY_OF_THE_VALLEY:
			case CORNFLOWER:
			case SEA_PICKLE:
			case FERN:
			case KELP:
			case GRASS:
			case SEAGRASS:
			case TUBE_CORAL:
			case TUBE_CORAL_FAN:
			case BRAIN_CORAL:
			case BRAIN_CORAL_FAN:
			case BUBBLE_CORAL:
			case BUBBLE_CORAL_FAN:
			case FIRE_CORAL:
			case FIRE_CORAL_FAN:
			case HORN_CORAL:
			case HORN_CORAL_FAN:
			case DEAD_TUBE_CORAL:
			case DEAD_TUBE_CORAL_FAN:
			case DEAD_BRAIN_CORAL:
			case DEAD_BRAIN_CORAL_FAN:
			case DEAD_BUBBLE_CORAL:
			case DEAD_BUBBLE_CORAL_FAN:
			case DEAD_FIRE_CORAL:
			case DEAD_FIRE_CORAL_FAN:
			case DEAD_HORN_CORAL:
			case DEAD_HORN_CORAL_FAN:
			case NETHER_WART: {
				return block.getRelative(BlockFace.DOWN);
			}
			case TWISTING_VINES: {
				// scan downwards for first different block
				Block below = block.getRelative(BlockFace.DOWN);
				while (below.getType() == block.getType() || below.getType() == Material.TWISTING_VINES_PLANT) {
					below = below.getRelative(BlockFace.DOWN);
				}
				return below;
			}
			case SUGAR_CANE:
			case BAMBOO:
			case ROSE_BUSH:
			case TWISTING_VINES_PLANT:
			case CACTUS:
			case SUNFLOWER:
			case LILAC:
			case TALL_GRASS:
			case LARGE_FERN:
			case TALL_SEAGRASS:
			case KELP_PLANT:
			case PEONY: {
				// scan downwards for first different block
				Block below = block.getRelative(BlockFace.DOWN);
				while (below.getType() == block.getType()) {
					below = below.getRelative(BlockFace.DOWN);
				}
				return below;
			}
			case ACACIA_DOOR:
			case BIRCH_DOOR:
			case DARK_OAK_DOOR:
			case IRON_DOOR:
			case SPRUCE_DOOR:
			case JUNGLE_DOOR:
			case WARPED_DOOR:
			case CRIMSON_DOOR:
			case OAK_DOOR: {
				if (block.getRelative(BlockFace.UP).getType() != block.getType()) {
					// block is upper half of a door
					return block.getRelative(BlockFace.DOWN);
				}
				return block;
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
			case YELLOW_BED: {
				Bed bed = (Bed) block.getBlockData();
				if (bed.getPart() == Bed.Part.HEAD) {
					return block.getRelative(bed.getFacing().getOppositeFace());
				}
				return block;
			}
			case TUBE_CORAL_WALL_FAN:
			case BRAIN_CORAL_WALL_FAN:
			case BUBBLE_CORAL_WALL_FAN:
			case FIRE_CORAL_WALL_FAN:
			case HORN_CORAL_WALL_FAN:
			case DEAD_TUBE_CORAL_WALL_FAN:
			case DEAD_BRAIN_CORAL_WALL_FAN:
			case DEAD_BUBBLE_CORAL_WALL_FAN:
			case DEAD_FIRE_CORAL_WALL_FAN:
			case DEAD_HORN_CORAL_WALL_FAN: {
				CoralWallFan cwf = (CoralWallFan) block.getBlockData();
				return block.getRelative(cwf.getFacing().getOppositeFace());
			}
			case WEEPING_VINES: {
				// scan upwards
				Block above = block.getRelative(BlockFace.UP);
				while (above.getType() == block.getType() || above.getType() == Material.WEEPING_VINES_PLANT) {
					above = above.getRelative(BlockFace.UP);
				}
				return above;
			}
			case WEEPING_VINES_PLANT: {
				// scan upwards
				Block above = block.getRelative(BlockFace.UP);
				while (above.getType() == block.getType()) {
					above = above.getRelative(BlockFace.UP);
				}
				return above;
			}
			default: {
				return block;
			}
		}
	}

	/**
	 * Checks if at the given block is a container, which is not insecure and which
	 * the player can not access due to missing perms
	 *
	 * @param player the player attempting to access stuff
	 * @param block  Block to check for
	 * @return True if the player can not do something like placing an adjacent
	 * chest or comparator, false otherwise
	 */
	public static boolean isPreventingBlockAccess(Player player, Block block) {
		if (block == null) {
			return false;
		}
		if (block.getState() instanceof InventoryHolder) {
			Reinforcement rein = getReinforcementProtecting(block);
			if (rein == null || rein.isInsecure()) {
				return false;
			}
			return !rein.hasPermission(player, CitadelPermissionHandler.getChests());
		}
		return false;
	}

}
