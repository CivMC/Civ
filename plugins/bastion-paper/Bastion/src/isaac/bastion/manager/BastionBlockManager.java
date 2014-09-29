package isaac.bastion.manager;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.storage.BastionBlockSet;
import isaac.bastion.util.QTBox;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.entity.PlayerReinforcement;


public class BastionBlockManager
{
	public BastionBlockSet set;
	private Map<String, Long> playerLastEroded = new HashMap<String, Long>();
	private static Random generator = new Random();


	public BastionBlockManager(){
		set=new BastionBlockSet();
		set.load();
	}
	public void close(){
		set.close();
	}

	public void addBastion(Location location, PlayerReinforcement reinforcement) {
		BastionBlock toAdd=new BastionBlock(location,reinforcement);
		set.add(toAdd);
	}

	
	public void erodeFromPlace(Block orrigin, Set<Block> result, String player, Set<BastionBlock> blocking){
		if(onCooldown(player)) return;
		
		if(Bastion.getConfigManager().getBastionBlocksToErode() < 0){
			for (BastionBlock bastion : blocking){
				bastion.erode(bastion.erosionFromBlock());
			}
		} else{
			List<BastionBlock> ordered = new LinkedList<BastionBlock>(blocking);
			for(int i = 0;i < ordered.size() && (i < Bastion.getConfigManager().getBastionBlocksToErode());++i){
				int erode = generator.nextInt(ordered.size()); 
				BastionBlock toErode = ordered.get(erode);
				toErode.erode(toErode.erosionFromBlock());
				ordered.remove(erode);
			}
		}
	}
	
	public void erodeFromTeleoprt(Location loc, String player, Set<BastionBlock> blocking){
		if(onCooldown(player)) return;
		
		List<BastionBlock> ordered = new LinkedList<BastionBlock>(blocking);
		
		BastionBlock toErode = ordered.get(generator.nextInt(ordered.size()));
		toErode.erode(toErode.erosionFromPearl());
	}
	
	public boolean onCooldown(String player){
		Long last_placed = playerLastEroded.get(player);
		if (last_placed == null){
			playerLastEroded.put(player, System.currentTimeMillis());
			return false;
		}
		
		if ((System.currentTimeMillis() - playerLastEroded.get(player)) < BastionBlock.MIN_BREAK_TIME) return true;
		else playerLastEroded.put(player, System.currentTimeMillis());
		
		return false;
	}

	public Set<BastionBlock> shouldStopLocation(Location loc, String player){
		return getBlockingBastions(loc, player);
	}
	

	//handles all block based events in a general way
	public Set<BastionBlock> shouldStopBlock(Block orrigin, Set<Block> result, String player){
		if(player != null) {
			Player playerB = Bukkit.getPlayer(player);
			if (playerB != null && playerB.hasPermission("Bastion.bypass")) return new CopyOnWriteArraySet<BastionBlock>();
		}
		
		Set<BastionBlock> toReturn = new HashSet<BastionBlock>();
		Set<String> accessors = new HashSet<String>();
		if(player != null)
			accessors.add(player);
		
		if(orrigin != null){
			PlayerReinforcement reinforcement = (PlayerReinforcement) Citadel.getReinforcementManager().
			getReinforcement(orrigin);
			if(reinforcement instanceof PlayerReinforcement)
				accessors.add(reinforcement.getOwner().getFounder());
			
			for(BastionBlock bastion: this.getBlockingBastions(orrigin.getLocation()))
				accessors.add(bastion.getOwner());
		}
		
		
		for(Block block: result)
			toReturn.addAll(getBlockingBastions(block.getLocation(),accessors));
		
		
		return toReturn;
	}

	private BastionBlock getBlockingBastion(Location loc, String player){
		
		
		Set<? extends QTBox> possible=set.forLocation(loc);

		@SuppressWarnings("unchecked")
		List<BastionBlock> possibleRandom=new LinkedList<BastionBlock>((Set<BastionBlock>)possible);
		Collections.shuffle(possibleRandom);

		for (BastionBlock bastion : possibleRandom){
			if (!bastion.canPlace(player) && bastion.inField(loc)){
				return bastion;
			}
		}
		return null;
	}

