package vg.civcraft.mc.civchat2.command.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.command.ChatCommand;
import vg.civcraft.mc.namelayer.NameAPI;

public class IgnoreList extends ChatCommand {
	
	public IgnoreList(String name) {
		super(name);
		setIdentifier("ignorelist");
		setDescription("Lists the players & groups you are ignoring");
		setUsage("/ignorelist");
		setArguments(0,0);
		setSenderMustBePlayer(true);
		setErrorOnTooManyArgs(false);
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		
		List<UUID> players = DBM.getIgnoredPlayers(me().getUniqueId());
		List<String> groups = DBM.getIgnoredGroups(me().getUniqueId());
		
		if(players == null){
			//no players ignored
			msg(ChatStrings.chatNotIgnoringAnyPlayers);
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("<i>Ignored Players: \n<n>");
			for(UUID playerUUID : players){
				String playerName = NameAPI.getCurrentName(playerUUID);
				if(playerName != null){
					sb.append(playerName);
					sb.append(", ");
				}
			}
			msg(sb.toString());
		}
		
		if(groups == null) {
			//no players ignored
			msg(ChatStrings.chatNotIgnoringAnyGroups);
			return true;
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("<i>Ignored Groups: \n<n>");
			for(String s : groups){
				sb.append(s);
				sb.append(", ");
			}
			msg(sb.toString());
			return true;
		}
	}
}
