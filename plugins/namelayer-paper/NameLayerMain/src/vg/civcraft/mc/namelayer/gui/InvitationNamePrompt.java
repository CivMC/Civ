package vg.civcraft.mc.namelayer.gui;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.NameAPI;

public class InvitationNamePrompt extends StringPrompt {
	
	private static Set <UUID> typingPlayers;
	
	private String rank;
	public InvitationNamePrompt(Player p, String niceRankName) {
		this.rank = niceRankName;
		if (typingPlayers == null) {
			typingPlayers = new HashSet<UUID>();
		}
		typingPlayers.add(p.getUniqueId());
	}
	
	@Override
	public Prompt acceptInput(ConversationContext con, String answer) {
		Player p = (Player) con.getForWhom();
		UUID invitedId = NameAPI.getUUID(answer);
		if (invitedId == null) {
			p.sendMessage(ChatColor.RED + "This player does not exist");
		}
		
		
		
		
		return Prompt.END_OF_CONVERSATION;
	}
	
	@Override
	public String getPromptText(ConversationContext con) {
		return ChatColor.LIGHT_PURPLE + "Enter the name of the player you want to invite as " + rank;
	}

}