	private BastionBlock getBlockingBastion(Location loc){
		Set<? extends QTBox> possible=set.forLocation(loc);

		@SuppressWarnings("unchecked")
		List<BastionBlock> possibleRandom=new LinkedList<BastionBlock>((Set<BastionBlock>)possible);
		Collections.shuffle(possibleRandom);

		for (BastionBlock bastion : possibleRandom){
			if (bastion.inField(loc)){
				return bastion;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private Set<BastionBlock> getBlockingBastions(Location loc){
		Set<? extends QTBox> boxes = set.forLocation(loc);
		Set<BastionBlock> bastions = null;
		
		if(boxes.size() > 0 && boxes.iterator().next() instanceof BastionBlock)
			bastions = (Set<BastionBlock>) boxes;

		if(bastions == null)
			return new CopyOnWriteArraySet<BastionBlock>();
		
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
	private Set<BastionBlock> getBlockingBastions(Location loc, String player){
		Set<? extends QTBox> boxes = set.forLocation(loc);
		Set<BastionBlock> bastions = null;
		
		if(boxes.size() > 0 && boxes.iterator().next() instanceof BastionBlock)
			bastions = (Set<BastionBlock>) boxes;

		if(bastions == null)
			return new CopyOnWriteArraySet<BastionBlock>();
		
		Iterator<BastionBlock> i = bastions.iterator();
		while (i.hasNext()){
			BastionBlock bastion = i.next();
			if (!bastion.inField(loc) || bastion.canPlace(player)){
				i.remove();
			}
		}
		
		return bastions;
	}
	
	
	@SuppressWarnings("unchecked")
	private Set<BastionBlock> getBlockingBastions(Location loc, Set<String> players){
		Set<? extends QTBox> boxes = set.forLocation(loc);
		Set<BastionBlock> bastions = null;
		
		if(boxes.size() != 0)
			bastions = (Set<BastionBlock>) boxes;

		if(bastions == null)
			return new CopyOnWriteArraySet<BastionBlock>();
		
		
		Iterator<BastionBlock> i = bastions.iterator();
		while (i.hasNext()){
			BastionBlock bastion = i.next();
			if(!bastion.inField(loc) || bastion.oneCanPlace(players))
				i.remove();
		};
		
		
		return bastions;
	}
	

	public String infoMessage(boolean dev, Block block, Block clicked, Player player){
		BastionBlock bastion=set.getBastionBlock(clicked.getLocation()); //Get the bastion at the location clicked.

		if(bastion!=null){ //See if anything was found
			return bastion.infoMessage(dev, player); //If there is actually something there tell the player about it.
		}

		bastion=getBlockingBastion(block.getLocation(),player.getName());
		if(bastion==null){
			bastion=getBlockingBastion(block.getLocation());
			if(bastion!=null){
				return ChatColor.GREEN+"A Bastion Block prevents others from building" + ( (dev) ? (ChatColor.BLACK + "\n" +  bastion.toString()) : "" );
			}
		} else{
			return ChatColor.RED+"A Bastion Block prevents you building" + ( (dev) ? (ChatColor.BLACK + "\n" +  bastion.toString()) : "" );
		}
		
		return ChatColor.YELLOW + "No Bastion Block";
	}
	
	
	
	
	public void handleBlockPlace(BlockPlaceEvent event) {
		Set<Block> blocks = new CopyOnWriteArraySet<Block>();
		blocks.add(event.getBlock());
		Set<BastionBlock> blocking = shouldStopBlock(null, blocks,event.getPlayer().getName());
		
		if(blocking.size() != 0){
			erodeFromPlace(null, blocks,event.getPlayer().getName(),blocking);
			
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "Bastion removed block");
			
			//event.getBlock().breakNaturally();
			//event.getBlockReplacedState().update(true, false); //most likely source of random blocks being removed. Only one I can think of.
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
		for(BlockState state: event.getBlocks())
			blocks.add(state.getBlock());
		
		Player player = event.getPlayer();
		String playerName = null;
		if(player != null)
			playerName = player.getName();
		
		Set<BastionBlock> blocking = shouldStopBlock(event.getLocation().getBlock(),blocks, playerName);
		
		if(blocking.size() != 0)
			event.setCancelled(true);
	}
	public void handlePistonPush(BlockPistonExtendEvent event) {
		Block pistion = event.getBlock();
		Set<Block> involved = new HashSet<Block>(event.getBlocks());
		involved.add(pistion.getRelative(event.getDirection()));

		
		Set<BastionBlock> blocking = shouldStopBlock (pistion, involved, null);
		
		
		if(blocking.size() != 0)
			event.setCancelled(true);
	}
	public void handleBucketPlace(PlayerBucketEmptyEvent event) {
		Set<Block> blocks = new HashSet<Block>();
		blocks.add(event.getBlockClicked().getRelative(event.getBlockFace()));
		
		Set<BastionBlock> blocking = shouldStopBlock(null,blocks, event.getPlayer().getName());
		
		if(blocking.size() != 0)
			event.setCancelled(true);
	}
	public void handleDispensed(BlockDispenseEvent event) {
		if (!(event.getItem().getType() == Material.WATER_BUCKET || event.getItem().getType() == Material.LAVA_BUCKET || event.getItem().getType() == Material.FLINT_AND_STEEL)) return;
		
		
		
		Set<Block> blocks = new HashSet<Block>();
		blocks.add(event.getBlock().getRelative( ((Dispenser) event.getBlock().getState().getData()).getFacing()));
		
		Set<BastionBlock> blocking = shouldStopBlock(event.getBlock(),blocks, null);
		
		if(blocking.size() != 0)
			event.setCancelled(true);
		
	}
	public void handleBlockBreakEvent(BlockBreakEvent event) {
		BastionBlock bastion = set.getBastionBlock(event.getBlock().getLocation());
		if (bastion != null)
			bastion.close();
	}
	public void handleEnderPearlLanded(PlayerTeleportEvent event) {
		if (!Bastion.getConfigManager().getEnderPearlsBlocked()) return; //don't block if the feature isn't enabled.
		if (event.getPlayer().hasPermission("Bastion.bypass")) return; //I'm not totally sure about the implications of this combined with humbug. It might cause some exceptions. Bukkit will catch.
		if (event.getCause() != TeleportCause.ENDER_PEARL) return; // Only handle enderpearl cases
		
		Set<BastionBlock> blocking = this.getBlockingBastions(event.getTo(), event.getPlayer().getName());
		
		if(Bastion.getConfigManager().getEnderPearlRequireMaturity()){
			Iterator<BastionBlock> i = blocking.iterator();
		
			while (i.hasNext()){
				BastionBlock bastion = i.next();
				if (!bastion.isMature()){
					i.remove();
				}
			};
		}
		
		if (blocking.size() > 0){
			this.erodeFromTeleoprt(event.getTo(), event.getPlayer().getName(), blocking);
			event.getPlayer().sendMessage(ChatColor.RED+"Ender pearl blocked by Bastion Block");
			event.getPlayer().getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
			
			event.setCancelled(true);
			return;
		}
		
		blocking = this.getBlockingBastions(event.getFrom(), event.getPlayer().getName());
		
		if(Bastion.getConfigManager().getEnderPearlRequireMaturity()){
			Iterator<BastionBlock> i = blocking.iterator();
		
			while (i.hasNext()){
				BastionBlock bastion = i.next();
				if (!bastion.isMature()){
					i.remove();
				}
			};
		}
		
		
		if (blocking.size() > 0){
			this.erodeFromTeleoprt(event.getTo(), event.getPlayer().getName(), blocking);
			event.getPlayer().sendMessage(ChatColor.RED+"Ender pearl blocked by Bastion Block");
			event.getPlayer().getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
			
			event.setCancelled(true);
			return;
		}	
	}

}
