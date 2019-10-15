package vg.civcraft.mc.citadel.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelPermissionHandler;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.events.ReinforcementAcidBlockedEvent;
import vg.civcraft.mc.citadel.model.AcidManager;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.civmodcore.util.TextUtil;

@CivCommand(id = "ctacid")
public class Acid extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player p = (Player) sender;
		Iterator<Block> itr = new BlockIterator(p, 40); // Within 2.5 chunks
		AcidManager acidMan = Citadel.getInstance().getAcidManager();
		while (itr.hasNext()) {
			Block block = itr.next();
			if (!acidMan.isPossibleAcidBlock(block)) {
				CitadelUtility.sendAndLog(p, ChatColor.RED, "That block is not a valid acid block");
				return true;
			}
			Reinforcement reinforcement = ReinforcementLogic.getReinforcementAt(block.getLocation());
			if (reinforcement == null) {
				CitadelUtility.sendAndLog(p, ChatColor.RED, "That block is not reinforced.");
				return true;
			}
			if (!reinforcement.hasPermission(p, CitadelPermissionHandler.getAcidblock())) {
				CitadelUtility.sendAndLog(p, ChatColor.RED,
						"You do not have sufficient permission to use acid blocks on this group.");
				return true;
			}
			long neededTime = acidMan.getRemainingAcidMaturationTime(reinforcement);
			if (neededTime > 0) {
				CitadelUtility.sendAndLog(p, ChatColor.RED, "That acid block will be mature in "
						+ TextUtil.formatDuration(neededTime, TimeUnit.MILLISECONDS));
				return true;
			}
			Block topFace = block.getRelative(BlockFace.UP);
			if (Material.AIR.equals(topFace.getType())) {
				CitadelUtility.sendAndLog(p, ChatColor.RED, "There is no block above to acid block.");
				return true;
			}
			Reinforcement topRein = ReinforcementLogic.getReinforcementProtecting(topFace);
			if (topRein == null) {
				CitadelUtility.sendAndLog(p, ChatColor.RED, "The block above doesn't have a reinforcement.");
				return true;
			}
			if (!acidMan.canAcidBlock(reinforcement.getType(), topRein.getType())) {
				CitadelUtility.sendAndLog(p, ChatColor.RED,
						reinforcement.getType().getName() + " can not acid away " + topRein.getType().getName());
				return true;
			}
			ReinforcementAcidBlockedEvent event = new ReinforcementAcidBlockedEvent(p, reinforcement, topRein);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				if (Citadel.getInstance().getConfigManager().isDebugEnabled()) {
					Citadel.getInstance().getLogger().log(Level.INFO,
							"Acid block event cancelled for acid at " + reinforcement.getLocation());
				}
				return true;
			}

			if (Citadel.getInstance().getConfigManager().logHostileBreaks()) {
				Citadel.getInstance().getLogger().log(Level.INFO, "Acid at {0} broke {1} at {2}, activated by {3}",
						new Object[] { block.getLocation(), topFace.getType(), topFace.getLocation(), p.getName() });
			}
			reinforcement.setHealth(-1);
			// play particles for top block
			ReinforcementLogic.damageReinforcement(topRein, topRein.getHealth() + 1, p);
			block.breakNaturally();
			topFace.breakNaturally();
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return new ArrayList<>();
	}
}
