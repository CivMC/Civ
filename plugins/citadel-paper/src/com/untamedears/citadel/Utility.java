package com.untamedears.citadel;

import com.untamedears.citadel.access.AccessDelegate;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.PlayerState;
import com.untamedears.citadel.entity.Reinforcement;
import com.untamedears.citadel.entity.ReinforcementMaterial;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/23/12
 * Time: 3:37 PM
 */
public class Utility {

    private static Map<SecurityLevel, MaterialData> securityMaterial;
    private static Random rng = new Random();
    private static Citadel plugin;
    
    static {
        securityMaterial = new HashMap<SecurityLevel, MaterialData>();
        securityMaterial.put(SecurityLevel.PUBLIC, new Wool(DyeColor.GREEN));
        securityMaterial.put(SecurityLevel.GROUP, new Wool(DyeColor.YELLOW));
        securityMaterial.put(SecurityLevel.PRIVATE, new Wool(DyeColor.RED));
    }

    private static Citadel getPlugin() {
        if (plugin == null)
            plugin = Citadel.getInstance();
        return plugin;
    }

    public static Block getAttachedChest(Block block) {
        if (block.getType() == Material.CHEST)
            for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
                Block b = block.getRelative(face);
                if (b.getType() == Material.CHEST) {
                    return b;
                }
            }
        return null;
    }

    public static Reinforcement createReinforcement(Player player, Block block) {
        if (Reinforcement.NON_REINFORCEABLE.contains(block.getTypeId())) return null;

        PlayerState state = PlayerState.get(player);
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
                player.getInventory().removeItem(new ItemStack(block.getType(), 1));
                break;
            default:
                return null;
        }

        if (player.getInventory().contains(material.getMaterial(), material.getRequirements())) {
            Faction owner = getPlugin().dao.findGroupByName(player.getDisplayName());
            player.getInventory().removeItem(material.getRequiredMaterials());
            //TODO: there will eventually be a better way to flush inventory changes to the client
            player.updateInventory();
            Reinforcement reinforcement = new Reinforcement(block, material, owner, state.getSecurityLevel());
            getPlugin().dao.save(reinforcement);
            sendThrottledMessage(player, ChatColor.GREEN, "Reinforced with %s at security level %s", material.getMaterial().name(), state.getSecurityLevel().name());
            // TODO: enable chained flashers, they're pretty cool
            //new BlockFlasher(block, material.getFlasher()).start(getPlugin());
            //new BlockFlasher(block, material.getFlasher()).chain(securityMaterial.get(state.getSecurityLevel())).start();
            return reinforcement;
        } else {
            return null;
        }
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

    public static boolean maybeReinforcementDamaged(Block block) {
        AccessDelegate delegate = AccessDelegate.getDelegate(block);
        Reinforcement reinforcement = delegate.getReinforcement();
        return reinforcement != null && reinforcementDamaged(reinforcement);
    }

    public static boolean reinforcementDamaged(Reinforcement reinforcement) {
        reinforcement.setDurability(reinforcement.getDurability() - 1);
        boolean cancelled = reinforcement.getDurability() > 0;
        if (reinforcement.getDurability() <= 0) {
            cancelled = reinforcementBroken(reinforcement);
        } else {
            getPlugin().logVerbose("Reinforcement damaged %s", reinforcement);

            getPlugin().dao.save(reinforcement);
        }
        return cancelled;
    }

    public static boolean reinforcementBroken(Reinforcement reinforcement) {
        getPlugin().logVerbose("Reinforcement %s destroyed", reinforcement);

        getPlugin().dao.delete(reinforcement);
        if (rng.nextDouble() <= reinforcement.getHealth()) {
            Location location = reinforcement.getBlock().getLocation();
            location.getWorld().dropItem(location, reinforcement.getMaterial().getRequiredMaterials());
        }
        return reinforcement.isSecurable();
    }
}
