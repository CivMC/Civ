package isaac.bastion.commands;

import isaac.bastion.Bastion;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.untamedears.citadel.Citadel;

public class PlayersStates {
	static public enum Mode {
		NORMAL,INFO,DELETE,DISABLED
	}
	static private Map<Player,Mode> playersModes=new HashMap<Player,Mode>();
	static private Map<Player,Integer> playersCallback=new HashMap<Player,Integer>();

	static public boolean playerInMode(Player player,Mode mode){
		Mode result=playersModes.get(player);
		if(result==mode)
			return true;
		return false;
	}

	static public void touchPlayer(Player player){
		registerReturnToNormal(player,playersModes.get(player));
	}

	static private class CallBack extends BukkitRunnable{
		private Player forPlayer;
		private Mode forMode;
		public CallBack(Player player,Mode mode){
			forPlayer=player;
			forMode=mode; 
		}

		@Override
		public void run() {
			PlayersStates.setModeForPlayer(forPlayer, forMode);
		}

	}

	static public void setModeForPlayer(Player player,Mode mode){
		if(mode==null)
			mode=Mode.NORMAL;
		
		Mode old=playersModes.get(player);
		
		if(old!=null&&old!=mode&&old!=Mode.NORMAL){
			player.sendMessage(ChatColor.YELLOW+"Bastion "+old.name()+" mode off");
		}
		player.sendMessage(ChatColor.GREEN+"Bastion "+mode.name()+" mode on");

		playersModes.put(player, mode);
		registerReturnToNormal(player,mode);

	}
	static public void toggleModeForPlayer(Player player, Mode mode){
		Mode old=playersModes.get(player);
		if(old==mode)
			mode=Mode.NORMAL;
		setModeForPlayer(player, mode);
	}

	static private void registerReturnToNormal(Player player,Mode fromMode){
		Integer previousId=playersCallback.get(player);

		if(previousId!=null)
			Bukkit.getScheduler().cancelTask(previousId);
		if(fromMode!=Mode.NORMAL){
			Integer id=Bukkit.getScheduler().scheduleSyncDelayedTask(
					Bastion.getPlugin(),
					new CallBack(player,Mode.NORMAL),
					20L * Citadel.getConfigManager().getAutoModeReset());

			playersCallback.put(player, id);
		}

	}
}
