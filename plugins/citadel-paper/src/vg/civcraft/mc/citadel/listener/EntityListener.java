package vg.civcraft.mc.citadel.listener;

import static vg.civcraft.mc.citadel.Utility.canPlace;
import static vg.civcraft.mc.citadel.Utility.createNaturalReinforcement;
import static vg.civcraft.mc.citadel.Utility.createPlayerReinforcement;
import static vg.civcraft.mc.citadel.Utility.explodeReinforcement;
import static vg.civcraft.mc.citadel.Utility.getRealBlock;
import static vg.civcraft.mc.citadel.Utility.maybeReinforcementDamaged;
import static vg.civcraft.mc.citadel.Utility.reinforcementBroken;
import static vg.civcraft.mc.citadel.Utility.reinforcementDamaged;
import static vg.civcraft.mc.citadel.Utility.timeUntilAcidMature;
import static vg.civcraft.mc.citadel.Utility.timeUntilMature;
import static vg.civcraft.mc.citadel.Utility.wouldPlantDoubleReinforce;

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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelWorldManager;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.ReinforcementMode;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.citadel.events.ReinforcementDamageEvent;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.database.GroupManagerDao;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class EntityListener implements Listener {
	protected GroupManager gm = NameAPI.getGroupManager();
	private ReinforcementManager rm = Citadel.getReinforcementManager();

	@EventHandler(ignoreCancelled = true)
	public void explode(EntityExplodeEvent eee) {
		Iterator<Block> iterator = eee.blockList().iterator();
		List<Block> blocks = new ArrayList<Block>();
		while (iterator.hasNext()) {
			Block b = iterator.next();
			Block block = Utility.getRealBlock(b);
			// if it's a plant we want to check the reinforcement of the soil block
			if (Utility.isPlant(block)) {
				Block soilBlock = Utility.findPlantSoil(block);
				if (soilBlock != null && Citadel.getReinforcementManager().isReinforced(soilBlock)) {
					block.getDrops().clear();
					iterator.remove();
				}
			}
			// getRealBlock should return the block we care about so if its already in the
			// list we know it is a double block and was already handled.
			if (blocks.contains(block)) {
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
			} catch (NoClassDefFoundError e) {
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

	// prevent creating golems from reinforced blocks
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void spawn(CreatureSpawnEvent cse) {
		CitadelWorldManager reinforcementManager = Citadel.getInstance().getReinforcementManager();
		EntityType type = cse.getEntityType();
		if (type != EntityType.IRON_GOLEM && type != EntityType.SNOWMAN && type != EntityType.WITHER
				&& type != EntityType.SILVERFISH) {
			return;
		}
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
			for (BlockFace face : new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST,
					BlockFace.WEST }) {
				Block arm = base.getRelative(face);
				if (arm.getType() == Material.IRON_BLOCK)
					blocks.add(arm);
			}
		}
		base = base.getRelative(BlockFace.UP);
		blocks.add(base);

		return blocks;
	}

	// @EventHandler(priority = EventPriority.HIGHEST)
	public void playerEntityInteractEvent(PlayerInteractEntityEvent event) {
		Entity entity = event.getRightClicked();
		if (entity instanceof ItemFrame) {
			Reinforcement rein = rm.getReinforcement(entity.getLocation());
			if (rein == null || !(rein instanceof PlayerReinforcement))
				return;
			PlayerReinforcement pr = (PlayerReinforcement) rein;
			Group group = pr.getGroup();
			if (group == null) {
				return;
			}

			if (group.isMember(event.getPlayer().getUniqueId()) == false) {
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerJoinEvent(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		final UUID uuid = p.getUniqueId();

		new BukkitRunnable() {
			@Override
			public void run() {
				GroupManagerDao db = NameLayerPlugin.getGroupManagerDao();
				for (String groupName : db.getGroupNames(uuid)) {
					if (NameAPI.getGroupManager().hasAccess(groupName, uuid,
							PermissionType.getPermission("REINFORCE"))) {
						db.updateTimestamp(groupName);
					}
				}
			}
		}.runTaskAsynchronously(Citadel.getInstance());

		if (OldCitadelConfigManager.defaultBypassOn()) {
			PlayerState state = PlayerState.get(p);
			if (!state.isBypassMode()) {
				state.toggleBypassMode();
			}
		}
	}
}
