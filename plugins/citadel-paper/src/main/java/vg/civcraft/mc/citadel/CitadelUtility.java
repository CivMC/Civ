package vg.civcraft.mc.citadel;

import java.util.logging.Level;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.citadel.acidtypes.AcidType;
import vg.civcraft.mc.citadel.model.AcidManager;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

/**
 * Just a useful class with general and misplaced methods that can be called
 * from anywhere.
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
            case PINK_PETALS:
            case ACACIA_SAPLING:
            case BIRCH_SAPLING:
            case DARK_OAK_SAPLING:
            case JUNGLE_SAPLING:
            case OAK_SAPLING:
            case SPRUCE_SAPLING:
            case FLOWERING_AZALEA:
            case CHERRY_SAPLING:
            case MANGROVE_PROPAGULE:
            case PALE_OAK_SAPLING:
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
            case ROSE_BUSH:
            case TORCHFLOWER:
            case PITCHER_PLANT:
            case TALL_GRASS:
            case SHORT_GRASS:
            case TWISTING_VINES:
            case TWISTING_VINES_PLANT:
            case WEEPING_VINES:
            case WEEPING_VINES_PLANT:
            case CRIMSON_FUNGUS:
            case CRIMSON_ROOTS:
            case WARPED_FUNGUS:
            case WARPED_ROOTS:
            case NETHER_SPROUTS:
            case KELP:
            case KELP_PLANT:
            case SPORE_BLOSSOM:
            case BIG_DRIPLEAF:
            case BIG_DRIPLEAF_STEM:
            case CAVE_VINES:
            case CAVE_VINES_PLANT:
            case SMALL_DRIPLEAF:
            case SWEET_BERRY_BUSH:
            case BAMBOO:
            case BAMBOO_SAPLING:
            case SEA_PICKLE:
            case POINTED_DRIPSTONE:
                return true;
            default:
                return false;
        }
    }

    public static void sendAndLog(CommandSender receiver, ChatColor color, String message) {
        receiver.sendMessage(color + message);
        if (Citadel.getInstance().getConfigManager().logMessages()) {
            Citadel.getInstance().getLogger().log(Level.INFO, "Sent {0} reply {1}",
                new Object[]{receiver.getName(), message});
        }
    }

    public static void sendAndLog(CommandSender receiver, ChatColor color, String message, Location location) {
        final TextComponent component = new TextComponent(color + message);
        addCoordsHoverComponent(component, location);
        receiver.spigot().sendMessage(component);
        if (Citadel.getInstance().getConfigManager().logMessages()) {
            Citadel.getInstance().getLogger().log(Level.INFO, "Sent {0} reply {1}",
                new Object[]{receiver.getName(), message});
        }
    }

    private static BaseComponent addCoordsHoverComponent(BaseComponent component, Location location) {
        String hoverText = String.format("Location: %s %s %s",
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ());
        component.setHoverEvent(new HoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            new Text(hoverText)));
        return component;
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
                "The group " + group.getName() + " seems to have been deleted in the mean time",
                block.getLocation());
            Citadel.getInstance().getStateManager().setState(player, null);
            return true;
        }
        // check if player still has permission
        if (!NameAPI.getGroupManager().hasAccess(group, player.getUniqueId(),
            CitadelPermissionHandler.getReinforce())) {
            CitadelUtility.sendAndLog(player, ChatColor.RED,
                "You seem to have lost permission to reinforce on " + group.getName(),
                block.getLocation());
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
            CitadelUtility.sendAndLog(player, ChatColor.RED,
                type.getName() + " can not reinforce " + block.getType(),
                block.getLocation());
            return false;
        }
        // check if reinforcement is allowed in the current world
        if (!type.isAllowedInWorld(block.getWorld().getName())) {
            CitadelUtility.sendAndLog(player, ChatColor.RED,
                type.getName() + " can not reinforce in this dimension", block.getLocation());
            return true;
        }
        ItemMap playerItems = new ItemMap(player.getInventory());
        // check inventory
        int available = playerItems.getAmount(type.getItem());
        int required = type.getItem().getAmount();
        // special handling if the reinforcement material is also the block being placed
        // consuming the reinforcement material once will count the block being placed as reinforcing itself
        // so detect that and consume an extra one
        boolean consumeExtra = false;
        //We do this over a material check to keep lore/name support intact etc
        ItemStack offfhandStack = player.getInventory().getItemInOffHand().clone();
        offfhandStack.setAmount(1);

        boolean blockIsAlsoReinforcement = block.getType() == type.getItem().getType();
        if (blockIsAlsoReinforcement) {
            boolean placingFromReinforcementStack = (player.getInventory().getHeldItemSlot() == player.getInventory().first(block.getType())) ||
                (offfhandStack.equals(type.getItem()));
            if (placingFromReinforcementStack) {
                consumeExtra = true;
                required++;
            }
        }
        if (available < required) {
            Citadel.getInstance().getStateManager().setState(player, null);
            CitadelUtility.sendAndLog(player, ChatColor.RED,
                "You have no items left to reinforce with " + type.getName(),
                block.getLocation());
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
        // warn if acid cannot complete
        if (Citadel.getInstance().getAcidManager().isPossibleAcidBlock(block)) {
            AcidManager acidMan = Citadel.getInstance().getAcidManager();
            AcidType acidType = acidMan.getAcidTypeFromMaterial(block.getType());
            for (BlockFace blockFace : acidType.blockFaces()) {
                Block relativeBlock = block.getRelative(blockFace);
                Reinforcement relativeReinforcement = ReinforcementLogic.getReinforcementProtecting(relativeBlock);
                if (
                    relativeReinforcement == null
                        || !relativeReinforcement.getType().canBeReinforced(relativeBlock.getType())
                        || (acidMan.isPossibleAcidBlock(relativeBlock) && acidMan.isAcidOnSameGroup(newRein, relativeReinforcement))
                ) {
                    continue;
                }
                if (!acidMan.canAcidBlock(type, relativeReinforcement.getType())) {
                    CitadelUtility.sendAndLog(player, ChatColor.RED,
                        "The " + blockFace.toString() + " acid will fail as it cannot acid tougher reinforcements!");
                }
            }
        }
        ReinforcementLogic.createReinforcement(newRein);
        return false;
    }
}
