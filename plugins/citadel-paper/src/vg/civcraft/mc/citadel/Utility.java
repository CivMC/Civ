package vg.civcraft.mc.citadel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Bed;
import org.bukkit.material.Door;
import org.bukkit.util.Vector;

import vg.civcraft.mc.citadel.events.ReinforcementCreationEvent;
import vg.civcraft.mc.citadel.listener.BlockListener;
import vg.civcraft.mc.citadel.misc.ReinforcemnetFortificationCancelException;
import vg.civcraft.mc.citadel.reinforcement.MultiBlockReinforcement;
import vg.civcraft.mc.citadel.reinforcement.NaturalReinforcement;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.NaturalReinforcementType;
import vg.civcraft.mc.citadel.reinforcementtypes.NonReinforceableType;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;
/**
 * Just a useful class with general and misplaced methods that can be
 * called from anywhere.
 *
 */
public class Utility {

    private static ReinforcementManager rm = Citadel.getReinforcementManager();
    private static Random rng = new Random();
    /**
     * Creates a PlayerReinforcement or returns null if if player doesn't have
     * the required requirements.
     * @param The Player who created the reinforcement.
     * @param The Group this reinforcement belongs too.
     * @param The Block this reinforcement is occurring on.
     * @param The ReinforcementType that is being reinforced on the block.
     * @param The ItemStack type of the block being placed (if CTF, null if CTR)
     * @return The PlayerReinforcement that comes from these parameters or null if certain checks failed.
     * @throws ReinforcemnetFortificationCancelException
     */
    public static PlayerReinforcement createPlayerReinforcement(Player player, Group g, Block block,
            ReinforcementType type, ItemStack reinfMat) {
		if (player == null || g == null || block == null || type == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility createPlayerReinforcement called with null: {0},{1},{2},{3}", 
					new Object[] {player, g, block, type});
			return null;
		}

        if (g.isDisciplined()) {
            player.sendMessage(ChatColor.RED + "This group is disciplined.");
            if (CitadelConfigManager.shouldLogInternal()) {
            	Citadel.getInstance().getLogger().log(Level.WARNING,
            			"Request to create reinforcement for disciplined group " + g.getName());
            }
            return null;
        }
        if (NonReinforceableType.isNonReinforceable(block.getType())){
            player.sendMessage(ChatColor.RED + "That block cannot be reinforced.");
            if (CitadelConfigManager.shouldLogInternal()) {
            	Citadel.getInstance().getLogger().log(Level.WARNING,
            			"Request to create reinforcement for unreinforceable block " + block.getType());
            }
            return null;
        }

        if (!NameAPI.getGroupManager().hasAccess(g.getName(), player.getUniqueId(), PermissionType.getPermission("REINFORCE"))) {
        	player.sendMessage(ChatColor.RED + "You don't have permission to reinforce on this group");
        	return null;
        }
        
        // Find necessary itemstacks
        final PlayerInventory inv = player.getInventory();
        final int invSize = inv.getSize();
        final ItemStack itemType = type.getItemStack();
        List<Integer> slots = new ArrayList<Integer>(type.getRequiredAmount());
        int requirementscheck =  type.getRequiredAmount();
        if (requirementscheck <= 0) {
            Citadel.Log("Reinforcement requirements too low for " + itemType.getType().name());
            return null;
        }
        if (reinfMat != null && itemType.isSimilar(reinfMat)){ // only in CTF.
            requirementscheck++;
        }
        int requirements = requirementscheck;
        boolean consumeOffhand = false;
        try {
        	//offhand needs special handling
        	
        	ItemStack offSlot = inv.getItemInOffHand();
        	if (offSlot != null && offSlot.isSimilar(itemType)) {
        		requirementscheck -= offSlot.getAmount();
        		consumeOffhand = true;
        	}
        	//cycle through normal inventory
            for (int slot = 0; slot < invSize && requirements > 0; ++slot) {
                final ItemStack slotItem = inv.getItem(slot);
                if (slotItem == null) {
                    continue;
                }
                if (!slotItem.isSimilar(itemType)) {
                    continue;
                }
                requirementscheck -= slotItem.getAmount();
                slots.add(slot);
            }
        } catch (Exception ex) {
            // Eat any inventory size mis-match exceptions, like with the Anvil
        }
        if (requirementscheck > 0) {
            // Not enough reinforcement material
            return null;
        }
        // Fire the creation event
        PlayerReinforcement rein = new PlayerReinforcement(block.getLocation(), 
                type.getHitPoints(), getIntFormofMaturation(System.currentTimeMillis(),type.getItemStack()), 
                getIntFormofAcidMaturation(System.currentTimeMillis(),type.getItemStack()),  
                g, type.getItemStack());
        ReinforcementCreationEvent event = new ReinforcementCreationEvent(rein, block, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            throw new ReinforcemnetFortificationCancelException();
        }
		if (CitadelConfigManager.shouldLogReinforcement()) {
			StringBuffer slb = new StringBuffer();
			if (player != null) {
				slb.append("Player ").append(player.getName()).append(" [").append(player.getUniqueId())
						.append("]");
			}
			slb.append("reinforced a ").append(block.getType()).append(" with a ")
					.append(rein.getMaterial()).append(" reinforcement at ")
					.append(rein.getLocation());
			Citadel.Log(slb.toString());
		}
        // Now eat the materials
        
