package isaac.bastion.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import isaac.bastion.Bastion;

public class PlayersStates {
	static public enum Mode {
		NORMAL,INFO,DELETE,OFF,BASTION,MATURE
	}
	static private Map<UUID,Mode> playersModes = new HashMap<>();
	static private Map<UUID,Integer> playersCallback = new HashMap<>();

	static public boolean playerInMode(Player player, Mode mode){
		Mode result = playersModes.get(player.getUniqueId());
		return result == mode;
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

	public static void setModeForPlayer(Player player, Mode mode){
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
	}
	
	public static void toggleModeForPlayer(Player player, Mode mode) {
		Mode old = playersModes.get(player.getUniqueId());
		if (old == mode) {
			mode = Mode.NORMAL;
		}
		setModeForPlayer(player, mode);
	}
}
