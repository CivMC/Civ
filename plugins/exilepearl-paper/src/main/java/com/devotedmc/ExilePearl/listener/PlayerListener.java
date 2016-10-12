package com.devotedmc.ExilePearl.listener;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Furnace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
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
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.PearlFreeReason;
import com.devotedmc.ExilePearl.PearlPlayer;
import com.devotedmc.ExilePearl.RepairMaterial;
import com.devotedmc.ExilePearl.event.PearlMovedEvent;
import com.devotedmc.ExilePearl.event.PlayerFreedEvent;
import com.devotedmc.ExilePearl.event.PlayerPearledEvent;

import vg.civcraft.mc.civmodcore.util.Guard;
import vg.civcraft.mc.civmodcore.util.TextUtil;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

/**
 * Handles events related to prison pearls
 * @author GFQ
 */
public class PlayerListener implements Listener {

	private final ExilePearlApi pearlApi;

	private Set<RepairMaterial> repairMaterials = new HashSet<RepairMaterial>();

	/**
	 * Creates a new PlayerListener instance
	 * @param pearlApi The pearlApi instance
	 */
	public PlayerListener(final ExilePearlApi pearlApi) {
		Guard.ArgumentNotNull(pearlApi, "pearlApi");

		this.pearlApi = pearlApi;
	}


	/**
	 * Sets up the pearl repair recipes
	 */
	public void setupRecipes() {

		try {
			// This item is basically used as a trigger to catch the recipe being created
			ItemStack resultItem = new ItemStack(Material.STONE_BUTTON, 1);
			ItemMeta im = resultItem.getItemMeta();
			im.addEnchant(Enchantment.DURABILITY, 1, true);
			im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			resultItem.setItemMeta(im);
			
			repairMaterials.addAll(pearlApi.getPearlConfig().getRepairMaterials());
			
			if (repairMaterials.size() == 0) {
				pearlApi.log("Failed to load any pearl repair materials. Defaulting to Obsidian.");
				repairMaterials.add(new RepairMaterial("Obsidian", new ItemStack(Material.OBSIDIAN), 2));
			}
			
			for(RepairMaterial mat : repairMaterials) {
				// Shapeless recipe
				ShapelessRecipe r1 = new ShapelessRecipe(resultItem);
				r1.addIngredient(1, Material.ENDER_PEARL);
				r1.addIngredient(1, mat.getStack().getData());

				Bukkit.getServer().addRecipe(r1);
			}
			
		} catch (Exception ex) {
			pearlApi.log(Level.SEVERE, "Failed to register the pearl repair recipes.");
		}
	}


	/**
	 * Announce the person in a pearl when a player holds it
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemHeldChange(PlayerItemHeldEvent event) {

		Inventory inv = event.getPlayer().getInventory();
		ItemStack item = inv.getItem(event.getNewSlot());
		ItemStack newitem = validatePearl(item);
		if (newitem != null) {
			inv.setItem(event.getNewSlot(), newitem);
		}
	}


	/**
	 * Updates all pearls in an inventory when opened
	 * @param e The event args
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryOpen(InventoryOpenEvent e) {
		Inventory inv = e.getInventory();
		for (Entry<Integer, ? extends ItemStack> entry : inv.all(Material.ENDER_PEARL).entrySet()) {
			ItemStack newitem = validatePearl(entry.getValue());
			if (newitem != null) {
				inv.setItem(entry.getKey(), newitem);
			}
		}
	}


	/**
	 * Validates an ender pearl item
	 * @param item The item to check
	 * @return the updated item
	 */
	private ItemStack validatePearl(ItemStack item) {
		if (item == null) {
			return null;
		}

		if (item.getType() == Material.ENDER_PEARL && item.getEnchantmentLevel(Enchantment.DURABILITY) != 0) {
			ExilePearl pearl = pearlApi.getPearlFromItemStack(item);
			if (pearl == null || pearl.getFreedOffline()) {
				return new ItemStack(Material.ENDER_PEARL, 1);
			}
			return pearl.createItemStack();
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

		// Don't drop if the player is tagged
		if (pearlApi.isPlayerTagged(imprisoner.getUniqueId())) {
			return;
		}

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
			pearlApi.log("Prevented pearl from despawning at %s for player %s.", pearl.getLocation().toString(), pearl.getPlayerName());
		}
	}
	

