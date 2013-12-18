package isaac.bastion.commands;

import isaac.bastion.Bastion;
import isaac.bastion.commands.PlayersStates.Mode;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class CommandListener implements Listener{
	@EventHandler
	public void clicked(PlayerInteractEvent event){
		Player player=event.getPlayer();

		Mode mode=PlayersStates.modeForPlayer(player);
		Block block=event.getClickedBlock();
		
		switch(mode){
		case INFO:
			
			break;
		case DELETE:
			Bastion.getBastionManager().bastions.silentRemove(block.getLocation());
			player.sendMessage("Deleted");
			break;
		default:
			break;

		}
	}
}
