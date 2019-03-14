package vg.civcraft.mc.citadel.playerstate;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.entity.Player;

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
			state = new NormalState(player, true);
			playerStateMap.put(player.getUniqueId(), state);
		}
		return state;
	}

	public void setState(Player player, AbstractPlayerState state) {
		if (player == null) {
			throw new IllegalArgumentException("Can not set state for null player");
		}
		AbstractPlayerState existingState = playerStateMap.get(player.getUniqueId());
		// null state is allowed, it just resets the state
		if (state == null) {
			if (existingState != null) {
				state = new NormalState(player, existingState.isBypassEnabled());
			} else {
				state = new NormalState(player, true);
			}
			playerStateMap.put(player.getUniqueId(), state);
			player.sendMessage("Switched Citadel mode to " + state.getName());
		} else {
			playerStateMap.put(player.getUniqueId(), state);
			player.sendMessage("Switched Citadel mode to " + state.getName() + " from " + existingState.getName());
		}
	}

}
