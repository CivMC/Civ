package com.github.maxopoly.KiraBukkitGateway.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;

import vg.civcraft.mc.civchat2.event.GroupChatEvent;

public class CivChatListener implements Listener {

	@EventHandler
	public void chat(GroupChatEvent e) {
		KiraBukkitGatewayPlugin.getInstance().getRabbit().sendGroupChatMessage(e.getGroup(), e.getPlayer().getName(),
				e.getMessage());
	}

}
