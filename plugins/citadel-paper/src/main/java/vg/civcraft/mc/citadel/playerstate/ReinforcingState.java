package vg.civcraft.mc.citadel.playerstate;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelPermissionHandler;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.events.ReinforcementChangeTypeEvent;
import vg.civcraft.mc.citadel.events.ReinforcementGroupChangeEvent;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.namelayer.NameLayerAPI;
import vg.civcraft.mc.namelayer.group.Group;

public class ReinforcingState extends AbstractPlayerState {

    private Group group;

    public ReinforcingState(Player p, Group group) {
        super(p);
        this.group = group;
    }

    public Group getGroup() {
        return group;
    }

    @Override
    public String getName() {
        return "Reinforcing mode on " + ChatColor.LIGHT_PURPLE + group.getName();
    }

    @Override
    public void handleBlockPlace(BlockPlaceEvent e) {
    }

    @Override
    public void handleInteractBlock(PlayerInteractEvent e) {
        // always cancel
        e.setCancelled(true);
        // does group still exist?
        if (!group.isValid()) {
            CitadelUtility.sendAndLog(e.getPlayer(), ChatColor.RED,
                "The group " + group.getName() + " seems to have been deleted in the mean time");
            Citadel.getInstance().getStateManager().setState(e.getPlayer(), null);
            return;
        }
        Player player = e.getPlayer();
        // does the player have permission to reinforce on that group
        if (!NameLayerAPI.getGroupManager().hasAccess(group, player.getUniqueId(),
            CitadelPermissionHandler.getReinforce())) {
            CitadelUtility.sendAndLog(player, ChatColor.RED,
                "You seem to have lost permission to reinforce on " + group.getName(),
                e.getClickedBlock().getLocation());
            Citadel.getInstance().getStateManager().setState(player, null);
            return;
        }
        // resolve reinforcement type from held item (may be null if not holding material)
        ReinforcementType type = null;
        if (e.getItem() != null) {
            type = Citadel.getInstance().getReinforcementTypeManager().getByItemStack(e.getItem(), player.getWorld().getName());
        }
        Block block = ReinforcementLogic.getResponsibleBlock(e.getClickedBlock());
        Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(block);
        // if there is no existing reinforcement and no material to create one, nothing to do
        // if reinforcement exists, check if player has permission to edit it
        if (rein != null && !rein.hasPermission(player, CitadelPermissionHandler.getBypass())) {
            CitadelUtility.sendAndLog(player, ChatColor.RED,
                "You do not have permission to bypass this reinforcement",
                e.getClickedBlock().getLocation());
            return;
        }
        // not holding reinforcement material: only allow group transfer on existing reinforcement
        if (type == null) {
            if (rein == null) {
                CitadelUtility.sendAndLog(player, ChatColor.RED,
                    "You need to hold a valid reinforcement material to reinforce a new block",
                    e.getClickedBlock().getLocation());
                return;
            }
            if (group.getGroupId() != rein.getGroup().getGroupId()) {
                ReinforcementGroupChangeEvent rgce = new ReinforcementGroupChangeEvent(player, rein, group);
                Bukkit.getPluginManager().callEvent(rgce);
                if (!rgce.isCancelled()) {
                    rein.setGroup(group);
                    CitadelUtility.sendAndLog(player, ChatColor.GREEN,
                        "Updated group to " + ChatColor.LIGHT_PURPLE + group.getName(),
                        e.getClickedBlock().getLocation());
                }
            } else {
                CitadelUtility.sendAndLog(player, ChatColor.YELLOW,
                    "This reinforcement is already on " + group.getName(),
                    e.getClickedBlock().getLocation());
            }
            return;
        }
        // holding reinforcement material: full reinforce/upgrade/transfer logic
        // can the item reinforce the clicked block
        if (!type.canBeReinforced(block.getType())) {
            CitadelUtility.sendAndLog(player, ChatColor.RED,
                type.getName() + " can not reinforce " + block.getType(),
                block.getLocation());
            return;
        }
        // is the reinforcement item allowed in the current world
        if (!type.isAllowedInWorld(block.getWorld().getName())) {
            CitadelUtility.sendAndLog(player, ChatColor.RED,
                type.getName() + " cannot reinforce in this dimension", block.getLocation());
            return;
        }
        Reinforcement newRein = null;
        if (rein == null || rein.getType() != type) {
            // check inventory for reinforcement item
            ItemMap toConsume = new ItemMap(type.getItem());
            if (!toConsume.isContainedIn(player.getInventory())) {
                CitadelUtility.sendAndLog(player, ChatColor.RED, "No reinforcing item found in your inventory?",
                    e.getClickedBlock().getLocation());
                return;
            }
            if (rein == null) {
                newRein = ReinforcementLogic.callReinforcementCreationEvent(player, block, type, group);
                if (newRein == null) {
                    //event was cancelled, error message is up to whoever cancelled it
                    return;
                }
            }
            // consume item from inventory
            if (!toConsume.removeSafelyFrom(player.getInventory())) {
                CitadelUtility.sendAndLog(player, ChatColor.RED,
                    "Failed to remove reinforcement item from your inventory",
                    e.getClickedBlock().getLocation());
                return;
            }
        }

        if (rein == null) {
            if (Citadel.getInstance().getConfigManager().logCreation()) {
                Citadel.getInstance().getLogger()
                    .info(player.getName() + " created reinforcement with " + type.getName() + " for "
                        + block.getType().toString() + " at "
                        + block.getLocation().toString());
            }
            // just create new reinforcement
            ReinforcementLogic.createReinforcement(newRein);
        } else {
            // replace existing one
            boolean changedGroup = false;
            if (group.getGroupId() != rein.getGroup().getGroupId()) {
                // switch group
                ReinforcementGroupChangeEvent rgce = new ReinforcementGroupChangeEvent(player, rein, group);
                Bukkit.getPluginManager().callEvent(rgce);
                if (!rgce.isCancelled()) {
                    rein.setGroup(group);
                    changedGroup = true;
                }
                // informing the user is up to whoever cancelled it
            }
            if (type != rein.getType()) {
                // switch type
                ReinforcementChangeTypeEvent rcte = new ReinforcementChangeTypeEvent(player, rein, type);
                Bukkit.getPluginManager().callEvent(rcte);
                if (!rcte.isCancelled()) {
                    if (rein.rollForItemReturn()) {
                        giveReinforcement(rein.getLocation().clone().add(0.5, 0.5, 0.5), player, rein.getType());
                    }
                    rein.setType(type);
                    rein.setHealth(type.getHealth());
                    rein.resetCreationTime();
                    CitadelUtility.sendAndLog(player, ChatColor.GREEN,
                        "Updated reinforcement to " + rein.getType().getName() + " on " + group.getName(),
                        e.getClickedBlock().getLocation());
                } else if (changedGroup) {
                    CitadelUtility.sendAndLog(player, ChatColor.GREEN,
                        "Updated group to " + ChatColor.LIGHT_PURPLE + group.getName(),
                        e.getClickedBlock().getLocation());
                }
            } else if (changedGroup) {
                CitadelUtility.sendAndLog(player, ChatColor.GREEN,
                    "Updated group to " + ChatColor.LIGHT_PURPLE + group.getName(),
                    e.getClickedBlock().getLocation());
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ReinforcingState)) {
            return false;
        }
        return ((ReinforcingState) o).group.getName().equals(this.getGroup().getName());
    }

    @Override
    public String getOverlayText() {
        return String.format("%sCTR %s%s", ChatColor.GOLD, ChatColor.LIGHT_PURPLE, group.getName());
    }
}
