package isaac.bastion.manager;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dispenser;

import isaac.bastion.BastionBlock;
import isaac.bastion.BastionType;
import isaac.bastion.storage.BastionBlockSet;
import isaac.bastion.util.QTBox;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class BastionBlockManager {
	public BastionBlockSet set;
	private HashMap<UUID, HashMap<BastionType, Long>> cooldowns = new HashMap<UUID, HashMap<BastionType, Long>>();
	private static Random generator = new Random();

	public BastionBlockManager() {
		set = new BastionBlockSet();
		set.load();
	}
	
	public void close() {
		set.close();
	}

	public void addBastion(Location location, PlayerReinforcement reinforcement, BastionType type) {
		BastionBlock toAdd = new BastionBlock(location, reinforcement, type);
		set.add(toAdd);
	}
	
	public void erodeFromPlace(UUID player, Set<BastionBlock> blocking) {
		erodeFromAction(player, blocking, true);
	}
	
	public void erodeFromTeleport(UUID player, Set<BastionBlock> blocking){
		erodeFromAction(player, blocking, false);
	}

	/**
	 * Common handler for erosion.
	 */
	private void erodeFromAction(UUID player, Set<BastionBlock> blocking, boolean fromBlock) {
		HashMap<BastionType, Set<BastionBlock>> typeMap = new HashMap<BastionType, Set<BastionBlock>>();
		for(BastionBlock block : blocking) {
			if(!typeMap.containsKey(block.getType())) {
				typeMap.put(block.getType(), new HashSet<BastionBlock>());
			}
			typeMap.get(block.getType()).add(block);
		}
		
		for(BastionType type : typeMap.keySet()) {
			if(onCooldown(player, type)) continue;
			Set<BastionBlock> bastions = typeMap.get(type);
			double damage = fromBlock ? type.getBlocksToErode() : type.getBlocksToErode() * type.getPearlScaleFactor();
			if(type.getBlocksToErode() < 0) {
				for(BastionBlock bastion : bastions) {
					bastion.erode(damage);
				}
			} else {
				List<BastionBlock> ordered = new LinkedList<BastionBlock>(blocking);
				for(int i = 0; i < ordered.size() && i < type.getBlocksToErode(); ++i) {
					int erode = generator.nextInt(ordered.size());
					BastionBlock toErode = ordered.get(erode);
					toErode.erode(damage);
					ordered.remove(erode);
				}
			}
		}
	}
	
	public boolean onCooldown(UUID player, BastionType type){
		if(!cooldowns.containsKey(player)) {
			cooldowns.put(player, new HashMap<BastionType, Long>());
			return false;
		}
		if(!cooldowns.get(player).containsKey(type)) {
			cooldowns.get(player).put(type, System.currentTimeMillis());
			return false;
		}
		long last_placed = cooldowns.get(player).get(type);

		if ((System.currentTimeMillis() - last_placed) < type.getPlacementCooldown()) {
			return true;
		} else {
			cooldowns.get(player).put(type, System.currentTimeMillis());
		}
		
		return false;
	}
	

	/** 
	 * handles all block based events in a general way
	 * @param origin
	 * @param result
	 * @param player
	 * @return
	 */
	public Set<BastionBlock> shouldStopBlock(Block origin, Set<Block> result, UUID player) {
		if (player != null) {
			Player playerB = Bukkit.getPlayer(player);
			if (playerB != null && playerB.hasPermission("Bastion.bypass")) return new CopyOnWriteArraySet<BastionBlock>();
		}
		
		Set<BastionBlock> toReturn = new HashSet<BastionBlock>();
		Set<UUID> accessors = new HashSet<UUID>();
		if (player != null) {
			accessors.add(player);
		}
		
		if (origin != null) {
			PlayerReinforcement reinforcement = (PlayerReinforcement) Citadel.getReinforcementManager().
			getReinforcement(origin);
			if (reinforcement instanceof PlayerReinforcement) {
				accessors.add(reinforcement.getGroup().getOwner());
			}
			
			for (BastionBlock bastion: this.getBlockingBastions(origin.getLocation())) {
				accessors.add(bastion.getOwner());
			}
		}
		
		for(Block block: result) {
			toReturn.addAll(getBlockingBastions(block.getLocation(),accessors));
		}
		
		return toReturn;
	}

	// TODO: This is potentially inefficient: new LL, plus shuffle, all to "random-choose" a bastion?
	//   Evaluable if forLocation returns a new Set; if so, just directly mess with the set.
	private BastionBlock getBlockingBastion(Location loc, Player player) {
		Set<? extends QTBox> possible = set.forLocation(loc);

		@SuppressWarnings("unchecked")
		List<BastionBlock> possibleRandom = new LinkedList<BastionBlock>((Set<BastionBlock>)possible);
		Collections.shuffle(possibleRandom);

		for (BastionBlock bastion : possibleRandom) {
			if (!bastion.canPlace(player) && bastion.inField(loc)) {
				return bastion;
			}
		}
		return null;
	}

	public BastionBlock getBlockingBastion(Location loc) {
		Set<? extends QTBox> possible = set.forLocation(loc);

		@SuppressWarnings("unchecked")
		List<BastionBlock> possibleRandom = new LinkedList<BastionBlock>((Set<BastionBlock>)possible);
		Collections.shuffle(possibleRandom);

		for (BastionBlock bastion : possibleRandom) {
			if (bastion.inField(loc)) {
				return bastion;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public Set<BastionBlock> getBlockingBastions(Location loc){
		Set<? extends QTBox> boxes = set.forLocation(loc);
		Set<BastionBlock> bastions = null;
		
		if(boxes.size() > 0 && boxes.iterator().next() instanceof BastionBlock) {
			bastions = (Set<BastionBlock>) boxes;
		}

		if (bastions == null) {
			return new CopyOnWriteArraySet<BastionBlock>();
		}
		
		Iterator<BastionBlock> i = bastions.iterator();
		
		while (i.hasNext()){
			BastionBlock bastion = i.next();
			if (!bastion.inField(loc)){
				i.remove();
			}
		};
		return bastions;
	}
	
	@SuppressWarnings("unchecked")
	private Set<BastionBlock> getBlockingBastions(Location loc, Player player, PermissionType perm){
		Set<? extends QTBox> boxes = set.forLocation(loc);
		Set<BastionBlock> bastions = null;
		
		if (boxes.size() > 0 && boxes.iterator().next() instanceof BastionBlock) {
			bastions = (Set<BastionBlock>) boxes;
		}

		if (bastions == null) {
			return new CopyOnWriteArraySet<BastionBlock>();
		}
		
		Iterator<BastionBlock> i = bastions.iterator();
		while (i.hasNext()) {
			BastionBlock bastion = i.next();
			if (!bastion.inField(loc) || bastion.permAccess(player, perm)) {
				i.remove();
			}
		}
		
		return bastions;
	}
	
	@SuppressWarnings("unchecked")
	private Set<BastionBlock> getBlockingBastions(Location loc, Set<UUID> players) {
		Set<? extends QTBox> boxes = set.forLocation(loc);
		Set<BastionBlock> bastions = null;
		
		if (boxes.size() != 0) {
			bastions = (Set<BastionBlock>) boxes;
		}

		if (bastions == null) {
			return new CopyOnWriteArraySet<BastionBlock>();
		}
		
		Iterator<BastionBlock> i = bastions.iterator();
		while (i.hasNext()) {
			BastionBlock bastion = i.next();
			if (!bastion.inField(loc) || bastion.oneCanPlace(players)) {
				i.remove();
			}
		}
		
		return bastions;
	}
	
	public String infoMessage(boolean dev, Block block, Block clicked, Player player) {
		BastionBlock bastion = set.getBastionBlock(clicked.getLocation()); //Get the bastion at the location clicked.

		if (bastion != null) { //See if anything was found
			return bastion.infoMessage(dev, player); //If there is actually something there tell the player about it.
		}

		StringBuilder sb = new StringBuilder();

		bastion = getBlockingBastion(block.getLocation(), player);
		if (bastion == null) {
			bastion = getBlockingBastion(block.getLocation());
			if (bastion != null) {
				sb.append(ChatColor.GREEN).append("A Bastion Block prevents others from building");
			} else {
				sb.append(ChatColor.YELLOW).append("No Bastion Block");
			}
		} else {
			sb.append(ChatColor.RED).append("A Bastion Block prevents you building");
		}

		if (dev && bastion != null) {
			sb.append(ChatColor.BLACK).append("\n").append(bastion.toString());
		}

		return sb.toString();
	}

	public void handleBlockPlace(BlockPlaceEvent event) {
		Set<Block> blocks = new CopyOnWriteArraySet<Block>();
		blocks.add(event.getBlock());
		Set<BastionBlock> blocking = shouldStopBlock(null, blocks,event.getPlayer().getUniqueId());
		
		if (blocking.size() != 0){
			erodeFromPlace(event.getPlayer().getUniqueId(),blocking);
			
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "Bastion removed block");
		}
	}

	public void handleFlowingWater(BlockFromToEvent event) {
		Set<Block> blocks = new CopyOnWriteArraySet<Block>();
		blocks.add(event.getToBlock());
		Set<BastionBlock> blocking = shouldStopBlock(event.getBlock(),blocks, null);
		
		if(blocking.size() != 0){
			event.setCancelled(true);
		}
	}
	
	public void handleTreeGrowth(StructureGrowEvent event) {
		HashSet<Block> blocks = new HashSet<Block>();
		for(BlockState state: event.getBlocks()) {
			blocks.add(state.getBlock());
		}
		
		Player player = event.getPlayer();
		UUID playerName = null;
		if (player != null) {
			playerName = player.getUniqueId();
		}
		
		Set<BastionBlock> blocking = shouldStopBlock(event.getLocation().getBlock(), blocks, playerName);
		
		if (blocking.size() != 0) {
			event.setCancelled(true);
		}
	}
	
	public void handlePistonPush(BlockPistonExtendEvent event) {
		Block pistion = event.getBlock();
		Set<Block> involved = new HashSet<Block>(event.getBlocks());
		involved.add(pistion.getRelative(event.getDirection()));
		
		Set<BastionBlock> blocking = shouldStopBlock(pistion, involved, null);
		
		if (blocking.size() != 0) {
			event.setCancelled(true);
		}
	}
	
	public void handleBucketPlace(PlayerBucketEmptyEvent event) {
		Set<Block> blocks = new HashSet<Block>();
		blocks.add(event.getBlockClicked().getRelative(event.getBlockFace()));
		
		Set<BastionBlock> blocking = shouldStopBlock(null, blocks, event.getPlayer().getUniqueId());
		
		if (blocking.size() != 0) {
			event.setCancelled(true);
		}
	}
	
	public void handleDispensed(BlockDispenseEvent event) {
		if (!(event.getItem().getType() == Material.WATER_BUCKET || event.getItem().getType() == Material.LAVA_BUCKET || event.getItem().getType() == Material.FLINT_AND_STEEL)) return;
		
		Set<Block> blocks = new HashSet<Block>();
		blocks.add(event.getBlock().getRelative( ((Dispenser) event.getBlock().getState().getData()).getFacing()));
		
		Set<BastionBlock> blocking = shouldStopBlock(event.getBlock(),blocks, null);
		
		if(blocking.size() != 0) {
			event.setCancelled(true);
		}
	}
	
	public void handleBlockBreakEvent(BlockBreakEvent event) {
		BastionBlock bastion = set.getBastionBlock(event.getBlock().getLocation());
		if (bastion != null) {
			bastion.close();
		}
	}
	
	public void handleEnderPearlLanded(PlayerTeleportEvent event) {
		if (event.getPlayer().hasPermission("Bastion.bypass")) return; //I'm not totally sure about the implications of this combined with humbug. It might cause some exceptions. Bukkit will catch.
		if (event.getCause() != TeleportCause.ENDER_PEARL) return; // Only handle enderpearl cases
		
		Set<BastionBlock> blocking = getBlockingBastions(event.getTo(), event.getPlayer(), PermissionType.getPermission("BASTION_PEARL"));
		
		Iterator<BastionBlock> i = blocking.iterator();
		
		while(i.hasNext()) {
			BastionBlock bastion = i.next();
			if(bastion.getType().isBlockMidair() || (bastion.getType().isRequireMaturity() && !bastion.isMature())) {
				i.remove();
			}
		}
		
		if(blocking.size() > 0) {
			erodeFromTeleport(event.getPlayer().getUniqueId(), blocking);
			event.getPlayer().sendMessage(ChatColor.RED + "Ender pearl blocked by Bastion Block");
			boolean consume = false;
			for(BastionBlock block : blocking) {
				if(block.getType().isConsumeOnBlock()) {
					consume = true;
				}
			}
			if(!consume) {
				event.getPlayer().getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
			}
			event.setCancelled(true);
			return;
		}
		
		blocking = getBlockingBastions(event.getFrom(), event.getPlayer(), PermissionType.getPermission("BASTION_PEARL"));
		
		i = blocking.iterator();
		
		while(i.hasNext()) {
			BastionBlock bastion = i.next();
			if(bastion.getType().isBlockMidair() || (bastion.getType().isRequireMaturity() && !bastion.isMature())) {
				i.remove();
			}
		}
		
		if(blocking.size() > 0) {
			erodeFromTeleport(event.getPlayer().getUniqueId(), blocking);
			event.getPlayer().sendMessage(ChatColor.RED + "Ender pearl blocked by Bastion Block");
			boolean consume = false;
			for(BastionBlock block : blocking) {
				if(block.getType().isConsumeOnBlock()) {
					consume = true;
				}
			}
			if(!consume) {
				event.getPlayer().getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
			}
			event.setCancelled(true);
			return;
		}
	}
}
