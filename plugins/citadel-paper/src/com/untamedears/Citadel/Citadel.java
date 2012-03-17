package com.untamedears.Citadel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Citadel extends JavaPlugin
{
	public static HashMap<Material, Integer> materialStrengths;
	public static HashMap<Material, Integer> materialRequirements;
	public HashMap<Player, Integer> playerPlacementState;
	public Connection connection;

	public void onEnable()
	{
		reloadConfig();
		materialStrengths = new HashMap();
		materialRequirements = new HashMap();
		this.playerPlacementState = new HashMap();

		List tmp1 = getConfig().getIntegerList("materials.strengths.keys");
		List tmp2 = getConfig().getIntegerList("materials.strengths.strengths");
		List tmp3 = getConfig().getIntegerList("materials.strengths.requirements");

		Integer[] keys = (Integer[])tmp1.toArray(new Integer[tmp1.size()]);
		Integer[] strengths = (Integer[])tmp2.toArray(new Integer[tmp2.size()]);
		Integer[] requirements = (Integer[])tmp3.toArray(new Integer[tmp3.size()]);

		if ((keys.length != strengths.length) || (keys.length != requirements.length))
		{
			System.err.println("Citadel - config lengths not matching!");
			System.err.println("File is most likely corrupted but I'll try to do the best I can");
			System.err.println("By interpreting the shortest of it all as the proper length");
			System.err.println("Proceed with caution and re-look-over your configuration file!");
		}
		for (int i = 0; i < Math.min(keys.length, Math.min(strengths.length, requirements.length)); i++)
		{
			materialStrengths.put(Material.getMaterial(keys[i].intValue()), strengths[i]);
			materialRequirements.put(Material.getMaterial(keys[i].intValue()), requirements[i]);
		}

		try
		{
			Class.forName("org.sqlite.JDBC");

			this.connection = DriverManager.getConnection("jdbc:sqlite:plugins/Citadel/Citadel.db");
			this.connection.createStatement().execute(
				"CREATE TABLE IF NOT EXISTS REENFORCEMENTS (x INTEGER,y INTEGER,z INTEGER,world TEXT,durability INTEGER);");

			this.connection.createStatement().execute(
				"CREATE TABLE IF NOT EXISTS GROUPS (grpName TEXT,member TEXT);");

			this.connection.createStatement().execute(
				"CREATE TABLE IF NOT EXISTS REGISTRY (x INTEGER,y INTEGER,z INTEGER,world TEXT,grp TEXT);");

			Bukkit.getServer().getPluginManager().registerEvents(new BlockListener(this), this);
		}
		catch (SQLException e)
		{
			System.err.println("sorry there was some sort of sql exception:");
			System.err.println(e.toString());
			System.err.println(e.getSQLState());
			return;
		} 
		catch (ClassNotFoundException e)
		{
			System.err.println("sorry we couldn't find the sqlite driver:");
			System.err.println(e.toString());
			return;
		} 
		catch (Exception e)
		{
			System.err.println("Other weird error when enabling Citadel: ");
			System.err.println(e.toString());
		}

		System.out.println("Citadel - Hi folks, Citadel is now on :D.");
	}
	
	public void onDisable()
	{
		System.out.println("Citadel - Hi folks, Citadel is disabled :(.");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You must be a player!");
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("ctfortify"))
		{
			if (args.length < 1)
			{
				sender.sendMessage(ChatColor.RED + "Please specify reenforcement block type.");
				return true;
			}
			Material matl = Material.getMaterial(args[0].toUpperCase());
			if (materialStrengths.containsKey(matl))
				BlockListener.playerReenforcers.put((Player)sender, matl);
			else
				sender.sendMessage(ChatColor.YELLOW + "Material " + args[0] + " not found as applicable reenforcement material");
		}
		else if (cmd.getName().equalsIgnoreCase("ctstop"))
		{
			BlockListener.playerReenforcers.remove((Player)sender);
			sender.sendMessage(ChatColor.GREEN + "You are now out of reenforcement mode");
		} 
		else if (cmd.getName().equalsIgnoreCase("ctplink"))
		{
			sender.sendMessage(ChatColor.RED + "Not yet implemented, sorry");
			if (args.length < 1)
				sender.sendMessage(ChatColor.RED + "Please specify reenforcement block type.");
		}
		else if (cmd.getName().equalsIgnoreCase("ctlist"))
		{
			if ((materialRequirements.keySet() == null) || (materialRequirements.keySet().size() == 0))
			{
				sender.sendMessage(ChatColor.YELLOW + "No reenforcement materials available.");
			}
			for (Material m : materialRequirements.keySet())
				sender.sendMessage(ChatColor.GREEN + m.toString() + " has strength " + materialStrengths.get(m) + " and you'll need " + materialRequirements.get(m) + " of it.");
		}
		else if (cmd.getName().equalsIgnoreCase("ctadd"))
		{
			if (args.length < 1)
			{
				sender.sendMessage(ChatColor.RED + "Please specify player to add.");
				return true;
			}
			try {
				PreparedStatement tmp = this.connection.prepareStatement(
					"INSERT INTO GROUPS (grpName,member) values (?,?)");
				tmp.setString(1, ((Player)sender).getDisplayName() + "Grp");
				tmp.setString(2, args[0]);
				tmp.execute();
				sender.sendMessage(ChatColor.GREEN + " player " + args[0] + " added.");
			}
			catch (SQLException e)
			{
				System.err.println("Error adding player " + args[0] + " to group " + ((Player)sender).getDisplayName() + ":\n" + e);
			}
		} 
		else if (cmd.getName().equalsIgnoreCase("ctrm"))
		{
			if (args.length < 1)
			{
				sender.sendMessage(ChatColor.RED + "Please specify player to add.");
				return true;
			}
			try
			{
				PreparedStatement tmp = this.connection.prepareStatement(
					"DELETE FROM GROUPS WHERE grpName=? AND member=?");
				tmp.setString(1, ((Player)sender).getDisplayName() + "Grp");
				tmp.setString(2, args[0]);
				tmp.execute();
				sender.sendMessage(ChatColor.GREEN + " player " + args[0] + " removed.");
			} 
			catch (SQLException e)
			{
				System.err.println("Error removing player " + args[0] + " from group " + ((Player)sender).getDisplayName() + ":\n" + e);
			}
		}
		else if (cmd.getName().equalsIgnoreCase("ctpublic"))
		{
			this.playerPlacementState.put((Player)sender, Integer.valueOf(0));
			sender.sendMessage(ChatColor.GREEN + "Now in public mode.");
		} 
		else if (cmd.getName().equalsIgnoreCase("ctprivate"))
		{
			this.playerPlacementState.put((Player)sender, Integer.valueOf(1));
			sender.sendMessage(ChatColor.GREEN + "Now in private mode.");
		} else if (cmd.getName().equalsIgnoreCase("ctgrp")) {
			this.playerPlacementState.put((Player)sender, Integer.valueOf(2));
			sender.sendMessage(ChatColor.GREEN + "Now in group mode.");
		}
		return true;
	}
}