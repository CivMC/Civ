package com.devotedmc.ExilePearl.listener;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.PearlFreeReason;
import com.devotedmc.ExilePearl.PearlPlayer;
import com.devotedmc.ExilePearl.event.PearlMovedEvent;
import com.devotedmc.ExilePearl.event.PlayerFreedEvent;
import com.devotedmc.ExilePearl.event.PlayerPearledEvent;
import com.devotedmc.ExilePearl.util.Guard;
import com.devotedmc.ExilePearl.util.TextUtil;

/**
 * Handles events related to prison pearls
 * @author GFQ
 */
public class PlayerListener implements Listener {

	private final ExilePearlApi pearlApi;
	
	/**
	 * Creates a new PlayerListener instance
	 * @param pearlApi The pearlApi instance
	 */
	public PlayerListener(final ExilePearlApi pearlApi) {
		Guard.ArgumentNotNull(pearlApi, "pearlApi");
		
		this.pearlApi = pearlApi;
	}


	/**
	 * Announce the person in a pearl when a player holds it
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemHeldChange(PlayerItemHeldEvent event) {

		Inventory inv = event.getPlayer().getInventory();
		ItemStack item = inv.getItem(event.getNewSlot());
		ItemStack newitem = validatePearl(event.getPlayer(), item);
		if (newitem != null) {
			inv.setItem(event.getNewSlot(), newitem);
		}
	}


	/**
	 * Announces a pearl change
	 * @param player 
	 * @param item
	 * @return
	 */
	private ItemStack validatePearl(Player player, ItemStack item) {
		if (item == null) {
			return null;
		}

		if (item.getType() == Material.ENDER_PEARL) {
			ExilePearl pearl = null;
			if(item.getItemMeta() != null && item.getItemMeta().getLore() != null && item.getItemMeta().getLore().get(0).contains("Exile Pearl")) {
				 pearl = pearlApi.getPearlFromItemStack(item);
				if (pearl == null || pearl.getFreedOffline()) {
					return new ItemStack(Material.ENDER_PEARL, 1);
				}
			}
		}

		return null;
	}


	/**
	 * Track the location of a pearl if it spawns as an item for any reason
	 * @param e The event args
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemSpawn(ItemSpawnEvent e) {
		Item item = e.getEntity();

		ExilePearl pearl = pearlApi.getPearlFromItemStack(item.getItemStack());
		if (pearl == null) {
			return;
		}

		pearl.setHolder(item);
	}


	/**
	 * Drops a pearl when the player leaves the game
	 * @param event The event args
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player imprisoner = event.getPlayer();

		/* TODO CombatTag
		if (SabrePlugin.instance().getCombatTag().isTagged(imprisoner.getUniqueId())) {
			return; // if player is tagged
		} */


