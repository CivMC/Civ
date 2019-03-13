package vg.civcraft.mc.citadel.playerstate;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.entity.Player;

public class PlayerStateManager {

	private Map<UUID, IPlayerState> playerStateMap;

	public PlayerStateManager() {
		this.playerStateMap = new TreeMap<>();
	}

	public IPlayerState getState(Player player) {
		if (player == null) {
			throw new IllegalArgumentException("Can not get state for null player");
		}
		IPlayerState state = playerStateMap.get(player.getUniqueId());
		if (state == null) {
			state = new NormalState(player, true);
			playerStateMap.put(player.getUniqueId(), state);
		}
		return state;
	}
	
	public void setState(Player p, IPlayerState state) {
		if (p == null) {
			throw new IllegalArgumentException("Can not set state for null player");
		}
		//null state is allowed, it just resets the state
		if (state == null) {
			playerStateMap.remove(p.getUniqueId());
		}
		else {
			playerStateMap.put(p.getUniqueId(), state);
		}
	}

}
