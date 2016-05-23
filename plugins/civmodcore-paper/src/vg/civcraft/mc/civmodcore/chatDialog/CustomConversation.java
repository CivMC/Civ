package vg.civcraft.mc.civmodcore.chatDialog;

import java.util.Map;

import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.Prompt;
import org.bukkit.plugin.Plugin;

public class CustomConversation extends Conversation {

	public CustomConversation(Plugin plugin, Conversable forWhom,
			Prompt firstPrompt) {
		super(plugin, forWhom, firstPrompt);
	}

	public CustomConversation(Plugin plugin, Conversable forWhom,
			Prompt firstPrompt, Map<Object, Object> initialSessionData) {
		super(plugin, forWhom, firstPrompt, initialSessionData);
	}

	// this isnt possible in the normal conversation class for some reason and
	// all bukkit conversations will be modal by default, which means the player
	// wont receive any chat messages at all while in a conversation
	public void setModal(boolean modal) {
		super.modal = modal;
	}

}
