package vg.civcraft.mc.citadel.playerstate;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import vg.civcraft.mc.citadel.Utility;

public class PlayerStateManager {

	private Map<UUID, AbstractPlayerState> playerStateMap;

	public PlayerStateManager() {
		this.playerStateMap = new TreeMap<>();
	}

	public AbstractPlayerState getState(Player player) {
		if (player == null) {
			throw new IllegalArgumentException("Can not get state for null player");
		}
		AbstractPlayerState state = playerStateMap.get(player.getUniqueId());
		if (state == null) {
			state = new NormalState(player);
			playerStateMap.put(player.getUniqueId(), state);
		}
		return state;
	}

	public void setState(Player player, AbstractPlayerState state) {
		if (player == null) {
			throw new IllegalArgumentException("Can not set state for null player");
		}
		AbstractPlayerState existingState = getState(player);
		// null state is allowed, it just resets the state
		if (state == null) {
			state = new NormalState(player);
		}
		playerStateMap.put(player.getUniqueId(), state);
		Utility.sendAndLog(player, ChatColor.GOLD, "Switched Citadel mode to " + ChatColor.YELLOW + state.getName()
				+ ChatColor.GOLD + " from " + ChatColor.YELLOW + existingState.getName());
	}

}
