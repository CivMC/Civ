package com.untamedears.citadel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.Citadel.VerboseMsg;
import com.untamedears.citadel.access.AccessDelegate;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.PlayerState;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.NaturalReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;
import com.untamedears.citadel.entity.ReinforcementKey;
import com.untamedears.citadel.entity.ReinforcementMaterial;
import com.untamedears.citadel.events.CreateReinforcementEvent;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/23/12
 * Time: 3:37 PM
 */
public class Utility {

    private static Map<SecurityLevel, MaterialData> securityMaterial;
    private static Random rng = new Random();
    
    static {
        securityMaterial = new HashMap<SecurityLevel, MaterialData>();
        securityMaterial.put(SecurityLevel.PUBLIC, new Wool(DyeColor.GREEN));
        securityMaterial.put(SecurityLevel.GROUP, new Wool(DyeColor.YELLOW));
        securityMaterial.put(SecurityLevel.PRIVATE, new Wool(DyeColor.RED));
    }

    public static Block getAttachedChest(Block block) {
        Material mat = block.getType();
        if (mat == Material.CHEST || mat == Material.TRAPPED_CHEST) {
            for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
                Block b = block.getRelative(face);
                if (b.getType() == mat) {
                    return b;
                }
            }
        }
        return null;
    }

    public static IReinforcement createNaturalReinforcement(Block block, Player player) {
        Material material = block.getType();
        int breakCount = Citadel
            .getConfigManager()
            .getMaterialBreakCount(material.getId(), block.getY());
        if (breakCount <= 1) {
            return null;
        }
        NaturalReinforcement nr = new NaturalReinforcement(block, breakCount);
        CreateReinforcementEvent event = new CreateReinforcementEvent(nr, block, player);
        Citadel.getStaticServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return null;
        }
        Citadel.getReinforcementManager().addReinforcement(nr);
        return nr;
    }

    @SuppressWarnings("deprecation")
    public static IReinforcement createPlayerReinforcement(Player player, Block block) {
        int blockTypeId = block.getTypeId();
        if (PlayerReinforcement.NON_REINFORCEABLE.contains(blockTypeId)) return null;

        PlayerState state = PlayerState.get(player);
        Faction group = state.getFaction();
        if(group == null) {
            try {
                group = Faction.getPersonalGroup(player.getUniqueId());
            } catch (NullPointerException e){
                sendMessage(player, ChatColor.RED, "You don't seem to have a personal group. Try logging out and back in first");
            }
        }
        if(group == null) {
            return null;
        }
        if (group.isDisciplined()) {
            sendMessage(player, ChatColor.RED, Faction.kDisciplineMsg);
            return null;
        }
        ReinforcementMaterial material;
        switch (state.getMode()) {
            case REINFORCEMENT:
            case REINFORCEMENT_SINGLE_BLOCK:
                Material inHand = player.getItemInHand().getType();
                material = ReinforcementMaterial.get(inHand);
                if (material == null) {
                    sendMessage(player, ChatColor.RED, "Material in hand %s is not a valid reinforcement material.", inHand.name());
                    state.reset();
                    return null;
                }
                break;
            case FORTIFICATION:
                material = state.getReinforcementMaterial();
                break;
            default:
                return null;
        }

        // Find necessary itemstacks
        final PlayerInventory inv = player.getInventory();
        final int invSize = inv.getSize();
        final Material materialType = material.getMaterial();
        List<Integer> slots = new ArrayList<Integer>(material.getRequirements());
        int requirements = material.getRequirements();
        if (requirements <= 0) {
            Citadel.severe("Reinforcement requirements too low for " + materialType);
            return null;
        }
        try {
            for (int slot = 0; slot < invSize && requirements > 0; ++slot) {
                final ItemStack slotItem = inv.getItem(slot);
                if (slotItem == null) {
                    continue;
                }
                if (!slotItem.getType().equals(materialType)) {
                    continue;
                }
                requirements -= slotItem.getAmount();
                slots.add(slot);
            }
        } catch (Exception ex) {
            // Eat any inventory size mis-match exceptions, like with the Anvil
        }
        if (requirements > 0) {
            // Not enough reinforcement material
            return null;
        }
        // Fire the creation event
        PlayerReinforcement reinforcement = new PlayerReinforcement(block, material, group, state.getSecurityLevel());
        CreateReinforcementEvent event = new CreateReinforcementEvent(reinforcement, block, player);
        Citadel.getStaticServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return null;
        }
        // Now eat the materials
        requirements = material.getRequirements();
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
            Citadel.warning(String.format(
                "Reinforcement material out of sync %d vs %d", requirements, material.getRequirements()));
        }
        //TODO: there will eventually be a better way to flush inventory changes to the client
        player.updateInventory();
        reinforcement = (PlayerReinforcement)Citadel.getReinforcementManager().addReinforcement(reinforcement);
        String securityLevelText = state.getSecurityLevel().name();
        if(securityLevelText.equalsIgnoreCase("group")){
        	securityLevelText = securityLevelText + "-" + group.getName();
        }
        sendThrottledMessage(player, ChatColor.GREEN, "Reinforced with %s at security level %s", material.getMaterial().name(), securityLevelText);
        Citadel.verbose(
            VerboseMsg.ReinCreated,
            player.getDisplayName(), material.getMaterialId(), block.getWorld().getName(),
            block.getX(), block.getY(), block.getZ());
        // TODO: enable chained flashers, they're pretty cool
        //new BlockFlasher(block, material.getFlasher()).start(getPlugin());
        //new BlockFlasher(block, material.getFlasher()).chain(securityMaterial.get(state.getSecurityLevel())).start();
        return reinforcement;
    }
    
    public static void sendMessage(CommandSender sender, ChatColor color, String messageFormat, Object... params) {
        sender.sendMessage(color + String.format(messageFormat, params));
    }

    public static void sendThrottledMessage(CommandSender sender, ChatColor color, String messageFormat, Object... params) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerState state = PlayerState.get(player);
            if (System.currentTimeMillis() - state.getLastThrottledMessage() > (1000 * 30)) {
                sendMessage(player, color, messageFormat, params);
            }
            state.setLastThrottledMessage(System.currentTimeMillis());
        }
    }

    public static boolean explodeReinforcement(Block block) {
        AccessDelegate delegate = AccessDelegate.getDelegate(block);
        IReinforcement reinforcement = delegate.getReinforcement();
        if (reinforcement == null) {
            reinforcement = createNaturalReinforcement(block, null);
        }
        if (reinforcement == null) {
            return false;
        }
        return reinforcementDamaged(reinforcement);
    }

    public static boolean isReinforced(Location location) {
        return getReinforcement(location) != null;
    }

    public static boolean isReinforced(Block block) {
        return getReinforcement(block) != null;
    }

    public static IReinforcement getReinforcement(Location location) {
        return getReinforcement(location.getBlock());
    }

    public static IReinforcement getReinforcement(Block block) {
        AccessDelegate delegate = AccessDelegate.getDelegate(block);
        IReinforcement reinforcement = delegate.getReinforcement();
        return reinforcement;
    }

    public static IReinforcement addReinforcement(IReinforcement reinforcement) {
        return Citadel.getReinforcementManager().addReinforcement(reinforcement);
    }

    public static void removeReinforcement(IReinforcement reinforcement) {
        Citadel.getReinforcementManager().removeReinforcement(reinforcement);
    }

    public static boolean isAuthorizedPlayerNear(PlayerReinforcement reinforcement, double distance) {
        ReinforcementKey key = reinforcement.getId();
        World reinWorld = Citadel.getPlugin().getServer().getWorld(key.getWorld());
        Location reinLocation = new Location(
            reinWorld, (double)key.getX(), (double)key.getY(), (double)key.getZ());
        double min_x = reinLocation.getX() - distance;
        double min_z = reinLocation.getZ() - distance;
        double max_x = reinLocation.getX() + distance;
        double max_z = reinLocation.getZ() + distance;
        List<Player> onlinePlayers = reinWorld.getPlayers();
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
                if (!reinforcement.isAccessible(player)
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
            Citadel.warning("ConcurrentModificationException at redstonePower() in BlockListener");
        }
        return result;
    }

    public static boolean maybeReinforcementDamaged(Block block) {
        AccessDelegate delegate = AccessDelegate.getDelegate(block);
        IReinforcement reinforcement = delegate.getReinforcement();
        return reinforcement != null && reinforcementDamaged(reinforcement);
    }

    public static int timeUntilMature(IReinforcement reinforcement) {
        // Doesn't explicitly save the updated Maturation time into the cache.
        //  That's the responsibility of the caller.
        if (reinforcement instanceof PlayerReinforcement) {
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
            return maturationTime;
        }
        return 0;
    }

    public static boolean reinforcementDamaged(IReinforcement reinforcement) {
        int durability = reinforcement.getDurability();
        int durabilityLoss = 1;
        if (reinforcement instanceof PlayerReinforcement && Citadel.getConfigManager().maturationEnabled()) {
          final int maturationTime = timeUntilMature(reinforcement);
          if (maturationTime > 0) {
              durabilityLoss = maturationTime / 60 + 1;
              int blockType = reinforcement.getBlock().getTypeId();
              if (PlayerReinforcement.MATERIAL_SCALING.containsKey(blockType)) {
                  final double scale = PlayerReinforcement.MATERIAL_SCALING.get(blockType);
                  durabilityLoss = (int)((double)durabilityLoss * scale);
                  if (durabilityLoss < 0) {
                      durabilityLoss = 1;
                  }
              }
          }
          if (durability < durabilityLoss) {
              durabilityLoss = durability;
          }
        }
        durability -= durabilityLoss;
        reinforcement.setDurability(durability);
        boolean cancelled = durability > 0;
        if (durability <= 0) {
            cancelled = reinforcementBroken(reinforcement);
        } else {
            if (reinforcement instanceof PlayerReinforcement) {
                Citadel.verbose(
                    VerboseMsg.ReinDmg,
                    reinforcement.getBlock().getLocation().toString());
            }
            Citadel.getReinforcementManager().addReinforcement(reinforcement);
        }
        return cancelled;
    }

    public static boolean reinforcementBroken(IReinforcement reinforcement) {
        Citadel.getReinforcementManager().removeReinforcement(reinforcement);
        if (reinforcement instanceof PlayerReinforcement) {
            PlayerReinforcement pr = (PlayerReinforcement)reinforcement;
            Citadel.verbose(VerboseMsg.ReinDestroyed, pr.getBlock().getLocation().toString());
            if (rng.nextDouble() <= pr.getHealth()) {
                Location location = pr.getBlock().getLocation();
    	        ReinforcementMaterial material = pr.getMaterial();
                location.getWorld().dropItem(location, material.getRequiredMaterials());
            }
            return pr.isSecurable();
        }
        return false;  // implicit isSecureable() == false
    }

    public static SecurityLevel getSecurityLevel(String[] args, Player player) {
        if (args.length > 0) {
            try {
                return SecurityLevel.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                sendMessage(player, ChatColor.RED, "Invalid access level %s", args[0]);
                return null;
            }
        }
        return SecurityLevel.PRIVATE;
    }
    
    private static List<PlacementMode> MULTI_MODE = Arrays.asList(PlacementMode.FORTIFICATION, PlacementMode.INFO, PlacementMode.REINFORCEMENT, PlacementMode.INSECURE);

    public static void setMultiMode(PlacementMode mode, SecurityLevel securityLevel, String[] args, Player player, PlayerState state) {
        if (!MULTI_MODE.contains(mode)) return;
        Faction group = state.getFaction();
        if (group != null && group.isDisciplined()) {
            sendMessage(player, ChatColor.RED, Faction.kDisciplineMsg);
            return;
        }
        if (state.getMode() == mode && state.getSecurityLevel() == securityLevel) {
            state.reset();
            sendMessage(player, ChatColor.GREEN, "%s mode off", mode.name());
        } else {
            state.setMode(mode);
            state.setSecurityLevel(securityLevel);
            switch (mode) {
                case REINFORCEMENT:
                    sendMessage(player, ChatColor.GREEN, "%s mode %s", mode.name(), securityLevel.name());
                    break;
                case FORTIFICATION:
                    sendMessage(player, ChatColor.GREEN, "%s mode %s, %s", mode.name(), state.getReinforcementMaterial().getMaterial().name(), securityLevel.name());
                    break;
                case INFO:
                case INSECURE:
                    sendMessage(player, ChatColor.GREEN, "%s mode on", mode.name());
                    break;
            }
            state.checkResetMode();
        }
    }
    
    public static void setSingleMode(SecurityLevel securityLevel, PlayerState state, Player player) {
        Faction group = state.getFaction();
        if (group != null && group.isDisciplined()) {
            sendMessage(player, ChatColor.RED, Faction.kDisciplineMsg);
            return;
        }
        if (state.getMode() != PlacementMode.REINFORCEMENT_SINGLE_BLOCK) {
            state.setSecurityLevel(securityLevel);
            state.setMode(PlacementMode.REINFORCEMENT_SINGLE_BLOCK);
            sendMessage(player, ChatColor.GREEN, "Single block reinforcement mode %s", securityLevel.name() + ".");
        }
    }
    
    public static String getTruncatedMaterialMessage(String prefix, List<Integer> materials) {
	    StringBuilder builder = new StringBuilder();
	    for (int materialId : materials) {
	        if (builder.length() > 0) builder.append(", ");
	        builder.append(Material.getMaterial(materialId).name());
	    }
	    builder.insert(0, prefix);
	    return builder.toString();
	}

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

    public static Block findPlantSoil(Block plant) {
        final Set<Material> soilTypes = getPlantSoilTypes(plant.getType());
        if (soilTypes.size() <= 0) {
            return null;
        }
        return findSoilBelow(plant, soilTypes);
    }

    public static boolean isSoilPlant(Material mat) {
        return mat.equals(Material.WHEAT)
            || mat.equals(Material.MELON_STEM)
            || mat.equals(Material.PUMPKIN_STEM)
            || mat.equals(Material.CARROT)
            || mat.equals(Material.POTATO)
            || mat.equals(Material.CROPS)
            || mat.equals(Material.MELON_BLOCK)
            || mat.equals(Material.PUMPKIN);
    }

    public static boolean isDirtPlant(Material mat) {
        return mat.equals(Material.SUGAR_CANE_BLOCK)
            || mat.equals(Material.MELON_BLOCK)
            || mat.equals(Material.PUMPKIN);
    }

    public static boolean isGrassPlant(Material mat) {
        return mat.equals(Material.SUGAR_CANE_BLOCK)
            || mat.equals(Material.MELON_BLOCK)
            || mat.equals(Material.PUMPKIN);
    }

    public static boolean isSandPlant(Material mat) {
        return mat.equals(Material.CACTUS)
            || mat.equals(Material.SUGAR_CANE_BLOCK);
    }

    public static boolean isSoulSandPlant(Material mat) {
        return mat.equals(Material.NETHER_WARTS);
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

    public static boolean isReinforceablePlant(Material mat) {
        // If this list changes, update wouldPlantDoubleReinforce to account
        // for the new soil types.
        return mat.equals(Material.MELON_BLOCK)
            || mat.equals(Material.PUMPKIN);
    }

    public static int maxPlantHeight(Block plant) {
        switch(plant.getType()) {
            case CACTUS:
            case SUGAR_CANE_BLOCK:
                return 3;
            default:
                return 1;
        }
    }

    public static Block findSoilBelow(Block plant, Set<Material> desiredTypes) {
        Block down = plant;
        int max_depth = maxPlantHeight(plant);
        for (int i = 0; i < max_depth; ++i) {
            down = down.getRelative(BlockFace.DOWN);
            if (desiredTypes.contains(down.getType())) {
                return down;
            }
        }
        return null;
    }

    public static boolean isRail(Block block) {
        return isRail(block.getType());
    }

    public static boolean isRail(Material mat) {
        return mat.equals(Material.RAILS)
            || mat.equals(Material.POWERED_RAIL)
            || mat.equals(Material.ACTIVATOR_RAIL)
            || mat.equals(Material.DETECTOR_RAIL);
    }

    public static boolean wouldPlantDoubleReinforce(final Block block) {
        final Material blockMat = block.getType();
        if (isReinforceablePlant(blockMat)
            && AccessDelegate.getDelegate(block).getReinforcement() != null) {
            return true;
        }
        final Block above = block.getRelative(BlockFace.UP);
        final Material aboveMat = above.getType();
        if (isReinforceablePlant(aboveMat)) {
            final Set<Material> soilTypes = getPlantSoilTypes(aboveMat);
            if (soilTypes.contains(blockMat)
                    && Citadel.getReinforcementManager().getReinforcement(above) != null) {
              return true;
            }
        }
        return false;
    }

    // Given either a UUID or name, return the corresponding player's
    //  account ID or null.
    public static UUID toAccountId(String uuidOrName) {
        // See if it is a player UUID
        try {
            UUID id = UUID.fromString(uuidOrName);
            if (Citadel.getAccountIdManager().getPlayerName(id) != null) {
                return id;
            }
        } catch (Exception ex) {}
        // Maybe it's a name
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuidOrName);
        if (player != null && player.hasPlayedBefore()) { // Some reason getting a player even if they don't exist...  This is better anyways
            return player.getUniqueId();
        }
        return null;
    }
}
