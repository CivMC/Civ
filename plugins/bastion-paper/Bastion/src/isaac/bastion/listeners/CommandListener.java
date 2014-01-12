package isaac.bastion.listeners;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.commands.PlayersStates;
import isaac.bastion.commands.PlayersStates.Mode;
import isaac.bastion.manager.BastionBlockManager;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class CommandListener implements Listener{
	private static BastionBlockManager manager;
	public CommandListener(){
		manager=Bastion.getBastionManager();
	}
	@EventHandler
	public void clicked(PlayerInteractEvent event){
		if(event.isCancelled())
			return;
		
		if(event.getAction()!=Action.RIGHT_CLICK_BLOCK)
			return;

		Block block=event.getClickedBlock();
		Player player=event.getPlayer();
		
		if(PlayersStates.playerInMode(player, Mode.NORMAL)){
			return;
		}
		
		
		if(PlayersStates.playerInMode(player, Mode.INFO)){
			boolean dev=player.hasPermission("Bastion.dev");
			String toSend=manager.infoMessage(dev, event);
			if(toSend!=null){
				PlayersStates.touchPlayer(player);
				player.sendMessage(manager.infoMessage(dev, event));
			}
		} else if(PlayersStates.playerInMode(player, Mode.DELETE)){
			
			BastionBlock bastionBlock=Bastion.getBastionManager().
					bastions.getBastionBlock(block.getLocation());
			
			if(bastionBlock==null)
				return;
			
			if(bastionBlock.canRemove(player)){
				if(Bastion.getBastionManager().bastions.silentRemove(bastionBlock)){
					player.sendMessage(ChatColor.GREEN+"Bastion Deleted");
					PlayersStates.touchPlayer(player);
					event.setCancelled(true);
				}
			}
		}
	}
}
