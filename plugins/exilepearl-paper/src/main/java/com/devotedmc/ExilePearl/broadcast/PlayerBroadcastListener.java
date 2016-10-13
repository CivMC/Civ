package com.devotedmc.ExilePearl.broadcast;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.Lang;

import vg.civcraft.mc.civmodcore.util.Guard;
import vg.civcraft.mc.civmodcore.util.TextUtil;

public class PlayerBroadcastListener implements BroadcastListener {
	
	private final UUID playerId;
	
	public PlayerBroadcastListener(final UUID playerId) {
		Guard.ArgumentNotNull(playerId, "playerId");
		
		this.playerId = playerId;
	}
	
	@Override
	public void broadcast(ExilePearl pearl) {
		Location l = pearl.getHolder().getLocation();
		String name = pearl.getHolder().getName();
		
		String msg = TextUtil.parse(Lang.pearlBroadcast, name, l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getWorld().getName());
		
		Player p = Bukkit.getPlayer(playerId);
		if (p != null && p.isOnline()) {
			p.sendMessage(msg);
		}
	}
	
	@Override
	public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PlayerBroadcastListener other = (PlayerBroadcastListener) o;

		return playerId.equals(other.playerId);
	}
	
	@Override
	public int hashCode() {
		return playerId.hashCode();
	}
}
