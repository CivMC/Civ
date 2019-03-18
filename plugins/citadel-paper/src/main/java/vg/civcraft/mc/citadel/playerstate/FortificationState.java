package vg.civcraft.mc.citadel.playerstate;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.namelayer.group.Group;

public class FortificationState extends AbstractPlayerState {

	private ReinforcementType type;
	private Group group;

	public FortificationState(Player p, boolean bypass, ReinforcementType type, Group group) {
		super(p, bypass);
		this.type = type;
		this.group = group;
	}

	public Group getGroup() {
		return group;
	}

	public String getName() {
		return "Fortifying mode with " + ChatColor.AQUA + type.getName() + ChatColor.YELLOW + " on "
				+ ChatColor.LIGHT_PURPLE + group.getName();
	}

	public ReinforcementType getType() {
		return type;
	}

	@Override
	public void handleBlockPlace(BlockPlaceEvent e) {
		boolean hadError = Utility.attemptReinforcementCreation(e.getBlock(), type, group, e.getPlayer());
		if (hadError) {
			e.setCancelled(true);
			Citadel.getInstance().getStateManager().setState(e.getPlayer(), null);
		}
	}

	@Override
	public void handleInteractBlock(PlayerInteractEvent e) {
	}
}