        // Handle special case with block reinforcements.
        if (type.getMaterial().isBlock()){
            if (slots.size()>1){
                if (inv.getItemInHand().isSimilar(itemType) && PlayerState.get(player).getMode() == ReinforcementMode.REINFORCEMENT_FORTIFICATION && slots.get(0) != inv.getHeldItemSlot()){
                	requirements--;
                }
            }
        }
        if (consumeOffhand) {
        	ItemStack is = inv.getItemInOffHand();
        	int stackSize = is.getAmount();
            int deduction = Math.min(stackSize, requirements);
            if (deduction < stackSize) {
                is.setAmount(stackSize - deduction);
            } else {
                inv.setItemInOffHand(null);
            }
            requirements -= deduction;
        }
        for (final int slot : slots) {
            if (requirements <= 0) {
                break;
            }
            final ItemStack slotItem = inv.getItem(slot);
            final int stackSize = slotItem.getAmount();
            final int deduction = Math.min(stackSize, requirements);
            
            if (deduction < stackSize) {
                slotItem.setAmount(stackSize - deduction);
            } else {
                inv.clear(slot);
            }
            requirements -= deduction;
        }
        if (requirements != 0) {
            Citadel.Log(String.format( "Reinforcement material out of sync %d vs %d", 
					requirements, type.getRequiredAmount()));
        }
        player.updateInventory();
        rm.saveInitialReinforcement(rein);
        return rein;
    }
    
    /**
     * Creates a player reinforcement without consuming any materials. This should only be used
     * for admin tools
     * @param The player who is creating the reinforcement
     * @param The Group this reinforcement belongs too.
     * @param The Block this reinforcement is occurring on.
     * @param The ReinforcementType that is being reinforced on the block.
     * @return The PlayerReinforcement that comes from these parameters or null if certain checks failed.
     * @throws ReinforcemnetFortificationCancelException
     */
    public static PlayerReinforcement createPlayerReinforcementWithoutMaterialConsumption(Player player, 
            Group g, Block block, ReinforcementType type) {
		if (g == null || block == null || type == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility createPlayerReinforcementWithoutMaterialConsumption called with null: {0},{1},{2}", 
					new Object[] {g, block, type});
			return null;
		} else if (player == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility createPlayerReinforcementWithoutMaterialConsumption called with player as null");
		}

    	//no error messages towards the player because this might be called a few thousand times 
        if (g.isDisciplined()) {
            return null;
        }
        if (NonReinforceableType.isNonReinforceable(block.getType())){
            return null;
        }
        PlayerReinforcement rein = new PlayerReinforcement(block.getLocation(), 
                type.getHitPoints(), getIntFormofMaturation(System.currentTimeMillis(),type.getItemStack()), 
                getIntFormofAcidMaturation(System.currentTimeMillis(),type.getItemStack()), 
                g, type.getItemStack());
        ReinforcementCreationEvent event = new ReinforcementCreationEvent(rein, block, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            throw new ReinforcemnetFortificationCancelException();
        }
        rm.saveInitialReinforcement(rein);
        return rein;        
    }
    
    /**
     * Checks if creating a plant reinforcement would result in a 
     * double reinforcement.
     * @param The block of the plant.
     * @return True if it would create a double reinforcement.
     * @return False if it would not.
     */
    public static boolean wouldPlantDoubleReinforce(final Block block) {
		if (block == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility wouldPlantDoubleReinforce called with null");
			return false;
		}
    	
        final Material blockMat = block.getType();
        if (isReinforceablePlant(blockMat)
            && rm.getReinforcement(block.getLocation()) != null) {
            return true;
        }
        final Block above = block.getRelative(BlockFace.UP);
        final Material aboveMat = above.getType();
        if (isReinforceablePlant(aboveMat)) {
            final Set<Material> soilTypes = getPlantSoilTypes(aboveMat);
            if (soilTypes.contains(blockMat)
                    && Citadel.getReinforcementManager().
                    getReinforcement(above.getLocation()) != null) {
              return true;
            }
        }
        return false;
    }
    
    private static boolean isReinforceablePlant(Material mat) {
        // If this list changes, update wouldPlantDoubleReinforce to account
        // for the new soil types.
        return Material.MELON_BLOCK.equals(mat)
            || Material.PUMPKIN.equals(mat);
    }
    /**
     * Returns a list of Materials that this plant can go on.
     * @param Material of the plant.
     * @return List of Materials that it can grow on.
     */
    public static Set<Material> getPlantSoilTypes(Material mat) {
        Set<Material> soilTypes = new HashSet<Material>();
        if (isSoilPlant(mat)) {
            soilTypes.add(Material.SOIL);
        }
        if (isDirtPlant(mat)) {
            soilTypes.add(Material.DIRT);
        }
        if (isGrassPlant(mat)) {
            soilTypes.add(Material.GRASS);
        }
        if (isSandPlant(mat)) {
            soilTypes.add(Material.SAND);
        }
        if (isSoulSandPlant(mat)) {
            soilTypes.add(Material.SOUL_SAND);
        }
        return soilTypes;
    }
    
    private static boolean isSoilPlant(Material mat) {
        return Material.WHEAT.equals(mat)
            || Material.MELON_STEM.equals(mat)
            || Material.PUMPKIN_STEM.equals(mat)
            || Material.CARROT.equals(mat)
            || Material.POTATO.equals(mat)
            || Material.CROPS.equals(mat)
            || Material.MELON_BLOCK.equals(mat)
            || Material.PUMPKIN.equals(mat);
    }

    private static boolean isDirtPlant(Material mat) {
        return Material.SUGAR_CANE_BLOCK.equals(mat)
            || Material.MELON_BLOCK.equals(mat)
            || Material.PUMPKIN.equals(mat);
    }

    private static boolean isGrassPlant(Material mat) {
        return Material.SUGAR_CANE_BLOCK.equals(mat)
            || Material.MELON_BLOCK.equals(mat)
            || Material.PUMPKIN.equals(mat);
    }

    private static boolean isSandPlant(Material mat) {
        return Material.CACTUS.equals(mat)
            || Material.SUGAR_CANE_BLOCK.equals(mat);
    }

    private static boolean isSoulSandPlant(Material mat) {
        return Material.NETHER_WARTS.equals(mat);
    }

    public static boolean isPlant(Block plant) {
        return isPlant(plant.getType());
    }

    public static boolean isPlant(Material mat) {
        return isSoilPlant(mat)
            || isDirtPlant(mat)
            || isGrassPlant(mat)
            || isSandPlant(mat)
            || isSoulSandPlant(mat);
    }

    private static int maxPlantHeight(Block plant) {
        switch(plant.getType()) {
            case CACTUS:
                return 3;
            case SUGAR_CANE_BLOCK:
                return 3;
            default:
                return 1;
        }
    }
    /**
     * Checks to see if a reinforcement is damaged if it exists.
     * @param The block holding the reinforcement.
     * @return Returns false if no reinforcement exists and if it was broken.
     */
    public static boolean maybeReinforcementDamaged(Block block) {
		if (block == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility maybeReinforcementDamaged called with null");
			return false;
		}

        Reinforcement reinforcement = rm.getReinforcement(block.getLocation());
        return reinforcement != null && reinforcementDamaged(null, reinforcement);
    }
    /**
     * Damages the reinforcement.
     * @param The Reinforcement required.
     * @return True if the reinforcement was securable.
     */
    public static boolean reinforcementDamaged(Player player, Reinforcement reinforcement) {
    	if (reinforcement == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility reinforcementDamaged called with null");
			return false;
		}
    	int durability = reinforcement.getDurability();
        int durabilityLoss = 1;
        if (reinforcement instanceof PlayerReinforcement && CitadelConfigManager.isMaturationEnabled()) {
          final int maturationTime = timeUntilMature(reinforcement);
          PlayerReinforcement rein = (PlayerReinforcement) reinforcement;
          ReinforcementType type = ReinforcementType.getReinforcementType(rein.getStackRepresentation());
          
          durabilityLoss = rein.getDamageMultiplier();
          
          if (maturationTime > 0 && type.getMaturationScale() != 0) {
              // the default amount of minutes it takes to mature
              int normal = type.getMaturationTime();
              if (maturationTime == normal) {
                  durabilityLoss = durability;
              } else {
                  double percentTo = (double) maturationTime / (double) normal; // the percent of time left of maturation
                  durabilityLoss = (int) (((double) durabilityLoss / (1.0d - percentTo)) * (double) type.getMaturationScale());
              } // this new code scales smoothly between MaturationScale and a very large number, being closer to 
              // MaturationScale the closer to "done" a maturation cycle
          }
          
          if (durability < durabilityLoss) {
              durabilityLoss = durability;
          }
        }
		int olddurability = durability;
        durability -= durabilityLoss;
        reinforcement.setDurability(durability);
        boolean cancelled = durability > 0;
        if (durability <= 0) {
            cancelled = reinforcementBroken(null, reinforcement);
        } else {
			/* TODO: Move to ReinforcementEvent listener*/
			if (CitadelConfigManager.shouldLogBreaks()) {
				StringBuffer slb = new StringBuffer();
				if (player != null) {
					slb.append("Player ").append(player.getName()).append(" [").append(player.getUniqueId())
							.append("]");
				} else {
					slb.append("Something ");
				}
				slb.append("damaged a ").append(reinforcement.getMaterial());
				int strength = 0;
				if (reinforcement instanceof PlayerReinforcement) {
					slb.append("reinforcement to ");
					ReinforcementType type = ReinforcementType.getReinforcementType(
							((PlayerReinforcement) reinforcement).getStackRepresentation());
					strength = type.getHitPoints();
				} else if (reinforcement instanceof NaturalReinforcement) {
					slb.append("natural reinforcement to ");
					NaturalReinforcementType type = NaturalReinforcementType.getNaturalReinforcementType(
							reinforcement.getType());
					strength = type.getDurability();
				} else {
					slb.append("null reinforcement to ");
				}
				double nratio = strength > 0 ? (double) durability / (double) strength: 0;
				double oratio = strength > 0 ? (double) olddurability / (double) strength: 1;
				if ( nratio <= 0.25 && oratio > 0.25) {
					slb.append("poor (");
				} else if ( nratio <= 0.5 && oratio > 0.5) {
					slb.append("decent (");
				} else if ( nratio <= 0.75 && oratio > 0.75) {
					slb.append("well (");
				} else if ( nratio < 1.0 && oratio == 1.0) {
					slb.append("excellent (");
				}
				slb.append(durability).append(") at ").append(reinforcement.getLocation());
			}
            if (reinforcement instanceof PlayerReinforcement) {
                // leave message
            }
            rm.saveReinforcement(reinforcement);
        }
        return cancelled;
    }
    /**
     * Used to get the amount of time left until a reinforcement is mature.
     * @param Reinforcement.
     * @return Returns 0 if it is mature or the time in minutes until it is mature.
     */
    public static int timeUntilMature(Reinforcement reinforcement) {
    	if (reinforcement == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility timeUntilMature called with null");
			return 0;
		}
    	// Doesn't explicitly save the updated Maturation time into the cache.
        //  That's the responsibility of the caller.
        if (reinforcement instanceof PlayerReinforcement){
            int maturationTime = reinforcement.getMaturationTime();
            if (maturationTime > 0) {
                final int curMinute = (int)(System.currentTimeMillis() / 60000L);
                if (curMinute >= maturationTime) {
                    maturationTime = 0;
                    reinforcement.setMaturationTime(0);
                } else {
                    maturationTime = maturationTime - curMinute;
                }
            }
            return (int) maturationTime; // should be small enough by now
        }
        return 0;
    }
    /**
     * Used to get the amount of time left until a reinforcement's acid component is mature.
     * @param Reinforcement.
     * @return Returns 0 if it is mature or the time in minutes until it is mature.
     */
    public static int timeUntilAcidMature(Reinforcement reinforcement) {
    	if (reinforcement == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility timeUntilAcidMature called with null");
			return 0;
		}        // Doesn't explicitly save the updated Acid Maturation time into the cache.
        //  That's the responsibility of the caller.
        if (reinforcement instanceof PlayerReinforcement){
            int maturationTime = reinforcement.getAcidTime();
            if (maturationTime > 0) {
                final int curMinute = (int)(System.currentTimeMillis() / 60000L);
                if (curMinute >= maturationTime) {
                    maturationTime = 0;
                    reinforcement.setAcidTime(0);
                } else {
                    maturationTime = maturationTime - curMinute;
                }
            }
            return (int) maturationTime; // should be small enough by now
        }
        return 0;
    }
    
    /**
     * 
     * /ctb mode type break
     *
     * @param The Player who broke the reinforcement
     * @param The Reinforcement broken.
     * @return Returns true if it is securable.
     * @return Returns false if it is no securable.
     */
    public static boolean reinforcementBroken(Player player, Reinforcement reinforcement) {
    	if (reinforcement == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility reinforcementBroken called with null reinforcement");
			return false;
		}
    	StringBuffer slb = null;
		if (CitadelConfigManager.shouldLogBreaks()) {
			slb = new StringBuffer();
			if (player != null) {
				slb.append("Player ").append(player.getName()).append(" [").append(player.getUniqueId())
						.append("]");
			} else {
				slb.append("Something ");
			}
			slb.append("broke a ").append(reinforcement.getMaterial()).append(" reinforcement at ")
					.append(reinforcement.getLocation());
		}
        Citadel.getReinforcementManager().deleteReinforcement(reinforcement);
        if (reinforcement instanceof PlayerReinforcement) {
            PlayerReinforcement pr = (PlayerReinforcement)reinforcement;
            ReinforcementType material = ReinforcementType.getReinforcementType(pr.getStackRepresentation());
            if (rng.nextDouble() <= pr.getHealth() * material.getPercentReturn()) {
                Location location = pr.getLocation();
                if (player != null){
                    Inventory inv = player.getInventory();
                    if (CitadelConfigManager.shouldDropReinforcedBlock()){
                        // If we should drop a block instead
                        ItemStack stack = createDroppedReinforcementBlock
                                (reinforcement.getLocation().getBlock(), (PlayerReinforcement) reinforcement);
                        for(ItemStack leftover : inv.addItem(
                                stack).values()) {
                        	dropItemAtLocation(location, leftover);
                        }
                    }
                    else {
                        for(ItemStack leftover : inv.addItem(
                                material.getItemStack())
                                .values()) {
                        	dropItemAtLocation(location, leftover);
                        }
                    }
                }
                else {
                	dropItemAtLocation(location, new ItemStack(material.getMaterial()
                            , material.getReturnValue()));
				}
                if (CitadelConfigManager.shouldLogBreaks()) {
                    slb.append(" - reinf mat refunded");
					Citadel.Log(slb.toString());
                }
            } else if (CitadelConfigManager.shouldLogBreaks()) { 
                slb.append(" - reinf mat lost");
				Citadel.Log(slb.toString());
            }
            return (pr.isDoor() || pr.isContainer());
        }
        if (CitadelConfigManager.shouldLogBreaks()) {
            Citadel.Log(slb.toString());
        }
        return false;  // implicit isSecureable() == false
    }


	/**
	 * A better version of dropNaturally that mimics normal drop behavior.
	 * 
	 * The built-in version of Bukkit's dropItem() method places the item at the block 
	 * vertex which can make the item jump around. 
	 * This method places the item in the middle of the block location with a slight 
	 * vertical velocity to mimic how normal broken blocks appear.
	 * @param l The location to drop the item
	 * @param is The item to drop
	 * 
	 * @author GordonFreemanQ
	 */
	public static void dropItemAtLocation(final Location l, final ItemStack is) {
		// Schedule the item to drop 1 tick later
		Bukkit.getScheduler().scheduleSyncDelayedTask(Citadel.getInstance(), new Runnable() {
			@Override
			public void run() {
				try {
					l.getWorld().dropItem(l.add(0.5, 0.5, 0.5), is).setVelocity(new Vector(0, 0.05, 0));
				} catch (Exception e) {
					Citadel.getInstance().getLogger().log(Level.WARNING,
								"Utility dropItemAtLocation called but errored: ", e);
				}
			}
		}, 1);
	}
	
	
	/**
	 * Overload for dropItemAtLocation(Location l, ItemStack is) that accepts a block parameter.
	 * @param b The block to drop it at
	 * @param is The item to drop
	 * 
	 * @author GordonFreemanQ
	 */
	public static void dropItemAtLocation(Block b, ItemStack is) {
    	if (b == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility dropItemAtLocation block called with null");
			return;
		}
		dropItemAtLocation(b.getLocation(), is);
	}
    
    /**
     * Checks if a Redstone player is trying to power a block.
     * @param The Reinforcement in question.
     * @param The distance the player is from the block.
     * @return Returns true if the player is on the group and has permission.
     * @return Returns false if the player is not on the group or doesn't have permission.
     */
    public static boolean isAuthorizedPlayerNear(PlayerReinforcement reinforcement, double distance) {
    	if (reinforcement == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility isAuthorizedPlayerNear called with null");
			return false;
		}
        Location reinLocation = reinforcement.getLocation();
        double min_x = reinLocation.getX() - distance;
        double min_z = reinLocation.getZ() - distance;
        double max_x = reinLocation.getX() + distance;
        double max_z = reinLocation.getZ() + distance;
        List<Player> onlinePlayers = new ArrayList<Player>();
        for (Player p: Bukkit.getOnlinePlayers()) {
            onlinePlayers.add(p);
        }
        boolean result = false;
        try {
            for (Player player : onlinePlayers) {
                if (player.isDead()) {
                    continue;
                }
                Location playerLocation = player.getLocation();
                double player_x = playerLocation.getX();
                double player_z = playerLocation.getZ();
                // Simple bounding box check to quickly rule out Players
                //  before doing the more expensive playerLocation.distance
                if (player_x < min_x || player_x > max_x ||
                        player_z < min_z || player_z > max_z) {
                    continue;
                }
                if (!reinforcement.canAccessDoors(player)
                        && !player.hasPermission("citadel.admin.accesssecurable")) {
                    continue;
                }
                double distanceSquared = playerLocation.distance(reinLocation);
                if (distanceSquared <= distance) {
                    result = true;
                    break;
                }
            }
        } catch (ConcurrentModificationException e) {
            Citadel.Log("ConcurrentModificationException at redstonePower() in BlockListener");
        }
        return result;
    }
    /**
     * Creates a Natural Reinforcement.
     * @param The block that will have a natural reinforcement
     * @param The Player who damaged the block.
     * @return Returns a natural Reinforcement for the block or null if it isn't meant to have one.
     */
    public static NaturalReinforcement createNaturalReinforcement(Block block, Player player) {
    	if (block == null || player == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility createNaturalReinforcement called with null");
			return null;
		}
        Material material = block.getType();
        NaturalReinforcementType nType = NaturalReinforcementType.
                getNaturalReinforcementType(material);
        if (nType == null) {
            return null;
        }
        int breakCount = nType.getDurability();
        NaturalReinforcement nr = new NaturalReinforcement(block, breakCount);
        ReinforcementCreationEvent event = new ReinforcementCreationEvent(nr, block, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            if (CitadelConfigManager.shouldLogInternal()) {
            	Citadel.getInstance().getLogger().log(Level.INFO,
            			"createNaturalReinforcement for " + block.getType() + " cancelled");
            }

            return null;
        }
        Citadel.getReinforcementManager().saveInitialReinforcement(nr);
        return nr;
    }
    
    /**
     * This method is used to convert Block Reinforcements to itemstacks
     * @param The block that is being converted into a BlockReinforcement
     * @param The reinforcement on it
     * @return Itemstack form of the reinforcement on a block
     */
    public static ItemStack createDroppedReinforcementBlock(Block block, PlayerReinforcement rein){
    	if (block == null || rein == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility createDroppedReinforcementBlock called with null");
			return null;
		}
        ItemStack reinBlock = new ItemStack(block.getType(), 1);
        ItemMeta lore = reinBlock.getItemMeta();
        List<String> info = new ArrayList<String>();
        String reinMat = rein.getMaterial().name();
        String amount = Integer.toString(rein.getStackRepresentation().getAmount());
        String dur = Integer.toString(ReinforcementType.getReinforcementType(rein.getStackRepresentation()).getHitPoints());
        String group = rein.getGroup().getName();
        info.add(reinMat);
        info.add(amount);
        info.add(dur);
        info.add(group);
        if (rein.getStackRepresentation().hasItemMeta()) {
            info.addAll(rein.getStackRepresentation().getItemMeta().getLore());
            lore.setLore(info);
        }
        reinBlock.setItemMeta(lore);
        return reinBlock;
    }
    /**
     * Returns the Reinforcement of a block if it was previously a reinforcement.
     * Importantly though if a group that this block was associated with 
     * gets deleted this block when placed will not belong to anyone.
     * @param The player placing the block
     * @param The stack the player is placing from
     * @param The location where the block is being placed
     * @return The PlayerReiforcement associated with this block
     */
    public static PlayerReinforcement isDroppedReinforcementBlock(Player p, ItemStack stack, Location loc){
    	if (stack == null || loc == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility isDroppedReinforcementBlock called with null");
			return null;
		}
        ItemMeta meta = stack.getItemMeta();
        List<String> lore = meta.getLore();
        try{
	        if (!meta.hasLore()) {
	            return null;
	        }
	        Iterator<String> value = lore.iterator();
	        if (!value.hasNext()) {
	            return null;
	        }
	        Material mat = Material.valueOf(value.next());
	        if (!value.hasNext()) {
	            return null;
	        }
	        int amount = Integer.parseInt(value.next());
	        if (!value.hasNext()) {
	            return null;
	        }
	        int dur = Integer.parseInt(value.next());
	        if (!value.hasNext()) {
	            return null;
	        }
	        String group = value.next();
	        List<String> itemStackInfo = new ArrayList<String>();
	        while(value.hasNext()) {
	            itemStackInfo.add(value.next());
	        }
	        ItemStack newStack = new ItemStack(mat, amount);
	        meta.setLore(itemStackInfo);
	        newStack.setItemMeta(meta);
	        ReinforcementType reinType = ReinforcementType.getReinforcementType(newStack);
	        if (reinType == null) {
	            return null;
	        }
	        Group g = GroupManager.getSpecialCircumstanceGroup(group);
	        PlayerReinforcement rein = new PlayerReinforcement(loc, dur, 
	                getIntFormofMaturation(System.currentTimeMillis(),reinType.getItemStack()),
	                getIntFormofAcidMaturation(System.currentTimeMillis(),reinType.getItemStack()),
	                g, reinType.getItemStack());
	        ReinforcementCreationEvent event = 
	                new ReinforcementCreationEvent(rein, loc.getBlock(), p);
	        Bukkit.getPluginManager().callEvent(event);
	        if (event.isCancelled()) {
	            if (CitadelConfigManager.shouldLogInternal()) {
	            	Citadel.getInstance().getLogger().log(Level.INFO,
	            			"Dropped reinforcement creation event for " + rein.getType() + " cancelled");
	            }
	            return null;
	        }
	        return rein;
        } catch (IllegalArgumentException iae){
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility isDroppedReinforcementBlock failed", iae);        	
        } catch(Exception ex){
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility isDroppedReinforcementBlock failed", ex);
        }
        return null;
    }
    /**
     * If a reinforcement is exploded checks if it has a reinforcement on it
     * or not.
     * @param The block that exploded.
     * @return True if there is a reinforcement.
     * @return False if there is not a reinforcement.
     */
    public static boolean explodeReinforcement(Block block) {
    	if (block == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility explodeReinforcement called with null");
			return false;
		}
    	Reinforcement reinforcement = rm.getReinforcement(block);
        if (reinforcement == null) {
            reinforcement = createNaturalReinforcement(block, null);
        }
        if (reinforcement == null) {
            return false;
        }
        return reinforcementDamaged(null, reinforcement);
    }
    
    /**
     * Creates a MultiBlockReinforcement and saves it to the db. This method is to be used only be other plugins. Citadel 
     * will not use this anywhere. 
     * @param locs The locations that make up the structure.
     * @param g The group this will belong too.
     * @param dur The durability this structure will have.
     * @param mature The amount of time until it is mature (in minutes).
     * @param acid The amount of time until it is mature (if acid -- in minutes).
     * @return
     */
    public static MultiBlockReinforcement createMultiBlockReinforcement(List<Location> locs, Group g, int dur, int mature, int acid){
    	if (locs == null || g == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility createMultiBlockReinforcement called with null");
			return null;
		}
        MultiBlockReinforcement rein = new MultiBlockReinforcement(locs, g, dur, mature, acid, -1);
        ReinforcementCreationEvent event = new ReinforcementCreationEvent(rein, rein.getLocation().getBlock(), null);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            if (CitadelConfigManager.shouldLogInternal()) {
            	Citadel.getInstance().getLogger().log(Level.INFO,
            			"multiblock reinforcement creation event for " + rein.getType() + " cancelled");
            }
            return null;
        }
        rm.saveInitialReinforcement(rein);
        return rein;
    }
    
    public static Block getAttachedChest(Block block) {
    	if (block == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility getAttachedChest called with null");
			return null;
		}
        Material mat = block.getType();
        if (Material.CHEST.equals(mat) || Material.TRAPPED_CHEST.equals(mat)) {
            for (BlockFace face : cardinals) {
                Block b = block.getRelative(face);
                if (b.getType() == mat) {
                    return b;
                }
            }
        }
        return null;
    }
    
    private static BlockFace[] cardinals = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}; 
    
    public static List<Material> doorTypes = new ArrayList<Material>(Arrays.asList(
            Material.WOODEN_DOOR, Material.IRON_DOOR_BLOCK,
            Material.ACACIA_DOOR, Material.BIRCH_DOOR,
            Material.DARK_OAK_DOOR, Material.JUNGLE_DOOR,
            Material.SPRUCE_DOOR, Material.WOOD_DOOR));
    /**
     * Returns the block the Citadel is looking at, example: for beds, doors we want the bottom half.
     * @param block
     * @return Returns the block we want.
     */
    public static Block getRealBlock(Block block){
    	if (block == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility getRealBlock called with null");
			return null;
		}    	
        Block b = block;
        switch (block.getType()){
        case CHEST:
        case TRAPPED_CHEST:
            if (!rm.isReinforced(block)) {
                b = getAttachedChest(block);
            }
            if (b == null) {
                b = block;
            }
            break;
        case WOODEN_DOOR:
        case IRON_DOOR_BLOCK:
        case ACACIA_DOOR:
        case BIRCH_DOOR:
        case DARK_OAK_DOOR:
        case JUNGLE_DOOR:
        case SPRUCE_DOOR:
        case WOOD_DOOR:
            if (!doorTypes.contains(block.getRelative(BlockFace.UP).getType())) {
                b = block.getRelative(BlockFace.DOWN);
            }
            break;
        case BED_BLOCK:
            if (((Bed) block.getState().getData()).isHeadOfBed()) {
                b = block.getRelative(((Bed) block.getState().getData()).getFacing().getOppositeFace());
            }
            break;
        default:
            b = block;
        }
        return b;
    }
    
    private static int getIntFormofMaturation(long creation, ItemStack stack){
    	if (stack == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility getIntFormofMaturation called with null");
			return 0;
		}
        int maturation = (int)(creation / 60000) + 
                ReinforcementType.
                getReinforcementType(stack)
                .getMaturationTime();
        return maturation;
    }
    
    private static int getIntFormofAcidMaturation(long creation, ItemStack stack) {
    	if (stack == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility getIntFormofAcidMaturation called with null");
			return 0;
		}
    	int maturation = (int)(creation / 60000) + 
                ReinforcementType.
                getReinforcementType(stack)
                .getAcidTime();
        return maturation;
    }
    
    public static Block findPlantSoil(Block block){
    	if (block == null) {
			Citadel.getInstance().getLogger().log(Level.WARNING,
					"Utility findPlantSoil called with null");
			return null;
		}    	
        final Set<Material> soilTypes = getPlantSoilTypes(block.getType());
        if(soilTypes.size() <= 0){
            return null;
        }
        //find the plants soil below it
        Block down = block;
        int max_depth = maxPlantHeight(block);
        for(int i =0; i < max_depth; ++i){
            down = down.getRelative(BlockFace.DOWN);
            if(soilTypes.contains(down.getType())){
                return down;
            }
        }
        return null;
    }
    
    public static boolean canPlace(Block block, Player player) {
        Material block_mat = block.getType();
        
        if (block_mat == Material.HOPPER || block_mat == Material.DROPPER){
            for (BlockFace direction : BlockListener.all_sides) {
                Block adjacent = block.getRelative(direction);
                if (!(adjacent.getState() instanceof InventoryHolder)) {
                    continue;
                }
                Reinforcement rein = rm.getReinforcement(adjacent);
                if (null != rein && rein instanceof PlayerReinforcement) {
                    PlayerReinforcement pr = (PlayerReinforcement)rein;
                    if (pr.isInsecure() && !pr.canAccessChests(player)) {
                        return false;
                    }
                }
            }
        }
        if (block_mat == Material.CHEST || block_mat == Material.TRAPPED_CHEST){
            for (BlockFace direction : BlockListener.planar_sides) {
                Block adjacent = block.getRelative(direction);
                if (!(adjacent.getState() instanceof InventoryHolder)) {
                    continue;
                }
                Reinforcement rein = rm.getReinforcement(adjacent);
                if (null != rein && rein instanceof PlayerReinforcement) {
                    PlayerReinforcement pr = (PlayerReinforcement)rein;
                    if (!pr.canAccessChests(player)) {
                        return false;
                    }
                }
            }
        }
        //stops players from modifying the reinforcement on a half slab by placing another block on top
        Reinforcement reinforcement_on_block = Citadel.getReinforcementManager().getReinforcement(block);
        if (reinforcement_on_block instanceof PlayerReinforcement) {
            PlayerReinforcement reinforcement = (PlayerReinforcement) reinforcement_on_block;
            if (!reinforcement.canBypass(player)) {
                return false;
            }
        } else if (reinforcement_on_block != null) {
            return false; //not really sure when this could happen but just in case
        }

        return true;
    }
}
