package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.google.common.collect.ImmutableList;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.bots.Bot;

public class HackBotPingHook extends PacketAdapter {
	private HackBot hack;
	private SimpleAdminHacks plugin;
	public HackBotPingHook(SimpleAdminHacks plugin, HackBot hack) {
		super(plugin, ListenerPriority.HIGHEST, PacketType.Status.Server.SERVER_INFO);
		this.plugin = plugin;
		this.hack = hack;
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		if (PacketType.Status.Server.SERVER_INFO == event.getPacketType()) {
			this.plugin.debug("Sending SERVER_INFO data");
			List<WrappedServerPing> pings = event.getPacket().getServerPings().getValues();
			this.plugin.debug("  Found {0} PING replies", pings.size());
			if (pings.size() > 0) {
				for (WrappedServerPing ping: pings) {
					int real = ping.getPlayersOnline();
					List<Bot> fakes = hack.getAliveBots();
					int fake = real + fakes.size();
					ping.setPlayersOnline(fake);
					this.plugin.debug("  Replaced {0} with {1} online players", new Object[]{real, fake});
					ImmutableList<WrappedGameProfile> players = ping.getPlayers();
					ImmutableList.Builder<WrappedGameProfile> builder = ImmutableList.builder();
					builder.addAll(players);
					for (Bot b : fakes) {
						builder.add(WrappedGameProfile.fromPlayer(b.npc().getBukkitEntity()));
					}
					ping.setPlayers(builder.build());
					this.plugin.debug("  Injected fake players");
				}
			} else {
				this.plugin.debug("  Constructing fake ping");
				WrappedServerPing ping = new WrappedServerPing();
				ArrayList<Player> players = new ArrayList<Player>(this.plugin.getServer().getOnlinePlayers());
				int real = players.size();
				List<Bot> fakes = hack.getAliveBots();
				int fake = real + fakes.size();
				ping.setPlayersOnline(fake);
				this.plugin.debug("  Replaced {0} with {1} online players", new Object[]{real, fake});
				for (Bot b : fakes) {
					players.add(b.npc().getBukkitEntity());
				}
				ping.setBukkitPlayers(players);
				ping.setMotD(plugin.getServer().getMotd());
				ping.setPlayersMaximum(plugin.getServer().getMaxPlayers());
				ping.setVersionName(plugin.getServer().getVersion());
				pings.add(ping);
			}
		}
	}
}
