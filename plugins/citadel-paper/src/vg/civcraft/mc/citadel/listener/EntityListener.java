package vg.civcraft.mc.citadel.listener;

import org.bukkit.inventory.ItemStack;
import static vg.civcraft.mc.citadel.Utility.explodeReinforcement;
import static vg.civcraft.mc.citadel.Utility.getRealBlock;
import static vg.civcraft.mc.citadel.Utility.maybeReinforcementDamaged;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelConfigManager;
import vg.civcraft.mc.citadel.PlayerState;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.database.GroupManagerDao;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;


public class EntityListener implements Listener{
	protected GroupManager gm = NameAPI.getGroupManager();
	private ReinforcementManager rm = Citadel.getReinforcementManager();

	@EventHandler(ignoreCancelled = true)
	public void explode(EntityExplodeEvent eee) {
		Iterator<Block> iterator = eee.blockList().iterator();
		List<Block> blocks = new ArrayList<Block>();
		while (iterator.hasNext()) {
			Block b = iterator.next();
			Block block = Utility.getRealBlock(b);
			//if it's a plant we want to check the reinforcement of the soil block
			if(Utility.isPlant(block)) {
				Block soilBlock = Utility.findPlantSoil(block);
				if(soilBlock != null && Citadel.getReinforcementManager().isReinforced(soilBlock)) {
					block.getDrops().clear();
					iterator.remove();
				}
			}
			// getRealBlock should return the block we care about so if its already in the list we know it is a double block and was already handled.
			if (blocks.contains(block)){
				block.getDrops().clear();
				iterator.remove();
				continue;
			}
			blocks.add(block);
			try {
				if (explodeReinforcement(block)) {
					block.getDrops().clear();
					iterator.remove();
				}
			} catch (NoClassDefFoundError e){
				Citadel.getInstance().getLogger().log(Level.WARNING, "Class Definition not found in explode", e);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void breakDoor(EntityBreakDoorEvent ebde) {
		ebde.setCancelled(maybeReinforcementDamaged(getRealBlock(ebde.getBlock())));
	}

	@EventHandler(ignoreCancelled = true)
	public void changeBlock(EntityChangeBlockEvent ecbe) {
		ecbe.setCancelled(maybeReinforcementDamaged(ecbe.getBlock()));
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void spawn(CreatureSpawnEvent cse) {
		ReinforcementManager reinforcementManager = Citadel.getReinforcementManager();
		EntityType type = cse.getEntityType();
		if (type != EntityType.IRON_GOLEM && type != EntityType.SNOWMAN && type != EntityType.WITHER && type != EntityType.SILVERFISH) return;

		for (Block block : getGolemBlocks(type, cse.getLocation().getBlock())) {
			Reinforcement reinforcement = reinforcementManager.getReinforcement(block);
			if (reinforcement != null) {
				cse.setCancelled(true);
			}
		}
	}

	private List<Block> getGolemBlocks(EntityType type, Block base) {
		ArrayList<Block> blocks = new ArrayList<Block>();
		blocks.add(base);
		base = base.getRelative(BlockFace.UP);
		blocks.add(base);
		if (type == EntityType.IRON_GOLEM) {
			for (BlockFace face : new BlockFace[]{ BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST }) {
				Block arm = base.getRelative(face);
				if (arm.getType() == Material.IRON_BLOCK)
					blocks.add(arm);
			}
		}
		base = base.getRelative(BlockFace.UP);
		blocks.add(base);

		return blocks;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerQuitEvent(PlayerQuitEvent event){
		Player p = event.getPlayer();
		PlayerState state = PlayerState.get(p);
		state.reset();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void hangingPlaceEvent(HangingPlaceEvent event) {
		// If Hanging Entity Reinforcements is not enabled, back out
		if (!CitadelConfigManager.hangersInheritReinforcements()) {
			return;
		}
		Reinforcement reinforcement = rm.getReinforcement(event.getBlock());
		// If no player reinforcement is present, do nothing
		if (!(reinforcement instanceof PlayerReinforcement)) {
			return;
		}
		PlayerReinforcement playerReinforcement = (PlayerReinforcement) reinforcement;
		Group group = playerReinforcement.getGroup();
		// If the player reinforcement doesn't have a group, do nothing
		if (group == null) {
			return;
		}
		// If the reinforcement is insecure, then allow the block placement
		if (playerReinforcement.isInsecure()) {
			return;
		}
		PermissionType permission = PermissionType.getPermission("REINFORCE");
		// If the REINFORCE permission is not registered, do nothing
		if (permission == null) {
			Citadel.getInstance().warning("Could not get the REINFORCE permission from NameLayer. Is it loaded?");
			return;
		}
		Player player = event.getPlayer();
		// If the player is a member of the group and has bypass permissions, do nothing
		if (gm.hasAccess(group, player.getUniqueId(), permission)) {
			return;
		}
		// Otherwise prevent the player from putting item frames on other people's reinforced blocks
		player.sendMessage(ChatColor.RED + "You cannot place those on blocks you don't have permissions for.");
		event.setCancelled(true);
		Bukkit.getScheduler().runTaskLater(Citadel.getInstance(), player::updateInventory, 1L);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void hangingEntityBreakEvent(HangingBreakByEntityEvent event) {
		// If Hanging Entity Reinforcements is not enabled, back out
		if (!CitadelConfigManager.hangersInheritReinforcements()) {
			return;
		}
		Hanging entity = event.getEntity();
		switch (event.getCause()) {
			// Allow it to break if:
			//  1) The host block broke
			//  2) A block was placed over it
			//  3) A plugin broke it
			case OBSTRUCTION:
			case PHYSICS:
			case DEFAULT:
				return;
			// Prevent break if breaker is player and does not have BYPASS permissions
			case ENTITY: {
				if (event.getRemover() instanceof Player) {
					Block host = entity.getLocation().getBlock().getRelative(entity.getAttachedFace());
					Reinforcement reinforcement = rm.getReinforcement(host.getLocation());
					// If the reinforcement doesn't exist or isn't a player reinforcement, we can safely back out
					// and let the entity be broken
					if (!(reinforcement instanceof PlayerReinforcement)) {
						return;
					}
					PlayerReinforcement playerReinforcement = (PlayerReinforcement) reinforcement;
					Group group = playerReinforcement.getGroup();
					// If the player reinforcement somehow does not have a group, just back out
					if (group == null) {
						return;
					}
					Player player = (Player) event.getRemover();
					// If the reinforcement is insecure, allow the break
					if (playerReinforcement.isInsecure()) {
						return;
					}
					// If the player has bypass permissions, allow the break
					if (playerReinforcement.canBypass(player)) {
						return;
					}
					// Otherwise prevent interaction and notify the player they do not have perms
					player.sendMessage(ChatColor.RED + "The host block is protecting this.");
				}
			}
		}
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerEntityInteractEvent(PlayerInteractEntityEvent event) {
		// If Hanging Entity Reinforcements is not enabled, back out
		if (!CitadelConfigManager.hangersInheritReinforcements()) {
			return;
		}
		// If the entity isn't a Item Frame, Painting, or LeashHitch, back out
		if (!(event.getRightClicked() instanceof Hanging)) {
			return;
		}
		Hanging entity = (Hanging) event.getRightClicked();
		Block host = entity.getLocation().getBlock().getRelative(entity.getAttachedFace());
		Reinforcement reinforcement = rm.getReinforcement(host.getLocation());
		// If no player reinforcement is present, do nothing
		if (!(reinforcement instanceof PlayerReinforcement)) {
			return;
		}
		PlayerReinforcement playerReinforcement = (PlayerReinforcement) reinforcement;
		Group group = playerReinforcement.getGroup();
		// If the player reinforcement doesn't have a group, do nothing
		if (group == null) {
			return;
		}
		Player player = event.getPlayer();
		// Item Frame specific behaviour
		if (entity instanceof ItemFrame) {
			// If the reinforcement is insecure, then allow all alterations
			if (playerReinforcement.isInsecure()) {
				return;
			}
			// If player can access doors, allow rotation alterations
			ItemStack heldItem = ((ItemFrame) entity).getItem();
			if (heldItem != null && heldItem.getType() != Material.AIR) {
				if (playerReinforcement.canAccessDoors(player)) {
					return;
				}
			}
		}
		// If the player has bypass permissions, allow all alterations
		if (playerReinforcement.canBypass(player)) {
			return;
		}
		// Otherwise prevent interaction and notify the player they do not have perms
		player.sendMessage(ChatColor.RED + "You do not have permission to alter that.");
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void entityDamageEvent(EntityDamageByEntityEvent event) {
		// If Hanging Entity Reinforcements is not enabled, back out
		if (!CitadelConfigManager.hangersInheritReinforcements()) {
			return;
		}
		// If the entity isn't a Item Frame, Painting, or LeashHitch, back out
		if (!(event.getEntity() instanceof Hanging)) {
			return;
		}
		Hanging entity = (Hanging) event.getEntity();
		// If the damager is not a player, prevent damage regardless
		if (!(event.getDamager() instanceof Player)) {
			event.setCancelled(true);
			return;
		}
		Block host = entity.getLocation().getBlock().getRelative(entity.getAttachedFace());
		Reinforcement reinforcement = rm.getReinforcement(host.getLocation());
		// If the reinforcement doesn't exist or isn't a player reinforcement, we can safely back out
		// and let the entity be broken
		if (!(reinforcement instanceof PlayerReinforcement)) {
			return;
		}
		PlayerReinforcement playerReinforcement = (PlayerReinforcement) reinforcement;
		Group group = playerReinforcement.getGroup();
		// If the player reinforcement somehow does not have a group, just back out
		if (group == null) {
			return;
		}
		// If the hanging entity is an item frame and it holds an item and the reinforcement is
		// insecure, then allow the item frame to be damaged, which will drop the item.
		if (entity instanceof ItemFrame) {
			if (playerReinforcement.isInsecure()) {
				ItemStack heldItem = ((ItemFrame) entity).getItem();
				if (heldItem != null && heldItem.getType() != Material.AIR) {
					return;
				}
			}
		}
		Player player = (Player) event.getDamager();
		// If the player is a member of the group and has bypass permissions, do nothing
		if (playerReinforcement.canBypass(player)) {
			return;
		}
		// Otherwise prevent interaction and notify the player they do not have perms
		player.sendRawMessage(ChatColor.RED + "The host block is protecting this.");
		event.setCancelled(true);
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void playerJoinEvent(PlayerJoinEvent event){
		Player p = event.getPlayer();
		final UUID uuid = p.getUniqueId();

		new BukkitRunnable() {
			@Override
			public void run() {
				GroupManagerDao db = NameLayerPlugin.getGroupManagerDao();
				for (String groupName : db.getGroupNames(uuid)){
					if(NameAPI.getGroupManager().hasAccess(groupName, uuid, PermissionType.getPermission("REINFORCE"))) {
						db.updateTimestamp(groupName);
					}
				}
			}
		}.runTaskAsynchronously(Citadel.getInstance());

		if (CitadelConfigManager.defaultBypassOn()) {
			PlayerState state = PlayerState.get(p);
			if (!state.isBypassMode()) {
				state.toggleBypassMode();
			}
		}
	}
}
