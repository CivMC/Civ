package com.untamedears.Citadel;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Door;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class BlockListener implements Listener
{
	private HashMap<Block, Integer> delayedReinforcements;
	private HashMap<Integer, Material> taskMaterial;
	private HashMap<Integer, Player> taskInitiator;
	public static HashMap<Player, Material> playerReenforcers;
	private JavaPlugin myPlugin;
	private Connection conn;

	public BlockListener(JavaPlugin jp)throws SQLException, ClassNotFoundException
	{
		this.delayedReinforcements = new HashMap();
		playerReenforcers = new HashMap();
		this.taskMaterial = new HashMap();
		this.taskInitiator = new HashMap();

		this.myPlugin = jp;
		this.conn = ((Citadel)this.myPlugin).connection;
	}

	@EventHandler
	public void applyReinforcement(BlockPlaceEvent bpe)
	{
		Player placer = bpe.getPlayer();
		Block block = bpe.getBlock();
		Material matl = (Material)playerReenforcers.get(placer);
		Integer pid = null;
		if ((matl != null) && (Citadel.materialStrengths.containsKey(matl)) && (Citadel.materialRequirements.containsKey(matl)))
		{
			System.out.println("matl: " + matl + " requires: " + Citadel.materialRequirements.get(matl));
			if (placer.getInventory().contains(matl, ((Integer)Citadel.materialRequirements.get(matl)).intValue()))
			{
				try
				{
					PreparedStatement tmp = this.conn.prepareStatement(
						"INSERT INTO REINFORCEMENTS (x,y,z,world,durability) values (?,?,?,?,?)");
					tmp.setInt(1, block.getX());
					tmp.setInt(2, block.getY());
					tmp.setInt(3, block.getZ());
					tmp.setString(4, block.getWorld().getName());
					tmp.setInt(5, ((Integer)Citadel.materialStrengths.get(matl)).intValue());

					pid = Integer.valueOf(this.myPlugin.getServer().getScheduler().scheduleSyncDelayedTask(
							this.myPlugin, 
							new DelayedReinforcement(tmp),
							20L));

					this.delayedReinforcements.put(block, pid);
					placer.getInventory().removeItem(new ItemStack[] { new ItemStack(matl, ((Integer)Citadel.materialRequirements.get(matl)).intValue()) });
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
		Block tmp = bbe.getBlock();

		if (this.delayedReinforcements.containsKey(tmp))
		{
			Integer pid = (Integer)this.delayedReinforcements.get(tmp);
			Material matl = (Material)this.taskMaterial.get(pid);
			this.myPlugin.getServer().getScheduler().cancelTask(((Integer)this.delayedReinforcements.get(tmp)).intValue());
			((Player)this.taskInitiator.get(pid)).getInventory().addItem(new ItemStack[] { new ItemStack(matl, ((Integer)Citadel.materialRequirements.get(matl)).intValue()) });

			this.delayedReinforcements.remove(tmp);
			this.taskInitiator.remove(pid);
			this.taskMaterial.remove(pid);
		}
        updateReinforcement(tmp, bbe);
        bbe.setCancelled(true);
    }
    private void updateReinforcement(Block block, Event event){

		try
		{
			PreparedStatement ask = this.conn.prepareStatement(
				"SELECT DURABILITY FROM REINFORCEMENTS WHERE x=? AND y=? AND z=? AND world=?");
			ask.setInt(1, block.getX());
			ask.setInt(2, block.getY());
			ask.setInt(3, block.getZ());
			ask.setString(4, block.getWorld().getName());
			ask.execute();
			ResultSet answer = ask.getResultSet();
			if (!answer.next())
			{
				answer.close();
				ask.close();
				return;
			}
			int durability = answer.getInt(1);
			durability--;
			ask.close();
			answer.close();
			if (durability <= 0)
			{
				PreparedStatement delete = this.conn.prepareStatement(
					"DELETE FROM REINFORCEMENTS WHERE x=? AND y=? AND z=? AND world=?");
				delete.setInt(1, block.getX());
				delete.setInt(2, block.getY());
				delete.setInt(3, block.getZ());
				delete.setString(4, block.getWorld().getName());
				delete.execute();
				delete.close();
			}
			else
			{
				PreparedStatement update = this.conn.prepareStatement(
					"UPDATE REINFORCEMENTS SET DURABILITY=? WHERE x=? AND y=? AND z=? AND world=?");
				update.setInt(1, durability);
				update.setInt(2, block.getX());
				update.setInt(3, block.getY());
				update.setInt(4, block.getZ());
				update.setString(5, block.getWorld().getName());
				update.execute();
				update.close();
			}
		} 
		catch (SQLException e)
		{
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
				PreparedStatement tmp = this.conn.prepareStatement(
					"SELECT grp FROM REGISTRY WHERE x=? AND y=? AND z=? AND world=?");
				tmp.setInt(1, block.getX());
				tmp.setInt(2, block.getY());
				tmp.setInt(3, block.getZ());
				tmp.setString(4, block.getWorld().getName());
				tmp.execute();
				ResultSet answer = tmp.getResultSet();

				if (!answer.next()) {
					answer.close();
					tmp.close();
					return;
				}
				do
				{
					PreparedStatement queryGroup = this.conn.prepareStatement(
						"SELECT grpName FROM GROUPS WHERE grpName=? AND member=?");
					queryGroup.setString(1, answer.getString(1));
					queryGroup.setString(2, p.getPlayerListName());
					queryGroup.execute();
					ResultSet tmpRs = queryGroup.getResultSet();
					if (tmpRs.next())
					{
						tmpRs.close();
						queryGroup.close();
						tmp.close();
						return;
					}
					tmpRs.close();
					queryGroup.close();
				}
				while (answer.next());
				tmp.close();
				
				p.sendMessage(ChatColor.RED + "That door/chest is locked.");
				/*
				p.sendMessage(ChatColor.RED + "That door/chest is locked.  O NOZ!!!");
				p.sendMessage(ChatColor.MAGIC + "Hey Beavis, that was, like, cool.");
				p.sendMessage(ChatColor.YELLOW + "Huh huh.");
				p.sendMessage(ChatColor.GREEN + "Uh, huh huh.  Oh yeah.  Money is cool!");
				p.sendMessage(ChatColor.BLUE + "YEAH!  It, like, uh, huh huh.  Uh...");
				p.sendMessage(ChatColor.LIGHT_PURPLE + "Huh huh huh.  Uh, hmmmm, heh, huh, hmmm huh huh.  Yeah.");
				*/
				pie.setCancelled(true);
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
				PreparedStatement tmp = this.conn.prepareStatement(
					"SELECT group FROM REGISTRY WHERE x=? AND y=? AND z=? AND world=?");
				tmp.setInt(1, block.getX());
				tmp.setInt(2, block.getY());
				tmp.setInt(3, block.getZ());
				tmp.setString(4, block.getWorld().getName());
				tmp.execute();
				ResultSet answer = tmp.getResultSet();

				if (answer.next())
				{
					bre.setNewCurrent(bre.getOldCurrent());
				}
				answer.close();
				tmp.close();
			}
			catch (SQLException e)
			{
				System.err.println("Error in redstone protection checking:\n" + e);
			}
	}

	@EventHandler
	public void makePrivateGroup(PlayerLoginEvent ple)
	{
		((Citadel)this.myPlugin).playerPlacementState.put(ple.getPlayer(), Integer.valueOf(1));
		try
		{
			PreparedStatement tmp = this.conn.prepareStatement("SELECT grpName FROM GROUPS WHERE grpName = ?");
			tmp.setString(1, ple.getPlayer().getDisplayName());
			tmp.execute();
			ResultSet rs = tmp.getResultSet();
			if (!rs.next())
			{
				tmp = this.conn.prepareStatement("INSERT INTO GROUPS (grpName,member) values (?,?)");
				tmp.setString(1, ple.getPlayer().getDisplayName());
				tmp.setString(2, ple.getPlayer().getDisplayName());
				tmp.execute();
				tmp.close();
			}
			rs.close();
			tmp.close();
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

		Integer playerState = (Integer)((Citadel)this.myPlugin).playerPlacementState.get(bpe.getPlayer());
		if (playerState == null)
		{
			((Citadel)this.myPlugin).playerPlacementState.put(bpe.getPlayer(), Integer.valueOf(1));
			playerState = Integer.valueOf(1);
		}

		if (playerState.intValue() > 0)
		{
			try
			{
				PreparedStatement tmp = this.conn.prepareStatement(
					"INSERT INTO REGISTRY (x,y,z,world,grp) VALUES (?,?,?,?,?)");
				tmp.setInt(1, blk.getX());
				tmp.setInt(2, blk.getY());
				tmp.setInt(3, blk.getZ());
				tmp.setString(4, blk.getWorld().getName());
				tmp.setString(5, bpe.getPlayer().getDisplayName());
				tmp.execute();
				if (playerState.intValue() > 1)
				{
					tmp.setString(5, bpe.getPlayer().getDisplayName() + "Grp");
					tmp.execute();
				}
				tmp.close();
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
			this.conn.close();
		}
		catch (SQLException e)
		{
			System.err.println("Citadel - Sorry, I'll have to close the database ungracefully!");
			System.err.println(e.toString());
		}
	}
}