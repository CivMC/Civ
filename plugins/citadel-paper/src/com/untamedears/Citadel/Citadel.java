package com.untamedears.Citadel;

import com.untamedears.Citadel.dao.CitadelDao;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class Citadel extends JavaPlugin {
    public static HashMap<Material, Integer> materialStrengths;
    public static HashMap<Material, Integer> materialRequirements;
    public HashMap<Player, Integer> playerPlacementState;
    public CitadelDao dao;
    public Logger log;

    public void onEnable() {
        log = this.getLogger();
        reloadConfig();
        materialStrengths = new HashMap<Material, Integer>();
        materialRequirements = new HashMap<Material, Integer>();
        this.playerPlacementState = new HashMap<Player, Integer>();

        List<Integer> tmp1 = getConfig().getIntegerList("materials.strengths.keys");
        List<Integer> tmp2 = getConfig().getIntegerList("materials.strengths.strengths");
        List<Integer> tmp3 = getConfig().getIntegerList("materials.strengths.requirements");

        Integer[] keys = tmp1.toArray(new Integer[tmp1.size()]);
        Integer[] strengths = tmp2.toArray(new Integer[tmp2.size()]);
        Integer[] requirements = tmp3.toArray(new Integer[tmp3.size()]);

        if ((keys.length != strengths.length) || (keys.length != requirements.length)) {
            log.warning("Citadel - config lengths not matching!");
            log.warning("File is most likely corrupted but I'll try to do the best I can");
            log.warning("By interpreting the shortest of it all as the proper length");
            log.warning("Proceed with caution and re-look-over your configuration file!");
        }
        for (int i = 0; i < Math.min(keys.length, Math.min(strengths.length, requirements.length)); i++) {
            materialStrengths.put(Material.getMaterial(keys[i]), strengths[i]);
            materialRequirements.put(Material.getMaterial(keys[i]), requirements[i]);
        }

        try {
            dao = new CitadelDao();

            Bukkit.getServer().getPluginManager().registerEvents(new BlockListener(this), this);
            log.info("Citadel - Hi folks, Citadel is now on :D.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void onDisable() {
        log.info("Citadel - Hi folks, Citadel is disabled :(.");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player!");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("ctfortify")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Please specify reinforcement block type.");
                return true;
            }
            Material matl = Material.getMaterial(args[0].toUpperCase());
            if (materialStrengths.containsKey(matl))
                BlockListener.playerReinforcers.put((Player) sender, matl);
            else
                sender.sendMessage(ChatColor.YELLOW + "Material " + args[0] + " not found as applicable reinforcement material");
        } else if (cmd.getName().equalsIgnoreCase("ctstop")) {
            BlockListener.playerReinforcers.remove((Player) sender);
            sender.sendMessage(ChatColor.GREEN + "You are now out of reinforcement mode");
        } else if (cmd.getName().equalsIgnoreCase("ctplink")) {
            sender.sendMessage(ChatColor.RED + "Not yet implemented, sorry");
            if (args.length < 1)
                sender.sendMessage(ChatColor.RED + "Please specify reinforcement block type.");
        } else if (cmd.getName().equalsIgnoreCase("ctlist")) {
            if ((materialRequirements.keySet() == null) || (materialRequirements.keySet().size() == 0)) {
                sender.sendMessage(ChatColor.YELLOW + "No reinforcement materials available.");
            }
            for (Material m : materialRequirements.keySet())
                sender.sendMessage(ChatColor.GREEN + m.toString() + " has strength " + materialStrengths.get(m) + " and you'll need " + materialRequirements.get(m) + " of it.");
        } else if (cmd.getName().equalsIgnoreCase("ctadd")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Please specify player to add.");
                return true;
            }
            dao.addPlayerToGroup(((Player) sender).getDisplayName(), args[0]);
            sender.sendMessage(ChatColor.GREEN + " player " + args[0] + " added.");
        } else if (cmd.getName().equalsIgnoreCase("ctrm")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Please specify player to add.");
                return true;
            }
            dao.removePlayerFromGroup(((Player) sender).getDisplayName(), args[0]);
            sender.sendMessage(ChatColor.GREEN + " player " + args[0] + " removed.");
        } else if (cmd.getName().equalsIgnoreCase("ctpublic")) {
            this.playerPlacementState.put((Player) sender, 0);
            sender.sendMessage(ChatColor.GREEN + "Now in public mode.");
        } else if (cmd.getName().equalsIgnoreCase("ctprivate")) {
            this.playerPlacementState.put((Player) sender, 1);
            sender.sendMessage(ChatColor.GREEN + "Now in private mode.");
        } else if (cmd.getName().equalsIgnoreCase("ctgrp")) {
            this.playerPlacementState.put((Player) sender, 2);
            sender.sendMessage(ChatColor.GREEN + "Now in group mode.");
        }
        return true;
    }
}