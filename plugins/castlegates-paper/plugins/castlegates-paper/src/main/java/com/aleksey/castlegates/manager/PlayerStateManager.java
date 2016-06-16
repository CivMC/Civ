/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.manager;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.aleksey.castlegates.CastleGates;
import com.aleksey.castlegates.types.CommandMode;

public class PlayerStateManager {
	private static class PlayerState {
		public CommandMode mode;
		public long lastInteracted;
	}
	
	private Map<Player, PlayerState> states = new WeakHashMap<Player, PlayerState>();
	private Map<Player, Integer> tasks = new WeakHashMap<Player, Integer>();

	public void clearPlayerMode(Player player) {
		Integer taskId = this.tasks.get(player);
		
		if(taskId != null) {
			Bukkit.getScheduler().cancelTask(taskId);
		}

		PlayerState state = this.states.get(player);
		
		if(state == null) return;
		
		this.states.remove(player);
			
		if(player.isOnline()) {
			player.sendMessage(ChatColor.YELLOW + "Castle Gates " + state.mode.name() + " mode off");
		}
	}
	
	public CommandMode getPlayerMode(Player player) {
		PlayerState state = this.states.get(player);
		return state != null ? state.mode: CommandMode.OFF;
	}
	
	public void setPlayerMode(Player player, CommandMode mode) {
		if(mode == CommandMode.OFF) {
			clearPlayerMode(player);
			return;
		}
		
		PlayerState state = this.states.get(player);
		
		if(state == null) {
			if(mode == CommandMode.NORMAL) {
				player.sendMessage(ChatColor.GREEN + "Castle Gates " + mode.name() + " mode on");
				return;
			}
			
			this.states.put(player, state = new PlayerState());
		}
		else if(state.mode == mode) {
			clearPlayerMode(player);
			return;
		}
		
		state.mode = mode;
		state.lastInteracted = System.currentTimeMillis();

		addTask(player);
		
		player.sendMessage(ChatColor.GREEN + "Castle Gates " + mode.name() + " mode on");
	}
	
	public void interact(Player player) {
		PlayerState state = this.states.get(player);
		
		if(state != null) {
			state.lastInteracted = System.currentTimeMillis();
		}
	}
	
	private void addTask(final Player player) {
		if(this.tasks.containsKey(player)) return;
		
		final PlayerStateManager stateManager = this;
		
		int taskId = Bukkit.getScheduler().runTaskTimer(
				CastleGates.getInstance(),
				new Runnable() {
		            public void run() {
		            	PlayerState state = stateManager.states.get(player);
		            	
		            	if(state != null) {
		            		long offTime = state.lastInteracted + 1000L * CastleGates.getConfigManager().getPlayerStateResetInSeconds();
		            		
		            		if(offTime < System.currentTimeMillis()) {
		            			clearPlayerMode(player);
		            		}
		            	}
		            }
				},
		        20L * 30,
		        20L * 30
			).getTaskId();
		
		this.tasks.put(player, taskId);
	}
}
