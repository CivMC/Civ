package vg.civcraft.mc.citadel.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import vg.civcraft.mc.citadel.AcidManager;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelWorldManager;
import vg.civcraft.mc.citadel.OldCitadelConfigManager;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.citadel.events.AcidBlockEvent;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

@CivCommand(id = "ctacid")
public class Acid extends StandaloneCommand {
	
	private static final String acidPermission = "ACIDBLOCK";

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player p = (Player) sender;
		Iterator<Block> itr = new BlockIterator(p, 40); // Within 2.5 chunks
		AcidManager acidMan = Citadel.getInstance().getAcidManager();
		while (itr.hasNext()) {
			Block block = itr.next();
			if (!acidMan.isPossibleAcidBlock(block)) {
				continue;
			}
			Reinforcement reinforcement = Citadel.getInstance().getReinforcementManager().getReinforcement(block);
			if (reinforcement == null) {
				Utility.sendAndLog(p, ChatColor.RED, "That block is not reinforced.");
				return true;
			}
			UUID uuid = NameAPI.getUUID(p.getName());
			if (!reinforcement.hasPermission(p, acidPermission)) {
				Utility.sendAndLog(p, ChatColor.RED, "You do not have sufficient permission to use acid blocks on this group.");
				return true;
			}
			
			
			
			int time = Utility.timeUntilAcidMature(pRein);
			if (time != 0) {
				Utility.sendAndLog(p, ChatColor.RED, "That acid block is not mature yet.");
				return true;
			}
			Block topFace = block.getRelative(BlockFace.UP);
			if (Material.AIR.equals(topFace.getType())) {
				Utility.sendAndLog(p, ChatColor.RED, "There is no block above to acid block.");
				return true;
			}
			Reinforcement topRein = rm.getReinforcement(topFace);
			if (topRein == null) {
				Utility.sendAndLog(p, ChatColor.RED, "That block doesn't have a reinforcement.");
				return true;
			}
			if (!(topRein instanceof PlayerReinforcement)) {
				continue;
			}
			PlayerReinforcement pTopRein = (PlayerReinforcement) topRein;
			ReinforcementType acidBlockType = ReinforcementType
					.getReinforcementType(pRein.getStackRepresentation());
			ReinforcementType topReinType = ReinforcementType
					.getReinforcementType(pTopRein.getStackRepresentation());
			if (acidBlockType.getAcidTime() < topReinType.getAcidTime()) {
				Utility.sendAndLog(p, ChatColor.RED, "This acid block is too weak for that reinforcement.");
				return true;
			}
			AcidBlockEvent event = new AcidBlockEvent(p, pRein, pTopRein);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) {
	            if (OldCitadelConfigManager.shouldLogInternal()) {
	            	Citadel.getInstance().getLogger().log(Level.INFO,
	            			"Acid block event cancelled for acid at " + pRein.getLocation() );
	            }
				return true;
			}

			if (OldCitadelConfigManager.shouldLogHostileBreaks()) {
				Citadel.getInstance().getLogger().log(Level.INFO, "Acid at {0} broke {1} at {2}",
						new Object[] {block.getLocation(), topFace.getType(), topFace.getLocation()});
			}

			// Consider if should simply be an AcidBlockEvent listener. This will do for now.
			Utility.reinforcementBroken(p, pRein);
			rm.deleteReinforcement(pTopRein);

			// Break the acid block
			block.breakNaturally();

			// Break the acided block
			if (OldCitadelConfigManager.breakAcidedBlockNaturally()) {
				topFace.breakNaturally();
			}
			else {
				topFace.setType(Material.AIR);
			}

		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new ArrayList<String>();
	}
}
