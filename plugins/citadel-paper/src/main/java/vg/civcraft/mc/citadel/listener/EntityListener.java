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
import vg.civcraft.mc.citadel.model.GlobalReinforcementManager;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.database.GroupManagerDao;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class EntityListener implements Listener {
	protected GroupManager gm = NameAPI.getGroupManager();

	//apply explosion damage to reinforcements
	@EventHandler(ignoreCancelled = true)
	public void explode(EntityExplodeEvent eee) {
		Iterator<Block> iterator = eee.blockList().iterator();
		//we can edit the result by removing blocks from the list
		while (iterator.hasNext()) {
			Block block = iterator.next();
			Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(block);
			if (rein != null) {
				rein.setHealth(rein.getHealth() - 1);
				iterator.remove();
			}
		}
	}

	//prevent zombies from breaking reinforced doors
	@EventHandler(ignoreCancelled = true)
	public void breakDoor(EntityBreakDoorEvent ebde) {
		Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(ebde.getBlock());
		if (rein != null) {
			ebde.setCancelled(true);
			ReinforcementLogic.damageReinforcement(rein, 1.0);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void changeBlock(EntityChangeBlockEvent ecbe) {
		Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(ecbe.getBlock());
		if (rein != null) {
			ecbe.setCancelled(true);
			ReinforcementLogic.damageReinforcement(rein, 1.0);
		}
	}

	// prevent creating golems from reinforced blocks
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void spawn(CreatureSpawnEvent cse) {
		GlobalReinforcementManager reinforcementManager = Citadel.getInstance().getReinforcementManager();
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
	}
}
