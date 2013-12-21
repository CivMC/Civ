package spaceFountain.bastion.commands;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import spaceFountain.bastion.Bastion;
import spaceFountain.bastion.BastionBlock;
import spaceFountain.bastion.commands.PlayersStates.Mode;
import spaceFountain.bastion.manager.BastionBlockManager;

public class CommandListener implements Listener{
	private static BastionBlockManager manager;
	public CommandListener(){
		manager=Bastion.getBastionManager();
	}
	@EventHandler
	public void clicked(PlayerInteractEvent event){
		//if(event.isCancelled())
		//return;
		if(event.getAction()!=Action.RIGHT_CLICK_BLOCK)
			return;

		Block block=event.getClickedBlock();

		Player player=event.getPlayer();
		Mode mode=PlayersStates.modeForPlayer(player);

		BastionBlock bastionBlock=Bastion.getBastionManager().
				bastions.getBastionBlock(block.getLocation());
		switch(mode){
		case INFO:
			boolean dev=player.hasPermission("Bastion.dev");
			String toSend=manager.infoMessage(dev, event);
			if(toSend!=null){
				player.sendMessage(manager.infoMessage(dev, event));
			}
			break;
		case DELETE:
			if(bastionBlock==null){
				return;
			}
			if(bastionBlock.canRemove(player)){
				if(Bastion.getBastionManager().bastions.silentRemove(bastionBlock)){
					player.sendMessage(ChatColor.GREEN+"Bastion Deleted");
					event.setCancelled(true);
				}
			}
			break;
		default:
			break;

		}
	}
}
