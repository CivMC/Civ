package isaac.bastion.commands;

import isaac.bastion.Bastion;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import vg.civcraft.mc.citadel.CitadelConfigManager;

public class PlayersStates {
	static public enum Mode {
		NORMAL,INFO,DELETE,OFF,BASTION,MATURE
	}
	static private Map<UUID,Mode> playersModes = new HashMap<UUID,Mode>();
	static private Map<UUID,Integer> playersCallback = new HashMap<UUID,Integer>();

	static public boolean playerInMode(Player player, Mode mode){
		Mode result = playersModes.get(player.getUniqueId());
		return result == mode;
	}

	static public void touchPlayer(Player player){
		registerReturnToNormal(player, playersModes.get(player.getUniqueId()));
	}

	static private class CallBack implements Runnable{
		private UUID forPlayer;
		private Mode forMode;
		public CallBack(UUID player,Mode mode){
			forPlayer = player;
			forMode = mode; 
		}

		@Override
		public void run() {
			try {
				PlayersStates.setModeForPlayer(Bastion.getPlugin().getServer().getPlayer(forPlayer), forMode);
			} catch (NullPointerException e) {
				// TODO: player offline perhaps? do something here.
			}
		}
	}

	static public void setModeForPlayer(Player player, Mode mode){
		if (mode==null) {
			mode = Mode.NORMAL;
		}
		
		UUID pid = player.getUniqueId();
		
		Mode old = playersModes.get(pid);
		
		if (old != null && old != mode && old != Mode.NORMAL) {
			player.sendMessage(ChatColor.YELLOW + "Bastion " + old.name() + " mode off");
		}
		player.sendMessage(ChatColor.GREEN + "Bastion " + mode.name() + " mode on");

		playersModes.put(pid, mode);
		registerReturnToNormal(player,mode);
	}
	
	static public void toggleModeForPlayer(Player player, Mode mode) {
		Mode old = playersModes.get(player.getUniqueId());
		if (old == mode) {
			mode = Mode.NORMAL;
		}
		setModeForPlayer(player, mode);
	}

	static private void registerReturnToNormal(Player player, Mode fromMode){
		Integer previousId = playersCallback.get(player.getUniqueId());

		if (previousId != null) {
			Bukkit.getScheduler().cancelTask(previousId);
		}
		if (fromMode != Mode.NORMAL) {
			Integer id=Bukkit.getScheduler().runTaskLater(
					Bastion.getPlugin(),
					new CallBack(player.getUniqueId(), Mode.NORMAL),
					20L * CitadelConfigManager.getPlayerStateReset()).getTaskId();

			playersCallback.put(player.getUniqueId(), id);
		}

	}
}
