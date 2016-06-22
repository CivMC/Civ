package com.github.maxopoly.finale.external;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.listeners.PearlCoolDownListener;

public class ProtocolLibManager {
	
	private ProtocolManager protocolManager;
	//no clue how to make this, so we snatch it from a normal packet
	private Object pearlModifierObjectExample;

	public ProtocolLibManager() {
		this.protocolManager = ProtocolLibrary.getProtocolManager();
		registerPacketListener();
	}

	private void registerPacketListener() {
		//pearl cooldown
		protocolManager.addPacketListener(new PacketAdapter(Finale.getPlugin(),
				ListenerPriority.NORMAL, PacketType.Play.Server.SET_COOLDOWN) {
			@Override
			public void onPacketSending(PacketEvent event) {
				if (event.getPacketType() != PacketType.Play.Server.SET_COOLDOWN) {
					return;
				}
				Player p = event.getPlayer();
				PacketContainer packet = event.getPacket();
				//ensure we are dealing with pearl cooldowns
				if (packet.getModifier().read(0).getClass().getSimpleName().equals("ItemEnderPearl")) {
					if (pearlModifierObjectExample == null) {
						pearlModifierObjectExample = packet.getModifier().read(0);
					}
					if (PearlCoolDownListener.getPearlCoolDown(p.getUniqueId()) != packet.getIntegers().read(0)) {
						//not our cooldown, so fuck it
						event.setCancelled(true);
					}
				}
			}
		});
	}
	
	public void sendPacketWithCoolDown(long coolDown, Player p) {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.SET_COOLDOWN);
		packet.getIntegers().write(0, (int) coolDown);
		packet.getModifier().write(0, pearlModifierObjectExample);
		try {
		    protocolManager.sendServerPacket(p, packet);
		} catch (InvocationTargetException e) {
			Finale.getPlugin().warning("Failed to send pearl cd package to " + p.getName());
			e.printStackTrace();
		}
	}

}
