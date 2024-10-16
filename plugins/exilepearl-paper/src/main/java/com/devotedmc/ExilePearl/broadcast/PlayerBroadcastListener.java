package com.devotedmc.ExilePearl.broadcast;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.Lang;
import com.google.common.base.Preconditions;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerBroadcastListener implements BroadcastListener {

	private final UUID playerId;

	public PlayerBroadcastListener(final Player player) {
		Preconditions.checkNotNull(player, "player");

		this.playerId = player.getUniqueId();
	}

	@Override
	public void broadcast(ExilePearl pearl) {
		Location l = pearl.getHolder().getLocation();
		String holderName = pearl.getHolder().getName();

		String msg = String.format(Lang.pearlBroadcast, pearl.getPlayerName(), holderName, l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getWorld().getName());

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

	@Override
	public boolean contains(Object o) {
		return playerId.equals(o);
	}
}
