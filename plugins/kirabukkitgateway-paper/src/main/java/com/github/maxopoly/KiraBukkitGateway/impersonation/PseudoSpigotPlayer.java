package com.github.maxopoly.KiraBukkitGateway.impersonation;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;

public class PseudoSpigotPlayer extends Player.Spigot {
	
	private PseudoPlayer player;
	
	public PseudoSpigotPlayer(PseudoPlayer player) {
		this.player = player;
	}
	
	@Override
	public void sendMessage(BaseComponent comp) {
		player.sendMessage(BaseComponent.toPlainText(comp));
	}
	
	@Override
	public void sendMessage(BaseComponent ... components) {
		player.sendMessage(BaseComponent.toPlainText(components));
	}
	
	@Override
	public void sendMessage(ChatMessageType position, BaseComponent comp) {
		player.sendMessage(BaseComponent.toPlainText(comp));
	}
	
	@Override
	public void sendMessage(ChatMessageType position, BaseComponent ... components) {
		player.sendMessage(BaseComponent.toPlainText(components));
	}
	

}
