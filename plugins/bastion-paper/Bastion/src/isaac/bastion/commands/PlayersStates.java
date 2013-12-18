package isaac.bastion.commands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public class PlayersStates {
	static public enum Mode {
	    NORMAL,INFO,DELETE
	}
	static private Map<Player,Mode> playersModes=new HashMap<Player,Mode>();
	
	static public Mode modeForPlayer(Player player){
		Mode result=playersModes.get(player);
		if(result==null)
			return Mode.NORMAL;
		return result;
	}
	static public void setModeForPlayer(Player player,Mode mode){
		if(mode==null)
			mode=Mode.NORMAL;
		playersModes.put(player, mode);
	}
}
