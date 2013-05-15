package com.untamedears.citadel.listener;

import static com.untamedears.citadel.Utility.createPlayerReinforcement;
import static com.untamedears.citadel.Utility.isReinforced;
import static com.untamedears.citadel.Utility.reinforcementBroken;
import static com.untamedears.citadel.Utility.sendMessage;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.material.Openable;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.GroupManager;
import com.untamedears.citadel.MemberManager;
import com.untamedears.citadel.PersonalGroupManager;
import com.untamedears.citadel.PlacementMode;
import com.untamedears.citadel.SecurityLevel;
import com.untamedears.citadel.access.AccessDelegate;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.Member;
import com.untamedears.citadel.entity.PlayerState;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;
import com.untamedears.citadel.events.CreateReinforcementEvent;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/21/12
 * Time: 9:57 PM
 * 
 * Last edited by JonnyD
 * 7/18/12
 */
public class PlayerListener implements Listener {

    @EventHandler
    public void login(PlayerLoginEvent ple) {
        MemberManager memberManager = Citadel.getMemberManager();
        memberManager.addOnlinePlayer(ple.getPlayer());

        String playerName = ple.getPlayer().getName();
        Member member = memberManager.getMember(playerName);
        if(member == null){
            member = new Member(playerName);
            memberManager.addMember(member);
        }

        PersonalGroupManager personalGroupManager = Citadel.getPersonalGroupManager();
        boolean hasPersonalGroup = personalGroupManager.hasPersonalGroup(playerName);
        GroupManager groupManager = Citadel.getGroupManager();
        if(!hasPersonalGroup){
            String groupName = playerName;
            int i = 1;
            while(groupManager.isGroup(groupName)){
                groupName = playerName + i;
                i++;
            }
            Faction group = new Faction(groupName, playerName);
            groupManager.addGroup(group);
            personalGroupManager.addPersonalGroup(groupName, playerName);
        } else if(hasPersonalGroup){
            String personalGroupName = personalGroupManager.getPersonalGroup(playerName).getGroupName();
            if(!groupManager.isGroup(personalGroupName)){
                Faction group = new Faction(personalGroupName, playerName);
                groupManager.addGroup(group);
            }
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent pqe) {
        Player player = pqe.getPlayer();
        MemberManager memberManager = Citadel.getMemberManager();
        memberManager.removeOnlinePlayer(player);
        PlayerState.remove(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void bookshelf(PlayerInteractEvent pie) {
        if (pie.hasBlock() && pie.getMaterial() == Material.BOOKSHELF)
            interact(pie);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void bucketEmpty(PlayerBucketEmptyEvent pbee) {
        Material bucket = pbee.getBucket();
        if (Material.LAVA_BUCKET == bucket || Material.WATER_BUCKET == bucket) {
            Block block = pbee.getBlockClicked();
            BlockFace face = pbee.getBlockFace();
            Block relativeBlock = block.getRelative(face);
            // Protection for reinforced rails types from direct lava bucket drop.
            Material relativeType = relativeBlock.getType();
            if (Material.RAILS == relativeType || Material.POWERED_RAIL == relativeType ||
                    Material.DETECTOR_RAIL == relativeType) {
                if (isReinforced(relativeBlock)) {
                   pbee.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void interact(PlayerInteractEvent pie) {
        try {
        if (!pie.hasBlock()) return;

        Player player = pie.getPlayer();
        Block block = pie.getClickedBlock();

        AccessDelegate accessDelegate = AccessDelegate.getDelegate(block);
        block = accessDelegate.getBlock();
        IReinforcement generic_reinforcement = accessDelegate.getReinforcement();
        PlayerReinforcement reinforcement = null;
        if (generic_reinforcement instanceof PlayerReinforcement) {
            reinforcement = (PlayerReinforcement)generic_reinforcement;
        }

        Action action = pie.getAction();
        boolean access_reinforcement = 
            action == Action.RIGHT_CLICK_BLOCK
            && reinforcement != null
            && reinforcement.isSecurable();
        boolean normal_access_denied =
            reinforcement != null
            && !reinforcement.isAccessible(player);
        boolean admin_can_access = player.hasPermission("citadel.admin.accesssecurable");
        if (access_reinforcement && normal_access_denied && !admin_can_access) {
            Citadel.info(String.format(
                "%s failed to access locked reinforcement at %s",
                player.getName(), block.getLocation().toString()));
            sendMessage(pie.getPlayer(), ChatColor.RED, "%s is locked", block.getType().name());
            pie.setCancelled(true);
        }
        if (pie.isCancelled()) return;

        PlayerState state = PlayerState.get(player);
        PlacementMode placementMode = state.getMode();
        switch (placementMode) {
            case NORMAL:
                if (access_reinforcement && normal_access_denied && admin_can_access) {
                    Citadel.info(String.format(
                        "[Admin] %s accessed locked reinforcement at %s",
                        player.getName(), block.getLocation().toString()));
                }
                return;
            case FORTIFICATION:
                return;
            case INFO:
                // did player click on a reinforced block?
                if (reinforcement != null) {
                    String reinforcementStatus = reinforcement.getStatus();
                    SecurityLevel securityLevel = reinforcement.getSecurityLevel();
                    Faction group = reinforcement.getOwner();
                    String message;
                    if (player.hasPermission("citadel.admin.ctinfodetails")) {
                        message = String.format("Loc[%s]  Chunk[%s]", 
                            reinforcement.getId().toString(),
                            reinforcement.getChunkId());
                        sendMessage(player, ChatColor.GREEN, message);
                        String groupName = "!NULL!";
                        if (group != null) {
                            if (group.isPersonalGroup()) {
                                groupName = String.format("[%s] (Personal)", group.getName());
                            } else {
                                groupName = String.format("[%s]", group.getName());
                            }
                        }
                        message = String.format(" Group%s  Durability[%d/%d]",
                            groupName,
                            reinforcement.getDurability(),
                            reinforcement.getMaterial().getStrength());
                        sendMessage(player, ChatColor.GREEN, message);
                    } else if(reinforcement.isAccessible(player)){
                        boolean is_personal_group = false;
                        String groupName = "!NULL!";
                        if (group != null) {
                            groupName = group.getName();
                            is_personal_group = group.isPersonalGroup();
                        }
                        if(is_personal_group){
                            message = String.format("%s, security: %s, group: %s (Default Group)", reinforcementStatus, securityLevel, groupName);
                        } else {
                            message = String.format("%s, security: %s, group: %s", reinforcementStatus, securityLevel, groupName);
                        }
                        sendMessage(player, ChatColor.GREEN, message);
                    } else {
                        sendMessage(player, ChatColor.RED, "%s, security: %s", reinforcementStatus, securityLevel);
                    }
                    if (player.getGameMode() == GameMode.CREATIVE) {
                        pie.setCancelled(true);
                    }
                }
                break;

            default:
                // player is in reinforcement mode
                if (reinforcement == null) {
                    // Break any natural reinforcement before placing the player reinforcement
                    if (generic_reinforcement != null) {
                        reinforcementBroken(generic_reinforcement);
                    }
                    createPlayerReinforcement(player, block);
                } else if (reinforcement.isBypassable(player)) {
                    boolean update = false;
                    String message = "";
                    if (reinforcement.getSecurityLevel() != state.getSecurityLevel()){
                        reinforcement.setSecurityLevel(state.getSecurityLevel());
                        update = true;
                        message = String.format("Changed security level %s", reinforcement.getSecurityLevel().name());
                    }
                    Faction group = state.getFaction();
                    if(!reinforcement.getOwner().equals(group)) {
                        reinforcement.setOwner(group);
                        update = true;
                        if(!message.equals("")){
                            message = message + ". ";
                        }
                        if(reinforcement.getSecurityLevel() != SecurityLevel.PRIVATE){
                            message = message + String.format("Changed group to %s", group.getName());
                        }
                    }
                    if(update){
                        CreateReinforcementEvent event = new CreateReinforcementEvent(reinforcement, block, player, true);
                        Citadel.getStaticServer().getPluginManager().callEvent(event);
                        if (!event.isCancelled()) {
                            Citadel.getReinforcementManager().addReinforcement(reinforcement);
                            sendMessage(player, ChatColor.GREEN, message);
                        }
                    }
                } else {
                    sendMessage(player, ChatColor.RED, "You are not permitted to modify this reinforcement");
                }
                pie.setCancelled(true);
                if (state.getMode() == PlacementMode.REINFORCEMENT_SINGLE_BLOCK) {
                    state.reset();
                } else {
                    state.checkResetMode();
                }
        }

        }
        catch(Exception e)
        {
            Citadel.printStackTrace(e);
        }
    }
}
