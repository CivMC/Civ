package com.untamedears.citadel.command.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.GroupManager;
import com.untamedears.citadel.ReinforcementManager;
import com.untamedears.citadel.command.PlayerCommand;

/**
 * User: JonnyD
 * Date: 7/18/12
 * Time: 11:57 PM
 */
public class StatsCommand extends PlayerCommand {

    public class SendResultsTask implements Runnable {
        public QueryDbTask previousTask;
        public SendResultsTask(QueryDbTask pt) {
            previousTask = pt;
        }
        @Override
        public void run() {
		    previousTask.sender.sendMessage(
                new StringBuilder().append("§cTotal Reinforcements:§e " ).append(previousTask.numReinforcements).toString());
		    previousTask.sender.sendMessage(
                new StringBuilder().append("§cTotal Groups:§e " ).append(previousTask.numGroups).toString());
        }
    }

    public class QueryDbTask implements Runnable {
        public CommandSender sender;
        public int numReinforcements;
        public int numGroups;
        public QueryDbTask(CommandSender s) {
            sender = s;
        }
        @Override
        public void run() {
		    ReinforcementManager reinforcementManager = Citadel.getReinforcementManager();
		    numReinforcements = reinforcementManager.getReinforcementsAmount();

		    GroupManager groupManager = Citadel.getGroupManager();
		    numGroups = groupManager.getGroupsAmount();

            Bukkit.getScheduler().runTask(
                Citadel.getPlugin(), new SendResultsTask(this));
        }
    }

	public StatsCommand() {
		super("View Stats");
		setDescription("View citadel stats");
		setUsage("/ctstats");
		setIdentifiers(new String[] {"ctstats"});
	}

	public boolean execute(CommandSender sender, String[] args) {		
        Bukkit.getScheduler().runTaskAsynchronously(
            Citadel.getPlugin(), new QueryDbTask(sender));
		return true;
	}

}
