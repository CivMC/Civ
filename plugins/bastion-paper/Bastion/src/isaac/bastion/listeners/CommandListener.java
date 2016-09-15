package isaac.bastion.listeners;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.commands.PlayersStates;
import isaac.bastion.commands.PlayersStates.Mode;
import isaac.bastion.manager.BastionBlockManager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;

public class CommandListener implements Listener {
	private static BastionBlockManager manager;
	
	public CommandListener() {
		manager=Bastion.getBastionManager();
	}
	
	@EventHandler(ignoreCancelled=true)
	public void clicked(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		Player player = event.getPlayer();

		if (PlayersStates.playerInMode(player, Mode.NORMAL)) {
			return;
		}

		Block block = event.getClickedBlock();

		if (PlayersStates.playerInMode(player, Mode.INFO)) {
			boolean dev = player.hasPermission("Bastion.dev");
			String toSend = manager.infoMessage(dev, block.getRelative(event.getBlockFace()), block, player);
			if (toSend != null) {
				PlayersStates.touchPlayer(player);
				player.sendMessage(toSend);
			}
		} else if (PlayersStates.playerInMode(player, Mode.DELETE)) {
			BastionBlock bastionBlock = Bastion.getBastionManager().
					set.getBastionBlock(block.getLocation());

			if (bastionBlock == null) {
				return;
			}

			if (bastionBlock.canRemove(player)) {
				if (Bastion.getBastionManager().set.remove(bastionBlock)) {
					player.sendMessage(ChatColor.GREEN + "Bastion Deleted");
					PlayersStates.touchPlayer(player);
					event.setCancelled(true);
				}
			}
		} else if (PlayersStates.playerInMode(player, Mode.MATURE)) {
			BastionBlock bastionBlock=Bastion.getBastionManager().
					set.getBastionBlock(block.getLocation());

			if (bastionBlock == null) {
				return;
			}
			bastionBlock.mature();
			player.sendMessage(ChatColor.GREEN + "Matured");
		} else if (BastionListener.pendingBastions.containsKey(block.getLocation()) && PlayersStates.playerInMode(player, Mode.BASTION)) {
			//event.getPlayer().sendMessage(bastionBlock.name());
			PlayerReinforcement reinforcement = (PlayerReinforcement) Citadel.getReinforcementManager().
					getReinforcement(block.getLocation());

			if (!(reinforcement instanceof PlayerReinforcement)) {
				return;
			}

			if (reinforcement.canBypass(player)) {
				Bastion.getBastionManager().addBastion(block.getLocation(), reinforcement, BastionListener.pendingBastions.get(block.getLocation()));
				player.sendMessage(ChatColor.GREEN + "Bastion block created");
				PlayersStates.touchPlayer(player);
			} else{
				player.sendMessage(ChatColor.RED + "You don't have the right permission");
			}
		}
	}
}
