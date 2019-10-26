package vg.civcraft.mc.citadel.listener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.database.GroupManagerDao;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class EntityListener implements Listener {
	protected GroupManager gm = NameAPI.getGroupManager();

	// prevent zombies from breaking reinforced doors
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void breakDoor(EntityBreakDoorEvent ebde) {
		Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(ebde.getBlock());
		if (rein != null) {
			ReinforcementLogic.damageReinforcement(rein, ReinforcementLogic.getDamageApplied(rein), ebde.getEntity());
			if (!rein.isBroken()) {
				ebde.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void changeBlock(EntityChangeBlockEvent ecbe) {
		Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(ecbe.getBlock());
		if (rein != null) {
			ReinforcementLogic.damageReinforcement(rein, ReinforcementLogic.getDamageApplied(rein), ecbe.getEntity());
			if (!rein.isBroken()) {
				ecbe.setCancelled(true);
			}
		}
	}

	// apply explosion damage to reinforcements
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void explode(EntityExplodeEvent eee) {
		Iterator<Block> iterator = eee.blockList().iterator();
		// we can edit the result by removing blocks from the list
		while (iterator.hasNext()) {
			Block block = iterator.next();
			Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(block);
			if (rein == null) {
				continue;
			}
			ReinforcementLogic.damageReinforcement(rein, ReinforcementLogic.getDamageApplied(rein), eee.getEntity());
			if (!rein.isBroken()) {
				iterator.remove();
			}
		}
	}

	private List<Block> getGolemBlocks(EntityType type, Block base) {
		ArrayList<Block> blocks = new ArrayList<>();
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

	@EventHandler(priority = EventPriority.HIGHEST)
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
						GroupManager.getGroup(groupName).updateActivityTimeStamp();
					}
				}
			}
		}.runTaskAsynchronously(Citadel.getInstance());
	}

	// prevent creating golems from reinforced blocks
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void spawn(CreatureSpawnEvent cse) {
		EntityType type = cse.getEntityType();
		if (type != EntityType.IRON_GOLEM && type != EntityType.SNOWMAN && type != EntityType.WITHER
				&& type != EntityType.SILVERFISH) {
			return;
		}
		for (Block block : getGolemBlocks(type, cse.getLocation().getBlock())) {
			Reinforcement reinforcement = Citadel.getInstance().getReinforcementManager().getReinforcement(block);
			if (reinforcement != null) {
				cse.setCancelled(true);
			}
		}
	}
}
