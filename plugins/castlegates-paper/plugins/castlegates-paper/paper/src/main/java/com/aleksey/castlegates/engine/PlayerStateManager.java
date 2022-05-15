/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.engine;

import java.util.Map;
import java.util.HashMap;

import com.aleksey.castlegates.CastleGates;
import com.aleksey.castlegates.types.TimerMode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.aleksey.castlegates.types.CommandMode;
import com.aleksey.castlegates.types.TimerOperation;

public class PlayerStateManager {
	public static class PlayerState {
		public CommandMode mode;
		public Integer timer;
		public TimerOperation timerOperation;
		public TimerMode timerMode;
		public long lastInteracted;
	}

	private final Map<Player, PlayerState> _states = new HashMap<>();
	private final Map<Player, Integer> _tasks = new HashMap<>();

	public void clearPlayerMode(Player player) {
		Integer taskId = _tasks.get(player);

		if(taskId != null) {
			Bukkit.getScheduler().cancelTask(taskId);
		}

		PlayerState state = _states.get(player);

		if(state == null) return;

		_states.remove(player);

		if(player.isOnline()) {
			player.sendMessage(ChatColor.YELLOW + "Castle Gates " + state.mode.name() + " mode off");
		}
	}

	public PlayerState getPlayerState(Player player) {
		return _states.get(player);
	}

	public void setPlayerMode(Player player, CommandMode mode, Integer timer, TimerOperation timerOperation, TimerMode timerMode) {
		if(mode == CommandMode.OFF) {
			clearPlayerMode(player);
			return;
		}

		PlayerState state = _states.get(player);

		if(state == null) {
			if(mode == CommandMode.NORMAL) {
				player.sendMessage(ChatColor.GREEN + "Castle Gates " + mode.name() + " mode on");
				return;
			}

			_states.put(player, state = new PlayerState());
		}
		else if(state.mode == mode) {
			clearPlayerMode(player);
			return;
		}

		state.mode = mode;
		state.timer = timer;
		state.timerOperation = timerOperation;
		state.timerMode = timerMode;
		state.lastInteracted = System.currentTimeMillis();

		addTask(player);

		player.sendMessage(ChatColor.GREEN + "Castle Gates " + mode.name() + " mode on");
	}

	public void interact(Player player) {
		PlayerState state = _states.get(player);

		if(state != null) {
			state.lastInteracted = System.currentTimeMillis();
		}
	}

	private void addTask(final Player player) {
		if(_tasks.containsKey(player)) return;

		final PlayerStateManager stateManager = this;

		int taskId = Bukkit.getScheduler().runTaskTimer(
				CastleGates.getInstance(),
				() -> {
					PlayerState state = stateManager._states.get(player);
					if(state != null) {
						long offTime = state.lastInteracted + 1000L * CastleGates.getConfigManager().getPlayerStateResetInSeconds();

						if(offTime < System.currentTimeMillis()) {
							clearPlayerMode(player);
						}
					}
				},
		        20L * 30,
		        20L * 30
			).getTaskId();

		_tasks.put(player, taskId);
	}
}
