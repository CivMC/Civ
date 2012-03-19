package com.untamedears.Citadel;

import com.untamedears.Citadel.dao.CitadelDao;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Door;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.HashMap;

public class BlockListener implements Listener
{
	private HashMap<Block, Integer> delayedReinforcements;
	private HashMap<Integer, Material> taskMaterial;
	private HashMap<Integer, Player> taskInitiator;
	public static HashMap<Player, Material> playerReenforcers;
	private JavaPlugin myPlugin;
	private CitadelDao dao;

	public BlockListener(JavaPlugin jp)throws SQLException, ClassNotFoundException
	{
		this.delayedReinforcements = new HashMap<Block, Integer>();
		playerReenforcers = new HashMap<Player, Material>();
		this.taskMaterial = new HashMap<Integer, Material>();
		this.taskInitiator = new HashMap<Integer, Player>();

		this.myPlugin = jp;
		this.dao = ((Citadel)this.myPlugin).dao;
	}

	@EventHandler
	public void applyReinforcement(BlockPlaceEvent bpe)
	{
		Player placer = bpe.getPlayer();
		Block block = bpe.getBlock();
		Material matl = playerReenforcers.get(placer);
		if ((matl != null) && (Citadel.materialStrengths.containsKey(matl)) && (Citadel.materialRequirements.containsKey(matl)))
		{
			System.out.println("matl: " + matl + " requires: " + Citadel.materialRequirements.get(matl));
			if (placer.getInventory().contains(matl, Citadel.materialRequirements.get(matl)))
			{
				try
				{
                    int pid = this.myPlugin.getServer().getScheduler().scheduleSyncDelayedTask(
                            this.myPlugin,
                            dao.addReinforcement(block, matl),
                            20L);

					this.delayedReinforcements.put(block, pid);
					placer.getInventory().removeItem(new ItemStack(matl, Citadel.materialRequirements.get(matl)));
					this.taskInitiator.put(pid, placer);
					this.taskMaterial.put(pid, matl);
				}
				catch (SQLException e)
				{
					System.err.println("Exception creating reinforcement:\n" + e);
					return;
				}
			}
			else
			{
				placer.sendMessage(ChatColor.YELLOW + "You require more " + matl + " to continue reinforcements.");
				playerReenforcers.remove(placer);
				placer.sendMessage("You are now out of reinforcement mode");
			}
		}
	}

	@EventHandler
	public void gracefullyRemoveReinforcementModeOnLogout(PlayerQuitEvent pqe)
	{
		if (playerReenforcers.containsKey(pqe.getPlayer()))
			playerReenforcers.remove(pqe.getPlayer());
	}

	@EventHandler
	public void checkDurabilityAndDelayedEventCheck(BlockBreakEvent bbe) {
        Block block = bbe.getBlock();
        try {
            if (this.delayedReinforcements.containsKey(block))
            {
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

            if (durability <= 0)
            {
                dao.removeReinforcement(block);
            } else
            {
                bbe.setCancelled(true);
            }
        } catch (SQLException e) {
            System.err.println("Citadel - error accessing database:\n" + e);
        }
    }

	@EventHandler
	public void controlAccess(PlayerInteractEvent pie) {
        if (!pie.hasBlock()) return;

		Block block = pie.getClickedBlock();
        Material matl = block.getType();

		Player p = pie.getPlayer();
		try
		{
			if ((matl == Material.CHEST) || (matl == Material.WOODEN_DOOR) || (matl == Material.IRON_DOOR)) {
                String group = dao.getRegisteredGroup(block);
				if (group != null && !dao.isPlayerInGroup(group, p.getDisplayName())) {
                    p.sendMessage(ChatColor.RED + "That door/chest is locked.");
                    pie.setCancelled(true);
                }
		    }
		}
		catch (SQLException e)
		{
			System.err.println("Error when seeing if the door's part of a group:\n" + e);
		}
	}

	public void controlRedstone(BlockRedstoneEvent bre)
	{
		Block block = bre.getBlock();

		if ((block instanceof Door))
			try
			{
                String group = dao.getRegisteredGroup(block);
                if (group != null) {
                    bre.setNewCurrent(bre.getOldCurrent());
                }
			}
			catch (SQLException e)
			{
				System.err.println("Error in redstone protection checking:\n" + e);
			}
	}

	@EventHandler
	public void makePrivateGroup(PlayerLoginEvent ple)
	{
		((Citadel)this.myPlugin).playerPlacementState.put(ple.getPlayer(), 1);
		try
		{
            String playerName = ple.getPlayer().getDisplayName();
            if (!dao.isPlayerInGroup(playerName, playerName)) {
                dao.addPlayerToGroup(playerName, playerName);
            }
		}
		catch (SQLException e)
		{
			System.err.println("Error in seeing if player has self-group:\n" + e);
		}
	}

	@EventHandler
	public void secureDoorOrChest(BlockPlaceEvent bpe)
	{
		Block blk = bpe.getBlock();
		Material object = bpe.getBlock().getType();

		if ((object != Material.CHEST) && (object != Material.WOODEN_DOOR) && (object != Material.IRON_DOOR))
		{
			return;
		}

		Integer playerState = ((Citadel)this.myPlugin).playerPlacementState.get(bpe.getPlayer());
		if (playerState == null)
		{
			((Citadel)this.myPlugin).playerPlacementState.put(bpe.getPlayer(), 0);
			playerState = 0;
		}

		if (playerState > 0)
		{
			try
			{
                dao.addRegisteredGroup(blk, bpe.getPlayer().getDisplayName());
			}
			catch (SQLException e)
			{
				System.err.println("Error putting chest/door into protection database:\n" + e);
			}
		}
	}

	public void close()
	{
		try
		{
			dao.close();
		}
		catch (SQLException e)
		{
			System.err.println("Citadel - Sorry, I'll have to close the database ungracefully!");
			System.err.println(e.toString());
		}
	}
}