package com.github.maxopoly.listeners;

import java.util.Map;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.github.maxopoly.Finale;

public class ProtocolLibPacketListener {

	private Map<String, Long> cooldowns;
	private ProtocolManager protocolManager;

	public ProtocolLibPacketListener(Map<String, Long> cooldowns) {
		this.cooldowns = cooldowns;
		this.protocolManager = ProtocolLibrary.getProtocolManager();
		registerPacketListener();
	}

	private void registerPacketListener() {
		protocolManager.addPacketListener(new PacketAdapter(Finale.getPlugin(),
				ListenerPriority.NORMAL, PacketType.Play.Server.SET_COOLDOWN) {
			@Override
			public void onPacketSending(PacketEvent event) {
				if (event.getPacketType() != PacketType.Play.Server.SET_COOLDOWN) {
					return;
				}
				PacketContainer packet = event.getPacket();
				System.out.println(packet.getIntegers().read(0));
				System.out.println(packet.getModifier().read(0).getClass().getSimpleName());
				System.out.println(packet.getModifier().read(0));
			}
		});
	}

}
