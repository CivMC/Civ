package vg.civcraft.mc.citadel.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelPermissionHandler;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.events.ReinforcementAcidBlockedEvent;
import vg.civcraft.mc.citadel.model.AcidManager;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;
import vg.civcraft.mc.civmodcore.utilities.TextUtil;

public class Acid extends BaseCommand {

	@CommandAlias("ctacid|acid")
	@Description("Removes the block above it if used on an acid block")
	public void execute(Player p) {
		Iterator<Block> itr = new BlockIterator(p, 40); // Within 2.5 chunks
		AcidManager acidMan = Citadel.getInstance().getAcidManager();
		boolean foundAny = false;
		while (itr.hasNext()) {
			Block block = itr.next();
			if (MaterialUtils.isAir(block.getType()) || !(block.getType().isSolid()) || !(block.getType().isOccluding())) {
				continue;
			}
			if (!acidMan.isPossibleAcidBlock(block)) {
				if (!foundAny) {
					CitadelUtility.sendAndLog(p, ChatColor.RED, "That block is not a valid acid block");
				}
				return;
			}
			Reinforcement reinforcement = ReinforcementLogic.getReinforcementAt(block.getLocation());
			if (reinforcement == null) {
				CitadelUtility.sendAndLog(p, ChatColor.RED, "That block is not reinforced.");
				return;
			}
			if (!reinforcement.hasPermission(p, CitadelPermissionHandler.getAcidblock())) {
				CitadelUtility.sendAndLog(p, ChatColor.RED,
						"You do not have sufficient permission to use acid blocks on this group.");
				return;
			}
			long neededTime = acidMan.getRemainingAcidMaturationTime(reinforcement);
			if (neededTime > 0) {
				CitadelUtility.sendAndLog(p, ChatColor.RED, "That acid block will be mature in "
						+ TextUtil.formatDuration(neededTime, TimeUnit.MILLISECONDS));
				return;
			}
			Block topFace = block.getRelative(BlockFace.UP);
			if (MaterialUtils.isAir(topFace.getType())) {
				CitadelUtility.sendAndLog(p, ChatColor.RED, "There is no block above to acid block.");
				return;
			}
			Reinforcement topRein = ReinforcementLogic.getReinforcementProtecting(topFace);
			if (topRein == null) {
				CitadelUtility.sendAndLog(p, ChatColor.RED, "The block above doesn't have a reinforcement.");
				return;
			}
			if (!acidMan.canAcidBlock(reinforcement.getType(), topRein.getType())) {
				CitadelUtility.sendAndLog(p, ChatColor.RED,
						reinforcement.getType().getName() + " can not acid away " + topRein.getType().getName());
				return;
			}
			ReinforcementAcidBlockedEvent event = new ReinforcementAcidBlockedEvent(p, reinforcement, topRein);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				if (Citadel.getInstance().getConfigManager().isDebugEnabled()) {
					Citadel.getInstance().getLogger().log(Level.INFO,
							"Acid block event cancelled for acid at " + reinforcement.getLocation());
				}
				return;
			}

			if (Citadel.getInstance().getConfigManager().logHostileBreaks()) {
				Citadel.getInstance().getLogger().log(Level.INFO, "Acid at {0} broke {1} at {2}, activated by {3}",
						new Object[] { block.getLocation(), topFace.getType(), topFace.getLocation(), p.getName() });
			}
			foundAny = true;
			reinforcement.setHealth(-1);
			// play particles for top block
			ReinforcementLogic.damageReinforcement(topRein, topRein.getHealth() + 1, p);
			block.breakNaturally();
			p.getWorld().dropItemNaturally(reinforcement.getLocation(), reinforcement.getType().getItem());
			if (!acidContainerBlock(topFace)) {
				topFace.breakNaturally();
			}
		}
	}

	/**
	 * Checks if a containers contents can be dropped
	 * @param  block  Container being acid blocked
	 * @return true if contents have been successfully dropped
	 */

	public boolean acidContainerBlock(Block block) {
		if (!(block instanceof Container)) {
			return false;
		}
		Container container = (Container) block.getBlockData();
		ItemStack[] items = container.getInventory().getContents();
		container.getInventory().clear();
		for (ItemStack item : items) {
			block.getWorld().dropItemNaturally(block.getLocation(), item);
		}
		return true;
	}
}
