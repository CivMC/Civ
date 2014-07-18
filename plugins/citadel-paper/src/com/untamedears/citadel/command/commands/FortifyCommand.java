package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.getSecurityLevel;
import static com.untamedears.citadel.Utility.sendMessage;
import static com.untamedears.citadel.Utility.setMultiMode;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.GroupManager;
import com.untamedears.citadel.PlacementMode;
import com.untamedears.citadel.SecurityLevel;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.PlayerState;
import com.untamedears.citadel.entity.ReinforcementMaterial;

/**
 * User: JonnyD
 * Date: 7/18/12
 * Time: 11:57 PM
 */
public class FortifyCommand extends PlayerCommand {

	public FortifyCommand() {
		super("Fority Mode");
		setDescription("Toggle fortification mode");
		setUsage("/ctfortify §8[security-level]");
		setArgumentRange(0, 2);
		setIdentifiers(new String[] {"ctfortify", "ctf"});
	}

	public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
			sender.sendMessage("Player only command");
			return true;
        }
		Player player = (Player) sender;
        UUID accountId = player.getUniqueId();
		PlayerState state = PlayerState.get(player);
		
		String secLevel = null;
		String groupName = null;
		if(args.length != 0){
			secLevel = args[0];
			if(args.length == 2){
				groupName = args[1];
			}
		}
                
                SecurityLevel securityLevel = getSecurityLevel(args, player);
                if ((secLevel == null || secLevel.isEmpty()) && state.getMode() == PlacementMode.FORTIFICATION) {
                    securityLevel = state.getSecurityLevel();
                }
                if (securityLevel == null) return false;
		
		if(securityLevel == SecurityLevel.GROUP){
                        Faction group;
                        if(!(groupName == null) && !(groupName.isEmpty()) && !(groupName.equals(""))){
                            GroupManager groupManager = Citadel.getGroupManager();
                            group = groupManager.getGroup(groupName);
                        } else if (state.getMode() == PlacementMode.FORTIFICATION && state.getSecurityLevel() == SecurityLevel.GROUP) {
                            /* Default to current faction */
                            group = state.getFaction();
                        } else {
                            sender.sendMessage(new StringBuilder().append("§cYou must specify a group in group fortification mode").toString());
                            sender.sendMessage(new StringBuilder().append("§cUsage:§e ").append("/ctfortify §8group <group-name>").toString());
                            return true;
                        }
			if(group == null){
				sendMessage(sender, ChatColor.RED, "Group doesn't exist");
				return true;
			}
			if (group.isDisciplined()) {
				sendMessage(sender, ChatColor.RED, Faction.kDisciplineMsg);
				return true;
			}
			if(!group.isFounder(accountId) && !group.isModerator(accountId)){
				sendMessage(sender, ChatColor.RED, "Invalid permission to use this group");
				return true;
			}
			if(group.isPersonalGroup()){
				sendMessage(sender, ChatColor.RED, "You cannot share your default group");
				return true;
			}
			state.setFaction(group);
		} else {
			state.setFaction(Faction.getPersonalGroup(accountId));
		}

        ReinforcementMaterial material = ReinforcementMaterial.get(player.getItemInHand().getType());
        if (state.getMode() == PlacementMode.FORTIFICATION) {
            // Only change material if a valid reinforcement material in hand and not current reinforcement
            if (material != null && material != state.getReinforcementMaterial()) {
                // Switch reinforcement materials without turning off and on again
                state.reset();
                state.setFortificationMaterial(material);
            }
            setMultiMode(PlacementMode.FORTIFICATION, securityLevel, args, player, state);
        } else {
            if (material == null) {
                sendMessage(sender, ChatColor.YELLOW, "Invalid reinforcement material %s", player.getItemInHand().getType().name());
            } else {
                state.setFortificationMaterial(material);
                setMultiMode(PlacementMode.FORTIFICATION, securityLevel, args, player, state);
            }
        }
        
        return true;
	}

}