	/**
	 * Prevent chunk that contain pearls from unloading
	 * @param e The event args
	 */
	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled = true)
	public void onChunkUnload(ChunkUnloadEvent e) {
		Chunk chunk = e.getChunk();
		for (Entity entity : chunk.getEntities()) {
			if (!(entity instanceof Item)) {
				continue;
			}
			
			Item item = (Item)entity;
			ExilePearl pearl = pearlApi.getPearlFromItemStack(item.getItemStack());
			
			if (pearl != null) {
				e.setCancelled(true);
				pearlApi.log("Prevented chunk (%d, %d) from unloading because it contained an exile pearl for player %s.", chunk.getX(), chunk.getZ(), pearl.getPlayerName());
			}
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
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {

		// Announce an prison pearl if it is clicked
		ItemStack newitem = validatePearl(event.getCurrentItem());
		if (newitem != null) {
			event.setCurrentItem(newitem);
		}

		InventoryAction a = event.getAction();
		//pearlApi.log("Inv Action: " + a.toString());
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
			PearlPlayer pKiller = pearlApi.getPearlPlayer(killer.getUniqueId());

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

			ExilePearl pearl = pearlApi.exilePlayer(imprisoned.getUniqueId(), pKiller.getUniqueId());
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
	 * @param e The event argst
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
		
		if (!pearlApi.getPearlConfig().getFreeByThrowing()) {
			pearlApi.getPearlPlayer(player.getUniqueId()).msg(Lang.pearlCantThrow);
			e.setCancelled(true);
			player.getInventory().setItemInMainHand(pearl.createItemStack());
			return;
		}

		e.setCancelled(true);
		if (pearlApi.freePearl(pearl, PearlFreeReason.PEARL_THROWN)) {
			player.getInventory().setItemInMainHand(null);
			pearlApi.getPearlPlayer(player.getUniqueId()).msg(Lang.pearlYouFreed, pearl.getPlayerName());
		}
	}
	
	/**
	 * Prevent pearling with an exile pearl
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPearlThrow(ProjectileLaunchEvent e) {
		if (!(e.getEntity() instanceof EnderPearl)) {
			return;
		}
		
		final Player p = (Player)e.getEntity().getShooter();
		if (p == null) {
			return;
		}
		
		ExilePearl pearl = pearlApi.getPearlFromItemStack(p.getInventory().getItemInMainHand());
		if (pearl == null) {
			return;
		}
		
		pearlApi.getPearlPlayer(p.getUniqueId()).msg(Lang.pearlCantThrow);
		e.setCancelled(true);
		
		// Need to schedule this or else the re-created pearl doesn't show up
		Bukkit.getScheduler().scheduleSyncDelayedTask(pearlApi, new Runnable() {
			@Override
			public void run() {
				p.getInventory().setItemInMainHand(pearl.createItemStack());
			}
		});
	}


	/**
	 * Handles new prison pearl events
	 * @param e The event args
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerPearled(PlayerPearledEvent e) {

		PearlPlayer imprisoned = e.getPearl().getPlayer();
		PearlPlayer imprisoner = pearlApi.getPearlPlayer(e.getPearl().getKillerName());

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

		String bcastMsg = TextUtil.parse(Lang.pearlBroadcast, imprisoned.getName(), 
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


	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onPrepareCraftPearl(PrepareItemCraftEvent e) {
		CraftingInventory inv = e.getInventory();
		ItemStack result = inv.getResult();

		if (result.getType() != Material.STONE_BUTTON) {
			return;
		}

		if (result.getEnchantmentLevel(Enchantment.DURABILITY) != 1) {
			inv.setResult(new ItemStack(Material.AIR));
			return;
		}

		// Get the pearl item being crafted
		ItemStack pearlItem = inv.getItem(inv.first(Material.ENDER_PEARL));

		if (pearlItem == null) {
			inv.setResult(new ItemStack(Material.AIR));
			return;
		}

		ExilePearl pearl = pearlApi.getPearlFromItemStack(pearlItem);
		if (pearl == null) {
			inv.setResult(new ItemStack(Material.AIR));
			return;
		}

		// Ignore pearls that are at full health
		if (pearl.getHealth() == pearlApi.getPearlConfig().getPearlHealthMaxValue()) {
			inv.setResult(new ItemStack(Material.AIR));
			return;
		}
		
		ItemMap invItems = new ItemMap(inv);
		RepairMaterial repairItem = null;
		
		// Find the repair material that is being used for crafting
		for(RepairMaterial item : repairMaterials) {
			if (invItems.getAmount(item.getStack()) > 0) {
				repairItem = item;
				break;
			}
		}
		
		// Quit if no repair item was found
		if (repairItem == null) {
			inv.setResult(new ItemStack(Material.AIR));
			return;
		}
		
		// Get the total possible repair amount. This doesn't need to be limited
		// because the lore generator will cap at 100%
		int repairAmount = invItems.getAmount(repairItem.getStack()) * repairItem.getRepairAmount();

		// Generate a new item with the updated health value as the crafting result
		ItemStack resultStack = pearl.createItemStack();
		ItemMeta im = resultStack.getItemMeta();
		im.setLore(pearlApi.getLoreProvider().generateLoreWithModifiedHealth(pearl, pearl.getHealth() + repairAmount));
		resultStack.setItemMeta(im);

		e.getInventory().setResult(resultStack);
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onRecipeUpdate(InventoryClickEvent e) {
		InventoryView view = e.getView();
		if (view == null) {
			return;
		}
		
		if (!(view.getTopInventory() instanceof CraftingInventory)) {
			return;
		}
		CraftingInventory inv = (CraftingInventory)view.getTopInventory();
		ItemStack result = inv.getResult();
		if (result == null || result.getType() != Material.ENDER_PEARL) {
			return;
		}
		
		InventoryAction a = e.getAction();
		if (a != InventoryAction.PLACE_ONE && a != InventoryAction.PLACE_SOME && a != InventoryAction.PLACE_ALL) {
			return;
		}
		
		ExilePearl pearl = pearlApi.getPearlFromItemStack(result);
		if (pearl == null) {
			return;
		}
		
		ItemMap invItems = new ItemMap(inv);
		RepairMaterial repairItem = null;
		
		// Find the repair material that is being used for crafting
		for(RepairMaterial item : repairMaterials) {
			if (invItems.getAmount(item.getStack()) > 0) {
				repairItem = item;
				break;
			}
		}
		
		// Quit if no repair item was found
		if (repairItem == null) {
			inv.setResult(new ItemStack(Material.AIR));
			return;
		}
		
		// Get the total possible repair amount. This doesn't need to be limited
		// because the lore generator will cap at 100%
		int repairAmount = invItems.getAmount(repairItem.getStack()) * repairItem.getRepairAmount();
		pearlApi.log("Repair amount = %d", repairAmount);

		// Generate a new item with the updated health value as the crafting result
		ItemStack resultStack = pearl.createItemStack();
		ItemMeta im = resultStack.getItemMeta();
		im.setLore(pearlApi.getLoreProvider().generateLoreWithModifiedHealth(pearl, pearl.getHealth() + repairAmount));
		resultStack.setItemMeta(im);

		inv.setResult(resultStack);
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onCraftPearl(CraftItemEvent e) {
		CraftingInventory inv = e.getInventory();
		ItemStack result = inv.getResult();

		// Check if we are crafting an exile pearl
		ExilePearl pearl = pearlApi.getPearlFromItemStack(result);
		if (pearl == null) {
			return;
		}
		
		ItemMap invItems = new ItemMap(inv);
		RepairMaterial repairItem = null;
		
		// Find the repair material that is being used for crafting
		for(RepairMaterial item : repairMaterials) {
			if (invItems.getAmount(item.getStack()) > 0) {
				repairItem = item;
				break;
			}
		}
		
		// Quit if no repair items were found in the crafting inventory
		if (repairItem == null) {
			return;
		}

		int maxHealth = pearlApi.getPearlConfig().getPearlHealthMaxValue();
		int repairPerItem = repairItem.getRepairAmount();
		int repairMatsAvailable = invItems.getAmount(repairItem.getStack());
		int repairMatsToUse = Math.min((int)Math.ceil((maxHealth - pearl.getHealth()) / (double)repairPerItem), repairMatsAvailable);
		int repairAmount = repairMatsToUse * repairPerItem;

		// Take away the consumed repair materials
		ItemStack removeStack = repairItem.getStack().clone();
		removeStack.setAmount(repairMatsToUse);
		ItemMap removeItems = new ItemMap(removeStack);
		removeItems.addItemStack(pearl.createItemStack());
		removeItems.removeSafelyFrom(inv);
		inv.remove(Material.ENDER_PEARL);

		// Repair the pearl and update the item stack
		pearl.setHealth(pearl.getHealth() + repairAmount);
		inv.setResult(pearl.createItemStack());
		pearlApi.log("The pearl for player %s was repaired by %d points.", pearl.getPlayerName(), repairAmount);
	}
}