		Location loc = imprisoner.getLocation();
		World world = imprisoner.getWorld();
		Inventory inv = imprisoner.getInventory();
		for (Entry<Integer, ? extends ItemStack> entry :
			inv.all(Material.ENDER_PEARL).entrySet()) {
			ItemStack item = entry.getValue();
			ExilePearl pearl = pearlApi.getPearlFromItemStack(item);
			if (pearl == null) {
				continue;
			}
			int slot = entry.getKey();
			inv.clear(slot);
			world.dropItemNaturally(loc, item);
		}
		imprisoner.saveData();
	}



	/**
	 * Prevents a pearl from despawning
	 * @param e The event args
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemDespawn(ItemDespawnEvent e) {
		ExilePearl pearl = pearlApi.getPearlFromItemStack(e.getEntity().getItemStack());
		if (pearl != null) {
			e.setCancelled(true);
		}
	}


	/**
	 * Free the pearl if it burns up
	 * @param e The event args
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityCombustEvent(EntityCombustEvent e) {
		if (!(e.getEntity() instanceof Item)) {
			return;
		}

		ExilePearl pearl = pearlApi.getPearlFromItemStack(((Item) e.getEntity()).getItemStack());
		if (pearl == null) {
			return;
		}

		pearlApi.log("%s (%s) is being freed. Reason: ExilePearl combusted(lava/fire).", pearl.getPlayerName(), pearl.getPlayerId());
		pearlApi.freePearl(pearl, PearlFreeReason.PEARL_DESTROYED);
	}


	/**
	 * Handle inventory dragging properly
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryDrag(InventoryDragEvent event) {

		Map<Integer, ItemStack> items = event.getNewItems();

		for(Integer slot : items.keySet()) {
			ItemStack item = items.get(slot);

			ExilePearl pearl = pearlApi.getPearlFromItemStack(item);
			if(pearl != null) {
				boolean clickedTop = event.getView().convertSlot(slot) == slot;

				InventoryHolder holder = clickedTop ? event.getView().getTopInventory().getHolder() : event.getView().getBottomInventory().getHolder();

				updatePearlHolder(pearl, holder, event);

				if(event.isCancelled()) {
					return;
				}
			}
		}
	}


	/**
	 * Updates the pearl holder
	 * @param pearl The pearl to update
	 * @param holder The pearl holder
	 * @param event The event
	 */
	private void updatePearlHolder(ExilePearl pearl, InventoryHolder holder, Cancellable event) {

		if (holder instanceof Chest) {
			updatePearl(pearl, (Chest)holder);
		} else if (holder instanceof DoubleChest) {
			updatePearl(pearl, (Chest) ((DoubleChest) holder).getLeftSide());
		} else if (holder instanceof Furnace) {
			updatePearl(pearl, (Furnace) holder);
		} else if (holder instanceof Dispenser) {
			updatePearl(pearl, (Dispenser) holder);
		} else if (holder instanceof BrewingStand) {
			updatePearl(pearl, (BrewingStand) holder);
		} else if (holder instanceof Player) {
			updatePearl(pearl, (Player) holder);
		}else {
			event.setCancelled(true);
		}
	}


	/**
	 * Updates the pearl status
	 * @param pearl The prison pearl
	 * @param block The block storing the pearl
	 */
	private <ItemBlock extends InventoryHolder & BlockState> void updatePearl(ExilePearl pearl, ItemBlock block) {
		pearl.setHolder(block.getBlock());
	}

	
	/**
	 * Updates the pearl status
	 * @param pearl The prison pearl
	 * @param player The player holding the pearl
	 */
	private void updatePearl(ExilePearl pearl, Player player) {
		pearl.setHolder(pearlApi.getPearlPlayer(player.getUniqueId()));
	}


	/**
	 * Prevent imprisoned players from placing ExilePearls in their inventory.
	 * @param event
	 */
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onExilePearlClick(InventoryClickEvent e) {
		Player clicker = (Player) e.getWhoClicked();

		ExilePearl pearl = pearlApi.getPearlFromItemStack(e.getCurrentItem());
		if(pearl != null) {
			if (pearlApi.isPlayerExiled(clicker)) {
				pearlApi.getPearlPlayer(clicker.getUniqueId()).msg(Lang.pearlCantHold);
				e.setCancelled(true);
			}
		}
	}


	/**
	 * Prevent imprisoned players from picking up Prisonpearls.
	 * @param e The event args
	 */
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerPickupPearl(PlayerPickupItemEvent e) {
		Item item = e.getItem();

		ExilePearl pearl = pearlApi.getPearlFromItemStack(item.getItemStack());
		if (pearl == null) {
			return;
		}

		if (pearlApi.isPlayerExiled(e.getPlayer())) {
			e.setCancelled(true);
		}
	}


	/**
	 * Track the location of a pearl
	 * Forbid pearls from being put in storage minecarts
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {

		// Announce an prison pearl if it is clicked
		ItemStack newitem = validatePearl((Player) event.getWhoClicked(), event.getCurrentItem());
		if (newitem != null) {
			event.setCurrentItem(newitem);
		}

		InventoryAction a = event.getAction();
		if(a == InventoryAction.COLLECT_TO_CURSOR
				|| a == InventoryAction.PICKUP_ALL 
				|| a == InventoryAction.PICKUP_HALF
				|| a == InventoryAction.PICKUP_ONE) {
			ExilePearl pearl = pearlApi.getPearlFromItemStack(event.getCurrentItem());

			if(pearl != null) {
				pearl.setHolder(pearlApi.getPearlPlayer(((Player) event.getWhoClicked()).getUniqueId()));
			}
		}
		else if(event.getAction() == InventoryAction.PLACE_ALL
				|| event.getAction() == InventoryAction.PLACE_SOME
				|| event.getAction() == InventoryAction.PLACE_ONE) {	
			ExilePearl pearl = pearlApi.getPearlFromItemStack(event.getCursor());

			if(pearl != null) {
				boolean clickedTop = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();

				InventoryHolder holder = clickedTop ? event.getView().getTopInventory().getHolder() : event.getView().getBottomInventory().getHolder();
				if (holder != null) {
					updatePearlHolder(pearl, holder, event);
				}
			}
		}
		else if(event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {			
			ExilePearl pearl = pearlApi.getPearlFromItemStack(event.getCurrentItem());

			if(pearl != null) {
				boolean clickedTop = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();

				InventoryHolder holder = !clickedTop ? event.getView().getTopInventory().getHolder() : event.getView().getBottomInventory().getHolder();
				if(holder != null && holder.getInventory().firstEmpty() >= 0) {
					updatePearlHolder(pearl, holder, event);
				}
			}
		}
		else if(event.getAction() == InventoryAction.HOTBAR_SWAP) {
			PlayerInventory playerInventory = event.getWhoClicked().getInventory();
			ExilePearl pearl = pearlApi.getPearlFromItemStack(playerInventory.getItem(event.getHotbarButton()));

			if(pearl != null) {
				boolean clickedTop = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();

				InventoryHolder holder = clickedTop ? event.getView().getTopInventory().getHolder() : event.getView().getBottomInventory().getHolder();

				updatePearlHolder(pearl, holder, event);
			}

			if(event.isCancelled())
				return;

			pearl = pearlApi.getPearlFromItemStack(event.getCurrentItem());

			if(pearl != null) {
				updatePearl(pearl, (Player) event.getWhoClicked());
			}
		}
		else if (event.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
			ExilePearl pearl = pearlApi.getPearlFromItemStack(event.getCursor());

			if(pearl != null) {
				boolean clickedTop = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();

				InventoryHolder holder = clickedTop ? event.getView().getTopInventory().getHolder() : event.getView().getBottomInventory().getHolder();

				updatePearlHolder(pearl, holder, event);
			}

			if(event.isCancelled())
				return;

			pearl = pearlApi.getPearlFromItemStack(event.getCurrentItem());

			if(pearl != null) {
				updatePearl(pearl, (Player) event.getWhoClicked());
			}
		}
		else if(event.getAction() == InventoryAction.DROP_ALL_CURSOR
				|| event.getAction() == InventoryAction.DROP_ALL_SLOT
				|| event.getAction() == InventoryAction.DROP_ONE_CURSOR
				|| event.getAction() == InventoryAction.DROP_ONE_SLOT) {
			// Handled by onItemSpawn
		}
		else if (a != InventoryAction.NOTHING) {
			if(pearlApi.getPearlFromItemStack(event.getCurrentItem()) != null || pearlApi.getPearlFromItemStack(event.getCursor()) != null) {
				((Player) event.getWhoClicked()).sendMessage(ChatColor.RED + "You can't do that with an exile pearl.");

				event.setCancelled(true);
			}
		}
	}


	/**
	 * Track the location of a pearl if a player picks it up
	 * @param e The event args
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerPickupItem(PlayerPickupItemEvent e) {
		Item item = e.getItem();

		ExilePearl pearl = pearlApi.getPearlFromItemStack(item.getItemStack());
		if (pearl == null) {
			return;
		}

		updatePearl(pearl, (Player) e.getPlayer());
	}


	/**
	 * Imprison people upon death
	 * @param event The event args
	 */
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent e) {
		if (!(e.getEntity() instanceof Player)) {
			return;
		}

		Player player = (Player)e.getEntity();

		Player killer = player.getKiller();
		if (killer != null) {
			
			// Need to get by name b/c of combat tag entity
			PearlPlayer imprisoned = pearlApi.getPearlPlayer(e.getEntity().getName());

			int firstpearl = Integer.MAX_VALUE;
			for (Entry<Integer, ? extends ItemStack> entry : killer.getInventory().all(Material.ENDER_PEARL).entrySet()) {

				// Make sure we're holding a blank pearl
				if (pearlApi.getPearlFromItemStack(entry.getValue()) == null) {
					firstpearl = Math.min(entry.getKey(), firstpearl);
				}
			}

			if (firstpearl ==  Integer.MAX_VALUE) {
				return;
			}
			
			ExilePearl pearl = pearlApi.exilePlayer(imprisoned.getPlayer(), killer);
			if (pearl == null) {
				return;
			}
			
			// Pearl succeeded
			// set up the imprisoner's inventory
			Inventory inv = killer.getInventory();
			ItemStack stack = null;
			int stacknum = -1;
			
			// Scan for the smallest stack of normal ender pearls
			for (Entry<Integer, ? extends ItemStack> entry :
					inv.all(Material.ENDER_PEARL).entrySet()) {
				ItemStack newstack = entry.getValue();
				int newstacknum = entry.getKey();
				if (newstack.getDurability() == 0) {
					if (stack != null) {
						// don't keep a stack bigger than the previous one
						if (newstack.getAmount() > stack.getAmount()) {
							continue;
						}
						// don't keep an identical sized stack in a higher slot
						if (newstack.getAmount() == stack.getAmount() &&
								newstacknum > stacknum) {
							continue;
						}
					}

					stack = newstack;
					stacknum = entry.getKey();
				}
			}
			
			int pearlnum;
			ItemStack dropStack = null;
			if (stacknum == -1) { // no pearl (admin command)
				// give him a new one at the first empty slot
				pearlnum = inv.firstEmpty();
			} else if (stack.getAmount() == 1) { // if he's just got one pearl
				pearlnum = stacknum; // put the prison pearl there
			} else {
				// otherwise, put the prison pearl in the first empty slot
				pearlnum = inv.firstEmpty();
				if (pearlnum > 0) {
					// and reduce his stack of pearls by one
					stack.setAmount(stack.getAmount() - 1);
					inv.setItem(stacknum, stack);
				} else { // no empty slot?
					dropStack = new ItemStack(Material.ENDER_PEARL, stack.getAmount() - 1);
					pearlnum = stacknum; // then overwrite his stack of pearls
				}
			}
			
			// Drop pearls that otherwise would be deleted
			Location l = killer.getLocation();
			if (dropStack != null) {
				killer.getWorld().dropItem(l, dropStack);
				pearlApi.log(l + ", " + dropStack.getAmount());
			}
			
			inv.setItem(pearlnum, pearl.createItemStack());
		}
	}
	
	
	/**
	 * Handles logging in players
	 * @param e The event args
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e) {
		UUID uid = e.getPlayer().getUniqueId();
		ExilePearl pearl = pearlApi.getPearl(uid);
		if (pearl != null && pearl.getFreedOffline()) {
			pearl.getPlayer().msg(Lang.pearlYouWereFreed);
			pearlApi.freePearl(pearl,PearlFreeReason.FREED_OFFLINE);
		}
	}
	
	/**
	 * Frees pearls when right-clicked
	 * @param e The event args
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent e) {
		ExilePearl pearl = pearlApi.getPearlFromItemStack(e.getItem());
		if (pearl == null) {
			return;
		}

		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Material m = e.getClickedBlock().getType();
			if (m == Material.CHEST || m == Material.WORKBENCH
					|| m == Material.FURNACE || m == Material.DISPENSER
					|| m == Material.BREWING_STAND)
				return;
		} else if (e.getAction() != Action.RIGHT_CLICK_AIR) {
			return;
		}

		Player player = e.getPlayer();
		player.getInventory().setItemInMainHand(null);
		e.setCancelled(true);
		
		pearlApi.freePearl(pearl, PearlFreeReason.PEARL_THROWN);
	}
	
	
	/**
	 * Handles new prison pearl events
	 * @param e The event args
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerPearled(PlayerPearledEvent e) {

		PearlPlayer imprisoned = e.getPearl().getPlayer();
		PearlPlayer imprisoner = pearlApi.getPearlPlayer(e.getKilledBy().getUniqueId());
		
		// Log the capturing ExilePearl event.
		pearlApi.log(String.format("%s has bound %s to a ExilePearl", imprisoner.getName(), imprisoned.getName()));
		
		imprisoner.msg(Lang.pearlYouBound, imprisoned.getName());
		imprisoned.msg(Lang.pearlYouWereBound, imprisoner.getName());
	}
	
	
	/**
	 * Handles player freed events
	 * @param e The event args
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerFreed(PlayerFreedEvent e) {
		e.getPearl().getPlayer().msg(Lang.pearlYouWereFreed);
	}
	
	
	/**
	 * Handles a pearl move event
	 * @param e The event args
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onPearlMoved(PearlMovedEvent e) {
		
		PearlPlayer imprisoned = e.getPearl().getPlayer();
		
		Location l = e.getDestinationHolder().getLocation();
		String name = e.getDestinationHolder().getName();
		imprisoned.msg(Lang.pearlPearlIsHeld, name, l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getWorld().getName());	
		
		String bcastMsg = TextUtil.instance().parse(Lang.pearlBroadcast, imprisoned.getName(), 
				name, l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getWorld().getName());
		
		for(PearlPlayer p : imprisoned.getBcastPlayers()) {
			p.msg(bcastMsg);
		}
	}
	
	
	/**
	 * Clears out a player's inventory when being summoned from the end
	 * @param player The player instance
	 * @param loc The location
	 */
    public void dropInventory(Player player, Location loc) {
		if (loc == null) {
			loc = player.getLocation();
		}
		World world = loc.getWorld();
		Inventory inv = player.getInventory();
		int end = inv.getSize();
		for (int i = 0; i < end; ++i) {
			ItemStack item = inv.getItem(i);
			if (item == null) {
				continue;
			}
			if (item.getType().equals(Material.ENDER_PEARL)) {
				continue;
			}
			inv.clear(i);
			world.dropItemNaturally(loc, item);
		}
	}	
}
