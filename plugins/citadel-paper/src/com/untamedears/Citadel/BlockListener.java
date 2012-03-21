package com.untamedears.Citadel;

import com.untamedears.Citadel.dao.CitadelDao;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Door;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class BlockListener implements Listener {
    private HashMap<Block, Integer> delayedReinforcements;
    private HashMap<Integer, Material> taskMaterial;
    private HashMap<Integer, Player> taskInitiator;
    public static HashMap<Player, Material> playerReinforcers;
    private JavaPlugin myPlugin;
    private CitadelDao dao;
    private Logger log;

    public BlockListener(JavaPlugin jp) throws ClassNotFoundException {
        this.delayedReinforcements = new HashMap<Block, Integer>();
        playerReinforcers = new HashMap<Player, Material>();
        this.taskMaterial = new HashMap<Integer, Material>();
        this.taskInitiator = new HashMap<Integer, Player>();

        this.myPlugin = jp;
        this.dao = ((Citadel) this.myPlugin).dao;
        this.log = myPlugin.getLogger();
    }

    @EventHandler
    public void applyReinforcement(BlockPlaceEvent bpe) {
        Player placer = bpe.getPlayer();
        Block block = bpe.getBlock();
        Material matl = playerReinforcers.get(placer);
        if ((matl != null) && (Citadel.materialStrengths.containsKey(matl)) && (Citadel.materialRequirements.containsKey(matl))) {
            log.info("matl: " + matl + " requires: " + Citadel.materialRequirements.get(matl));
            if (placer.getInventory().contains(matl, Citadel.materialRequirements.get(matl))) {
                int pid = this.myPlugin.getServer().getScheduler().scheduleSyncDelayedTask(
                        this.myPlugin,
                        dao.addReinforcement(block, matl),
                        20L);

                this.delayedReinforcements.put(block, pid);
                placer.getInventory().removeItem(new ItemStack(matl, Citadel.materialRequirements.get(matl)));
                this.taskInitiator.put(pid, placer);
                this.taskMaterial.put(pid, matl);
            } else {
                placer.sendMessage(ChatColor.YELLOW + "You require more " + matl + " to continue reinforcements.");
                playerReinforcers.remove(placer);
                placer.sendMessage("You are now out of reinforcement mode");
            }
        }
    }

    @EventHandler
    public void gracefullyRemoveReinforcementModeOnLogout(PlayerQuitEvent pqe) {
        if (playerReinforcers.containsKey(pqe.getPlayer()))
            playerReinforcers.remove(pqe.getPlayer());
    }

    @EventHandler
    public void checkDurabilityAndDelayedEventCheck(BlockBreakEvent bbe) {
        Block block = bbe.getBlock();
        if (this.delayedReinforcements.containsKey(block)) {
            Integer pid = this.delayedReinforcements.get(block);
            Material matl = this.taskMaterial.get(pid);
            this.myPlugin.getServer().getScheduler().cancelTask(this.delayedReinforcements.get(block));
            this.taskInitiator.get(pid).getInventory().addItem(new ItemStack(matl, Citadel.materialRequirements.get(matl)));

            this.delayedReinforcements.remove(block);
            this.taskInitiator.remove(pid);
            this.taskMaterial.remove(pid);
        }

        Integer durability = dao.updateReinforcement(block, 1);
        if (durability == null) return;

        if (durability <= 0) {
            dao.removeReinforcement(block);
        } else {
            bbe.setCancelled(true);
        }
    }
    
    @EventHandler
	public void checkForExplosion(EntityExplodeEvent event){
		
		if(event.isCancelled()){
			return;
		}
		
		//Store blocks that are inside explosion's blast radius
		List<Block> blastRadiusBlocks = event.blockList();
		
		//If explosion occurs in mid air it returns 0 so no need to go any
		//further since there are no blocks inside the explosions radius
		if(blastRadiusBlocks.size() < 1){
			return;
		}
		
		HashMap<Coordinate, Block> affectedBlocks = new HashMap<Coordinate, Block>();
		
		//Initialize min & max X,Y,Z coordinates
		int smallestX = blastRadiusBlocks.get(0).getX();
		int largestX = smallestX;
		int smallestY = blastRadiusBlocks.get(0).getY();
		int largestY = smallestY;
		int smallestZ = blastRadiusBlocks.get(0).getZ();
		int largestZ = smallestZ;

		//World Name
		String worldName = blastRadiusBlocks.get(0).getWorld().getName();
		World world = this.myPlugin.getServer().getWorld(worldName);
		
		//Find min & max X,Y,Z coordinates
		for(int i = 0; i < blastRadiusBlocks.size(); i++){
			Block block = blastRadiusBlocks.get(i);
			int blockX = block.getX();
			int blockY = block.getY();
			int blockZ = block.getZ();
			
			if(blockX < smallestX){
				smallestX = blockX;
			}
			
			if(blockX > largestX){
				largestX = blockX;
			}
			
			if(blockY < smallestY){
				smallestY = blockY;
			}
			
			if(blockY > largestY){
				largestY = blockY;
			}
			
			if(blockZ < smallestZ){
				smallestZ = blockZ;
			}
			
			if(blockZ > largestZ){
				largestZ = blockZ;
			}
			
			//Instantiate Coordinate class passing in parameters
			Coordinate coordinate = new Coordinate(world, blockX, blockY, blockZ);
			//Put a new entry of type Coordinate as key and type Block as value
			affectedBlocks.put(coordinate, block);
		}
		
		//Query database for any reinforced blocks that may be in the blast radius
		//Reinforced blocks should have a durability > 0 (aka >= 1)
		ResultSet result = dao.selectReinforcements(worldName, smallestX, largestX, smallestY, largestY, smallestZ, largestZ);
			
		//If there was some found, loop through each one
		try {
			while(result.next()){
				//Get X,Y,Z coords of reinforced block
				int x = result.getInt(1);
				int y = result.getInt(2);
				int z = result.getInt(3);
								    
				//Pass in x, y, z of reinforced block into affectedBlocks HashMap to instantiate a Block
				Block protectedBlock = affectedBlocks.get(new Coordinate(world, x, y, z));
				//Then remove the protectedBlock from explosion list
				event.blockList().remove(protectedBlock);
				}
			} catch (SQLException e) {
				System.err.println("Citadel - explosion error");
		}
		 
		//Update reinforcements to set durability of blocks that are within blast radius
		//Explosion should decrement durability by 1
		dao.updateReinforcements(worldName, smallestX, largestX, smallestY, largestY, smallestZ, largestZ);
	}
	

    @EventHandler
    public void controlAccess(PlayerInteractEvent pie) {
        if (!pie.hasBlock()) return;

        Block block = pie.getClickedBlock();
        Material matl = block.getType();

        Player p = pie.getPlayer();
        if ((matl == Material.CHEST) || (matl == Material.WOODEN_DOOR) || (matl == Material.IRON_DOOR)) {
            String group = dao.getRegisteredGroup(block);
            if (group != null && !dao.isPlayerInGroup(group, p.getDisplayName())) {
                p.sendMessage(ChatColor.RED + "That door/chest is locked.");
                pie.setCancelled(true);
            }
        }
    }

    public void controlRedstone(BlockRedstoneEvent bre) {
        Block block = bre.getBlock();

        if ((block instanceof Door)) {
            String group = dao.getRegisteredGroup(block);
            if (group != null) {
                bre.setNewCurrent(bre.getOldCurrent());
            }
        }
    }

    @EventHandler
    public void makePrivateGroup(PlayerLoginEvent ple) {
        ((Citadel) this.myPlugin).playerPlacementState.put(ple.getPlayer(), 1);
        String playerName = ple.getPlayer().getDisplayName();
        if (!dao.isPlayerInGroup(playerName, playerName)) {
            dao.addPlayerToGroup(playerName, playerName);
        }
    }

    @EventHandler
    public void secureDoorOrChest(BlockPlaceEvent bpe) {
        Block blk = bpe.getBlock();
        Material object = bpe.getBlock().getType();

        if ((object != Material.CHEST) && (object != Material.WOODEN_DOOR) && (object != Material.IRON_DOOR)) {
            return;
        }

        Integer playerState = ((Citadel) this.myPlugin).playerPlacementState.get(bpe.getPlayer());
        if (playerState == null) {
            ((Citadel) this.myPlugin).playerPlacementState.put(bpe.getPlayer(), 0);
            playerState = 0;
        }

        if (playerState > 0) {
            dao.addRegisteredGroup(blk, bpe.getPlayer().getDisplayName());
        }
    }

    public void close() {
        dao.close();
    }
}