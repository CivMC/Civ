package com.untamedears.citadel.listener;

import static com.untamedears.citadel.Utility.createReinforcement;
import static com.untamedears.citadel.Utility.sendMessage;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
import com.untamedears.citadel.entity.Reinforcement;

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

    	String playerName = ple.getPlayer().getDisplayName();
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void interact(PlayerInteractEvent pie) {
        if (!pie.hasBlock()) return;

        Player player = pie.getPlayer();
        Block block = pie.getClickedBlock();

        AccessDelegate accessDelegate = AccessDelegate.getDelegate(block);
        block = accessDelegate.getBlock();
        Reinforcement reinforcement = accessDelegate.getReinforcement();
        
        if (reinforcement != null
                && reinforcement.isSecurable()
                && !reinforcement.isAccessible(pie.getPlayer())) {
            Block reinforcedBlock = reinforcement.getBlock();
            Citadel.info("%s failed to access locked reinforcement %s, " 
        			+ player.getDisplayName() + " at " 
        			+ reinforcedBlock.getLocation().toString());
            pie.setCancelled(true);
            pie.setUseInteractedBlock(Event.Result.DENY);
            sendMessage(player, ChatColor.RED, "%s is locked", block.getType().name());
            return;
        }
        if (pie.isCancelled()) return;

        PlayerState state = PlayerState.get(player);
        PlacementMode placementMode = state.getMode();
        switch (placementMode) {
            case NORMAL:
            	return;
            case FORTIFICATION:
                return;
            case INFO:
                // did player click on a reinforced block?
                if (reinforcement != null) {
                	String reinforcementStatus = reinforcement.getStatus();
                	SecurityLevel securityLevel = reinforcement.getSecurityLevel();
                    if(reinforcement.isAccessible(player)){
                    	Faction group = reinforcement.getOwner();
                    	if(group.isPersonalGroup()){
                    		sendMessage(player, ChatColor.GREEN, "%s, security: %s", reinforcementStatus, securityLevel);
                    	} else {
	                    	if(securityLevel == SecurityLevel.PRIVATE){
	                    		sendMessage(player, ChatColor.GREEN, "%s, security: %s", reinforcementStatus, securityLevel);
	                    	} else {
	                    		sendMessage(player, ChatColor.GREEN, "%s, security: %s, group: %s", reinforcementStatus, securityLevel, group.getName());
	                    	}
                    	}
                    } else {
                    	sendMessage(player, ChatColor.RED, "%s, security: %s", reinforcementStatus, securityLevel);
                    }
                }
                break;
            default:
                // player is in reinforcement mode
                if (reinforcement == null) {
                    createReinforcement(player, block);
                } else if (reinforcement.isBypassable(player)) {
                	boolean update = false;
                	String message = "";
                    if (reinforcement.getSecurityLevel() != state.getSecurityLevel()){
                        reinforcement.setSecurityLevel(state.getSecurityLevel());
                        update = true;
                        message = String.format("Changed security level %s", reinforcement.getSecurityLevel().name());
                    }
                   	if(!reinforcement.getOwner().equals(state.getFaction())) {
                        reinforcement.setOwner(state.getFaction());
                        update = true;
                        if(!message.equals("")){
                        	message = message + ". ";
                        }
                        if(reinforcement.getSecurityLevel() != SecurityLevel.PRIVATE){
                        	message = message + String.format("Changed group to %s", state.getFaction().getName());
                        }
                    }
                   	if(update){
                        Citadel.getReinforcementManager().addReinforcement(reinforcement);
                        sendMessage(player, ChatColor.GREEN, message);
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
}
