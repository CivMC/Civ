package isaac.bastion.listeners;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.BastionType;
import isaac.bastion.commands.PlayersStates;
import isaac.bastion.commands.PlayersStates.Mode;
import isaac.bastion.manager.BastionBlockManager;
import isaac.bastion.storage.BastionBlockStorage;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.events.ReinforcementCreationEvent;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;

public class BastionInteractListener implements Listener {
	
	private BastionBlockManager manager;
	private BastionBlockStorage storage;
	
	public BastionInteractListener() {
		manager = Bastion.getBastionManager();
		storage = Bastion.getBastionStorage();
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onBlockClicked(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		final Player player = event.getPlayer();

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
			BastionBlock bastionBlock = storage.getBastionBlock(block.getLocation());

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
			BastionBlock bastionBlock=storage.getBastionBlock(block.getLocation());

			if (bastionBlock == null) {
				return;
			}
			bastionBlock.mature();
			player.sendMessage(ChatColor.GREEN + "Matured");
		} else if (PlayersStates.playerInMode(player, Mode.BASTION)) {
			final BastionType type = storage.getAndRemovePendingBastion(block.getLocation());
			if(type == null) return; //if it wasnt stored it cant have been a bastion
			PlayerReinforcement reinforcement = (PlayerReinforcement) Citadel.getReinforcementManager().
					getReinforcement(block.getLocation());

			if (!(reinforcement instanceof PlayerReinforcement)) {
				return;
			}
			
			if (reinforcement.canBypass(player)) {
				final Location loc = block.getLocation().clone();
				new BukkitRunnable() {
					@Override
					public void run() {
						if(storage.createBastion(loc,  type, player)) {
							player.sendMessage(ChatColor.GREEN + "Bastion block created");
						} else {
							player.sendMessage(ChatColor.RED + "Failed to create bastion");
						}
					}
				}.runTask(Bastion.getPlugin());
				PlayersStates.touchPlayer(player);
			} else{
				player.sendMessage(ChatColor.RED + "You don't have the right permission");
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		ItemStack inHand = event.getItemInHand();
		if (inHand == null) return;

		BastionType type = blockToType(event.getBlock(), inHand);
		if(type != null) {
			Bastion.getPlugin().getLogger().log(Level.INFO, "Pending a bastion at {0}", event.getBlock().getLocation());
			storage.addPendingBastion(event.getBlock().getLocation(), type);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onReinforcement(ReinforcementCreationEvent event) {
		final BastionType type = storage.getAndRemovePendingBastion(event.getBlock().getLocation());
		if(type != null && 
				!PlayersStates.playerInMode(event.getPlayer(), Mode.OFF) && event.getReinforcement() instanceof PlayerReinforcement) {
			PlayersStates.touchPlayer(event.getPlayer());
			Bastion.getPlugin().getLogger().log(Level.INFO, "Registering to create a {0} bastion", type);
			final Location loc = event.getBlock().getLocation().clone();
			final Player player = event.getPlayer();
			// Can't do it immediately, as the reinforcement doesn't exist _during_ the create event.
			new BukkitRunnable() {
				@Override
				public void run() {
					if(storage.createBastion(loc,  type, player)) {
						player.sendMessage(ChatColor.GREEN + "Bastion block created");
					} else {
						player.sendMessage(ChatColor.RED + "Failed to create bastion");
					}
				}
			}.runTask(Bastion.getPlugin());
		}
	}

	@SuppressWarnings("deprecation")
	public BastionType blockToType(Block block, ItemStack inHand) {
		MaterialData mat = new MaterialData(block.getType(), block.getData());
		String displayName = null;
		List<String> lore = null;
		if (inHand != null) {
			ItemMeta im = inHand.getItemMeta();
			if (im != null && im.hasLore()) {
				lore = im.getLore();
			} 
			if (im != null && im.hasDisplayName()) {
				displayName = im.getDisplayName();
			}
		}
		BastionType type = BastionType.getBastionType(mat, displayName, lore);
		return type;
	}
}
