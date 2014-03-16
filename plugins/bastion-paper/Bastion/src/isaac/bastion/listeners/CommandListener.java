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

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.entity.PlayerReinforcement;

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
		} else if(PlayersStates.playerInMode(player, Mode.DELETE) && block.getType() == Bastion.getConfigManager().getBastionBlockMaterial()){
			event.setCancelled(true);

			PlayerReinforcement reinforcement = (PlayerReinforcement) Citadel.getReinforcementManager().
					getReinforcement(block.getLocation());

			if(!(reinforcement instanceof PlayerReinforcement))
				return;

			if(reinforcement.isBypassable(player)){
				Bastion.getBastionManager().addBastion(block.getLocation(),reinforcement);
				player.sendMessage(ChatColor.GREEN+"Bastion block created");
				PlayersStates.touchPlayer(player);
			} else{
				player.sendMessage(ChatColor.RED+"You don't have permissions in "+ChatColor.BLACK+reinforcement.getOwner().getName());
			}

		} else if(PlayersStates.playerInMode(player, Mode.MATURE)){

			BastionBlock bastionBlock=Bastion.getBastionManager().
					bastions.getBastionBlock(block.getLocation());

			if(bastionBlock==null)
				return;
			bastionBlock.mature();
		} else if(PlayersStates.playerInMode(player, Mode.BASTION)){

			BastionBlock bastionBlock=Bastion.getBastionManager().
					bastions.getBastionBlock(block.getLocation());

			if(bastionBlock==null)
				return;
			bastionBlock.mature();
		}


	}
}
