package vg.civcraft.mc.citadel.listener;

import static vg.civcraft.mc.citadel.Utility.createNaturalReinforcement;
import static vg.civcraft.mc.citadel.Utility.createPlayerReinforcement;
import static vg.civcraft.mc.citadel.Utility.explodeReinforcement;
import static vg.civcraft.mc.citadel.Utility.maybeReinforcementDamaged;
import static vg.civcraft.mc.citadel.Utility.reinforcementBroken;
import static vg.civcraft.mc.citadel.Utility.reinforcementDamaged;
import static vg.civcraft.mc.citadel.Utility.timeUntilAcidMature;
import static vg.civcraft.mc.citadel.Utility.timeUntilMature;
import static vg.civcraft.mc.citadel.Utility.wouldPlantDoubleReinforce;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.ContainerBlock;
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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelConfigManager;
import vg.civcraft.mc.citadel.PlayerState;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.ReinforcementMode;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.citadel.events.ReinforcementDamageEvent;
import vg.civcraft.mc.citadel.misc.ReinforcemnetFortificationCancelException;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.events.PromotePlayerEvent;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
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
				Block soilBlock = block.getRelative(BlockFace.DOWN);
				if(Citadel.getReinforcementManager().isReinforced(soilBlock)) {
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
		ebde.setCancelled(maybeReinforcementDamaged(ebde.getBlock()));
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
	public void PlayerPromoteEvent(PromotePlayerEvent event){
		Player p = event.getPlayer();
		Group g = event.getGroup();
		PlayerType currentType = event.getCurrentPlayerType();
		PlayerType futureType = event.getFuturePlayerType();
		PlayerState state = PlayerState.get(p);
		GroupPermission gPerm = gm.getPermissionforGroup(g);
		if(state.getMode() == ReinforcementMode.NORMAL){
			//player is in NORMAL mode don't need to do anything
			return;
		}
		if(gPerm.isAccessible(currentType, PermissionType.BLOCKS)){
			//see if they can access blocks current state
			if (!gPerm.isAccessible(futureType, PermissionType.BLOCKS)){
				//if they will no longer be able to access blocks
				Group citadelGroup = state.getGroup();
				if(citadelGroup.getName() == g.getName()){
					//Player is Actively Reinforcing in that group
					state.reset();
					String msg = "Your PlayerType in group (" + g.getName() + ") changed you no longer have access to reinforce/bypass";
					p.sendMessage(ChatColor.RED + msg);
					p.sendMessage(ChatColor.GREEN + "Your mode has been set to " + 
							ReinforcementMode.REINFORCEMENT.name() + ".");
				}


			}
		}
	}


	@EventHandler(priority = EventPriority.HIGHEST)
	public void hangingPlaceEvent(HangingPlaceEvent event){
		Player p = event.getPlayer();
		Block b = event.getBlock().getRelative(event.getBlockFace());
		Inventory inv = p.getInventory();
		PlayerState state = PlayerState.get(p);
		if (ReinforcementMode.REINFORCEMENT_FORTIFICATION != state.getMode()) {
			return;
		}
		if (!canPlace(b, p)){
			sendAndLog(p, ChatColor.RED, "Cancelled block place, mismatched reinforcement.");
			event.setCancelled(true);
			return;
		}
		ReinforcementType type = state.getReinforcementType();
		// Don't allow double reinforcing reinforceable plants
		if (wouldPlantDoubleReinforce(b)) {
			sendAndLog(p, ChatColor.RED, "Cancelled block place, crop would already be reinforced.");
			event.setCancelled(true);
			return;
		}
		int required = type.getRequiredAmount();
		if (type.getItemStack().isSimilar(p.getItemInHand())){
			required++;
		}
		if (inv.containsAtLeast(type.getItemStack(), required)) {
			try {
				if (createPlayerReinforcement(p, state.getGroup(), b, type, p.getItemInHand()) == null) {
					sendAndLog(p, ChatColor.RED, String.format("%s is not a reinforcible material ", b.getType().name()));
				} else {
					state.checkResetMode();
				}	
			} catch(ReinforcemnetFortificationCancelException ex){
				Citadel.getInstance().getLogger().log(Level.WARNING, "ReinforcementFortificationCancelException occured in BlockListener, BlockPlaceEvent ", ex);
			}
		} else {
			sendAndLog(p, ChatColor.YELLOW, String.format("%s depleted, left fortification mode ",  
					state.getReinforcementType().getMaterial().name()));
			state.reset();
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void hangingEntityBreakEvent(HangingBreakByEntityEvent event){
		Reinforcement rein = rm.getReinforcement(event.getEntity().getLocation()); if (rein == null){return;}
		if (RemoveCause.PHYSICS.equals(event.getCause())){
			//Checks if block entity was attached to was broken
			if (event.getEntity().getLocation().getBlock().getRelative(
					event.getEntity().getAttachedFace()).getType().equals(Material.AIR)){
				//Comment out these next two lines to keep floating hanging entities if they are reinforced
				rm.deleteReinforcement(rein);
				return;
			} else {
				event.setCancelled(true);
				return;
			}
		}
		Entity remover = event.getRemover(); if (!(remover instanceof Player)){event.setCancelled(true);return;}
		Player player = (Player)remover;
		Block block = event.getEntity().getLocation().getBlock();
		boolean is_cancelled = true;
		if (rein instanceof PlayerReinforcement) {
			PlayerReinforcement pr = (PlayerReinforcement) rein;
			PlayerState state = PlayerState.get(player);
			ReinforcementMode mode = state.getMode();
			if (ReinforcementMode.REINFORCEMENT_INFORMATION.equals(mode)){
				Group group = pr.getGroup();
				StringBuilder sb;
				if (player.hasPermission("citadel.admin.ctinfodetails")) {
					sendAndLog(player, ChatColor.GREEN, String.format(
							"Loc[%s]", pr.getLocation().toString()));
					String groupName = "!NULL!";
					if (group != null) {
						groupName = String.format("[%s] (%s)", group.getName(),
								group.getType().name());
					}
					sb = new StringBuilder();
					sb.append(String.format(" Group%s Durability[%d/%d]",
							groupName,
							pr.getDurability(),
							ReinforcementType.getReinforcementType
							(pr.getStackRepresentation()).getHitPoints()));
					int maturationTime = timeUntilMature(pr);
					if (maturationTime != 0) {
						sb.append(" Immature[");
						sb.append(maturationTime);
						sb.append("]");
					}
					int acidTime = timeUntilAcidMature(pr);
					if (CitadelConfigManager.getAcidBlock() == block.getType()) {
						sb.append(" Acid ");
						if (acidTime != 0) {
							sb.append("Immature[");
							sb.append(acidTime);
							sb.append("]");
						} else {
							sb.append("Mature");
						}
					}
					if (pr.isInsecure()) {
						sb.append(" (Insecure)");
					}
					if (group.isDisciplined()) {
						sb.append(" (Disciplined)");
					}
					sb.append("\nGroup id: " + pr.getGroupId());

					sendAndLog(player, ChatColor.GREEN, sb.toString());
					event.setCancelled(is_cancelled);
					return;
				}
			}
			boolean admin_bypass = player.hasPermission("citadel.admin.bypassmode");   
			if (state.isBypassMode() && (pr.isBypassable(player) || admin_bypass) && !pr.getGroup().isDisciplined()) {
				reinforcementBroken(player, rein);
				is_cancelled = false;
			} else {

				ReinforcementDamageEvent dre = new ReinforcementDamageEvent(rein, player, event.getEntity().getLocation().getBlock());

				Bukkit.getPluginManager().callEvent(dre);

				if(dre.isCancelled()) {
					is_cancelled = true;
				}
				else {
					is_cancelled = reinforcementDamaged(player, rein);
				}
			}
			if (!is_cancelled) {
				// The player reinforcement broke. Now check for natural
				is_cancelled = createNaturalReinforcement(block, player) != null;
			}
		} else {
			ReinforcementDamageEvent dre = new ReinforcementDamageEvent(rein, player, block);

			Bukkit.getPluginManager().callEvent(dre);

			if(dre.isCancelled()) {
				is_cancelled = reinforcementDamaged(player, rein);
				return;
			}
			else {
				is_cancelled = reinforcementDamaged(player, rein);
			}
		}

		if (is_cancelled) {
			event.setCancelled(true);
			block.getDrops().clear();
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void hangingBreakEvent(HangingBreakEvent event){
		Reinforcement rein = rm.getReinforcement(event.getEntity().getLocation()); if (rein == null){return;}
		if (RemoveCause.PHYSICS.equals(event.getCause())){
			//Checks if block entity was attached to was broken
			if (event.getEntity().getLocation().getBlock().getRelative(
					event.getEntity().getAttachedFace()).getType().equals(Material.AIR)){
				//Comment out these next two lines to keep floating hanging entities if they are reinforced
				rm.deleteReinforcement(rein);
				return;
			}
		}
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerEntityInteractEvent(PlayerInteractEntityEvent event){
		Entity entity = event.getRightClicked();
		if (entity instanceof ItemFrame){
			Reinforcement rein = rm.getReinforcement(entity.getLocation());
			if (rein == null || !(rein instanceof PlayerReinforcement))
				return;
			PlayerReinforcement pr = (PlayerReinforcement)rein; 
			Group group = pr.getGroup(); if (group == null){return;}

			if (group.isMember(event.getPlayer().getUniqueId()) == false){
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void entityDamageEvent(EntityDamageByEntityEvent event){
		Entity entity = event.getEntity();
		if (entity instanceof ItemFrame){
			Reinforcement rein = rm.getReinforcement(entity.getLocation());
			if (rein == null || !(rein instanceof PlayerReinforcement))
				return;
			Entity damager = event.getDamager(); if (!(damager instanceof Player)){event.setCancelled(true);return;}
			Player player = (Player)damager;
			PlayerReinforcement pr = (PlayerReinforcement)rein; 
			Group group = pr.getGroup(); if (group == null){return;}

			if (group.isMember(player.getUniqueId()) == false){
				event.setCancelled(true);
				return;
			}
		}
	}

	private boolean canPlace(Block block, Player player) {
		Material block_mat = block.getType();

		if (block_mat == Material.HOPPER || block_mat == Material.DROPPER){
			for (BlockFace direction : BlockListener.all_sides) {
				Block adjacent = block.getRelative(direction);
				if (!(adjacent.getState() instanceof ContainerBlock)) {
					continue;
				}
				Reinforcement rein = rm.getReinforcement(adjacent);
				if (null != rein && rein instanceof PlayerReinforcement) {
					PlayerReinforcement pr = (PlayerReinforcement)rein;
					if (pr.isInsecure() && !pr.isAccessible(player, PermissionType.CHESTS)) {
						return false;
					}
				}
			}
		}
		if (block_mat == Material.CHEST || block_mat == Material.TRAPPED_CHEST){
			for (BlockFace direction : BlockListener.planar_sides) {
				Block adjacent = block.getRelative(direction);
				if (!(adjacent.getState() instanceof ContainerBlock)) {
					continue;
				}
				Reinforcement rein = rm.getReinforcement(adjacent);
				if (null != rein && rein instanceof PlayerReinforcement) {
					PlayerReinforcement pr = (PlayerReinforcement)rein;
					if (!pr.isAccessible(player, PermissionType.CHESTS)) {
						return false;
					}
				}
			}
		}
		//stops players from modifying the reinforcement on a half slab by placing another block on top
		Reinforcement reinforcement_on_block = Citadel.getReinforcementManager().getReinforcement(block);
		if (reinforcement_on_block instanceof PlayerReinforcement) {
			PlayerReinforcement reinforcement = (PlayerReinforcement) reinforcement_on_block;
			if (!reinforcement.isBypassable(player)) {
				return false;
			}
		} else if (reinforcement_on_block != null) {
			return false; //not really sure when this could happen but just in case
		}

		return true;
	}

	protected void sendAndLog(Player receiver, ChatColor color, String message) {
		receiver.sendMessage(color + message);
		if (CitadelConfigManager.shouldLogPlayerCommands()) {
			Citadel.getInstance().getLogger().log(Level.INFO, "Sent {0} reply {1}", new Object[]{receiver.getName(), message});
		}
	}
}
