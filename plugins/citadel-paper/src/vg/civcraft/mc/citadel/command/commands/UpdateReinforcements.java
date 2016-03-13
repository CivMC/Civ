package vg.civcraft.mc.citadel.command.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.reinforcement.MultiBlockReinforcement;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

public class UpdateReinforcements extends PlayerCommandMiddle{

	private GroupManager gm = NameAPI.getGroupManager();
	
	public UpdateReinforcements(String name) {
		super(name);
		setIdentifier("ctur");
		setDescription("Updates all reinforcements in a chunk and group to a certain group.");
		setUsage("/ctur <old group> <new group>");
		setArguments(0,2);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage("How would this even work.");
			return true;
		}
		Player p = (Player) sender;
		if (!p.hasPermission("citadel.admin") || !p.isOp()){
			sendAndLog(p, ChatColor.RED, "Nice try");
			return true;
		}
		
		if (args.length == 0){
			sendAndLog(p, ChatColor.GREEN, "Searching for groups in current chunk.");
			Bukkit.getScheduler().runTaskAsynchronously(Citadel.getInstance(), new FindGroups(p, p.getLocation()));
			return true;
		}
		else if (args.length == 1){
			sendAndLog(p, ChatColor.RED, "Please enter two groups.");
			return true;
		}
		
		Group old = gm.getGroup(args[0]);
		Group n = gm.getGroup(args[1]);
		
		if (old == null || n == null){
			sendAndLog(p, ChatColor.RED, "One of the groups does not exist.");
			return true;
		}
		
		sendAndLog(p, ChatColor.GREEN, "Beginning to change groups.");
		Bukkit.getScheduler().runTaskAsynchronously(Citadel.getInstance(), new UpdateGroups(p, p.getLocation().getChunk(), old, n));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}
	
	class UpdateGroups implements Runnable{

		private final Group old, n;
		private final Player p;
		private final Chunk c;
		public UpdateGroups(Player p, Chunk c, Group old, Group n){
			this.old = old;
			this.n = n;
			this.p = p;
			this.c = c;
		}
		
		@Override
		public void run() {
			ReinforcementManager rm = Citadel.getReinforcementManager();
			List<Reinforcement> reins = rm.getReinforcementsByChunk(c);
			for (Reinforcement r: reins){
				if (r instanceof PlayerReinforcement){
					PlayerReinforcement rein = (PlayerReinforcement) r;
					if (rein.getGroup().getName().equals(old.getName())) {
						rein.setGroup(n);
					}
				}
				else if (r instanceof MultiBlockReinforcement){
					MultiBlockReinforcement rein = (MultiBlockReinforcement) r;
					if (rein.getGroup().getName().equals(old.getName())) {
						rein.setGroup(n);
					}
				}
			}
			if (!p.isOnline())
				return;
			
			sendAndLog(p, ChatColor.GREEN, "The groups have been updated.");
		}
		
	}
	
	class FindGroups implements Runnable{

		private final Player p;
		private final Location loc;
		
		public FindGroups(Player p, Location loc){
			this.p = p;
			this.loc = loc;
		}
		
		@Override
		public void run() {
			ReinforcementManager rm = Citadel.getReinforcementManager();
			List<Reinforcement> reins = rm.getReinforcementsByChunk(loc.getChunk());
			List<String> groups = new ArrayList<String>();
			for (Reinforcement r: reins){
				if (r instanceof PlayerReinforcement){
					PlayerReinforcement rein = (PlayerReinforcement) r;
					String name = rein.getGroup().getName();
					if (groups.contains(name)) {
						continue;
					}
					groups.add(name);
				}
				else if (r instanceof MultiBlockReinforcement){
					MultiBlockReinforcement rein = (MultiBlockReinforcement) r;
					String name = rein.getGroup().getName();
					if (groups.contains(name)) {
						continue;
					}
					groups.add(name);
				}
			}
			if (!p.isOnline()) {
				return;
			}
			StringBuilder names = new StringBuilder();
			for (String g: groups) {
				names.append(g).append(" ");
			}
			
			sendAndLog(p, ChatColor.GREEN, "The groups in this chunk are: " + names);
		}
		
	}

}
