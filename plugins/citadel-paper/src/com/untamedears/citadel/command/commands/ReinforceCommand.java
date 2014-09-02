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

/**
 * User: JonnyD
 * Date: 7/18/12
 * Time: 11:57 PM
 */
public class ReinforceCommand extends PlayerCommand {

	public ReinforceCommand() {
		super("Reinforce Mode");
		setDescription("Toggles reinforce mode");
		setUsage("/ctreinforce §8[security-level] [group-name]");
		setArgumentRange(0, 2);
		setIdentifiers(new String[] {"ctreinforce", "ctr"});
	}

	public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
			sender.sendMessage("Console curently isn't supported");
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
		if(secLevel != null && secLevel.equalsIgnoreCase("group")){
			if(groupName == null || groupName.isEmpty() || groupName.equals("")){
				sender.sendMessage(new StringBuilder().append("§cYou must specify a group in group reinforce mode").toString());
				sender.sendMessage(new StringBuilder().append("§cUsage:§e ").append("/ctreinforce §8group <group-name>").toString());
				return true;
			}
			GroupManager groupManager = Citadel.getGroupManager();
			Faction group = groupManager.getGroup(groupName);
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
		
		SecurityLevel securityLevel = getSecurityLevel(args, player);
        if (securityLevel == null) return false;
        
        setMultiMode(PlacementMode.REINFORCEMENT, securityLevel, args, player, state);
        return true;
	}

}
