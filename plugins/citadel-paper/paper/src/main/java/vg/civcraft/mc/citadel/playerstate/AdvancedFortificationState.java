package vg.civcraft.mc.citadel.playerstate;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.namelayer.group.Group;

public class AdvancedFortificationState extends AbstractPlayerState {

	private Map<ItemStack, ReinforcingSetup> setups;

	public AdvancedFortificationState(Player p) {
		super(p);
		setups = new HashMap<>();
	}

	@Override
	public String getName() {
		return "Advanced fortification mode";
	}

	@Override
	public void handleBlockPlace(BlockPlaceEvent e) {
		ItemStack placed = e.getItemInHand();
		Player player = e.getPlayer();
		// check if we have a setup for this block, we need a copy for lookup so we can
		// set amount to 1
		ItemStack lookUpCopy = placed.clone();
		lookUpCopy.setAmount(1);
		ReinforcingSetup setup = setups.get(lookUpCopy);
		if (setup == null) {
			CitadelUtility.sendAndLog(player, ChatColor.RED,
					"No setup configured for this block type, no reinforcement was applied");
			return;
		}
		boolean hadError = CitadelUtility.attemptReinforcementCreation(e.getBlock(), setup.type, setup.group, e.getPlayer());
		if (hadError) {
			e.setCancelled(true);
			Citadel.getInstance().getStateManager().setState(e.getPlayer(), null);
		}
	}

	public void addSetup(ItemStack forBlock, ReinforcementType type, Group group) {
		ItemStack copy = forBlock.clone();
		copy.setAmount(1);
		ReinforcingSetup existing = setups.get(copy);
		setups.put(copy, new ReinforcingSetup(type, group));
		Player player = Bukkit.getPlayer(uuid);
		if (player != null) {
			if (existing != null) {
				CitadelUtility.sendAndLog(player, ChatColor.GOLD,
						"Replaced existing setup with " + ChatColor.AQUA + existing.type.getName() + ChatColor.GOLD
								+ " on " + ChatColor.LIGHT_PURPLE + existing.group.getName() + ChatColor.GOLD + "for "
								+ copy.getType() + " with " + ChatColor.AQUA + type.getName() + ChatColor.GOLD + " on "
								+ ChatColor.LIGHT_PURPLE + group.getName());
			} else {
				CitadelUtility.sendAndLog(player, ChatColor.GOLD, copy.getType() + " will be reinforced with " + ChatColor.AQUA
						+ type.getName() + ChatColor.GOLD + " on " + ChatColor.LIGHT_PURPLE + group.getName());
			}
		}
	}

	@Override
	public void handleInteractBlock(PlayerInteractEvent e) {

	}

	private class ReinforcingSetup {

		ReinforcementType type;
		Group group;

		ReinforcingSetup(ReinforcementType type, Group group) {
			this.type = type;
			this.group = group;
		}

	}

	@Override
	public boolean equals(Object o) {
		//just always make a new state for this one
		return false;
	}

	@Override
	public String getOverlayText() {
		return ChatColor.GOLD + "CTAF";
	}

}
