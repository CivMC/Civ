package isaac.bastion.listeners;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.BastionType;
import isaac.bastion.commands.PlayersStates;
import isaac.bastion.commands.PlayersStates.Mode;
import isaac.bastion.manager.BastionBlockManager;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.events.ReinforcementCreationEvent;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;

public class BastionInteractListener implements Listener {
	
	private BastionBlockManager manager;
	private HashMap<Location, String> pendingBastions;
	
	public BastionInteractListener() {
		manager = Bastion.getBastionManager();
		pendingBastions = new HashMap<Location, String>();
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onBlockClicked(PlayerInteractEvent event) {
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
			BastionBlock bastionBlock = Bastion.getBastionStorage().getBastionBlock(block.getLocation());

			if (bastionBlock == null) {
				return;
			}

			if (bastionBlock.canRemove(player)) {
				bastionBlock.destroy();
				player.sendMessage(ChatColor.GREEN + "Bastion Deleted");
				PlayersStates.touchPlayer(player);
				event.setCancelled(true);
			}
		} else if (PlayersStates.playerInMode(player, Mode.MATURE)) {
			BastionBlock bastionBlock=Bastion.getBastionStorage().getBastionBlock(block.getLocation());

			if (bastionBlock == null) {
				return;
			}
			bastionBlock.mature();
			player.sendMessage(ChatColor.GREEN + "Matured");
		} else if (PlayersStates.playerInMode(player, Mode.BASTION)) {
			BastionType type = BastionType.getBastionType(pendingBastions.get(block.getLocation()));
			if(type == null) return; //if it wasnt stored it cant have been a bastion
			PlayerReinforcement reinforcement = (PlayerReinforcement) Citadel.getReinforcementManager().
					getReinforcement(block.getLocation());

			if (!(reinforcement instanceof PlayerReinforcement)) {
				return;
			}

			if (reinforcement.canBypass(player)) {
				if(Bastion.getBastionStorage().createBastion(block.getLocation(), type)) {
					player.sendMessage(ChatColor.GREEN + "Bastion block created");
				} else {
					player.sendMessage(ChatColor.RED + "Failed to create bastion");
				}
				PlayersStates.touchPlayer(player);
			} else{
				player.sendMessage(ChatColor.RED + "You don't have the right permission");
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if(!event.isCancelled()) {
			MaterialData mat = new MaterialData(event.getBlock().getType(), event.getBlock().getData());
			String lore = "";
			if(event.getItemInHand() != null && event.getItemInHand().hasItemMeta() && event.getItemInHand().getItemMeta().hasLore()) {
				lore = event.getItemInHand().getItemMeta().getLore().get(0);
			}
			BastionType type = BastionType.getBastionType(mat, lore);
			if(type != null) pendingBastions.put(event.getBlock().getLocation(), type.getName());
		}
	}
	
	@EventHandler
	public void onReinforcement(ReinforcementCreationEvent event) {
		BastionType type = BastionType.getBastionType(pendingBastions.get(event.getBlock().getLocation()));
		pendingBastions.remove(event.getBlock().getLocation());
		if(type != null && 
				!PlayersStates.playerInMode(event.getPlayer(), Mode.OFF) && event.getReinforcement() instanceof PlayerReinforcement) {
			PlayersStates.touchPlayer(event.getPlayer());
			if(Bastion.getBastionStorage().createBastion(event.getBlock().getLocation(),  type)) {
				event.getPlayer().sendMessage(ChatColor.GREEN + "Bastion block created");
			} else {
				event.getPlayer().sendMessage(ChatColor.RED + "Failed to create bastion");
			}
		}
	}
}
