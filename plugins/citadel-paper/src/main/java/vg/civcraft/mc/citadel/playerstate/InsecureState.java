package vg.civcraft.mc.citadel.playerstate;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import vg.civcraft.mc.citadel.CitadelPermissionHandler;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.Reinforcement;

public class InsecureState extends AbstractPlayerState {

	public InsecureState(Player p) {
		super(p);
	}

	@Override
	public String getName() {
		return "Insecure mode";
	}

	@Override
	public void handleBlockPlace(BlockPlaceEvent e) {
	}

	@Override
	public void handleInteractBlock(PlayerInteractEvent e) {
		Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(e.getClickedBlock());
		if (rein == null) {
			return;
		}
		e.setCancelled(true);
		if (rein.hasPermission(e.getPlayer(), CitadelPermissionHandler.getInsecure())) {
			rein.toggleInsecure();
			if (rein.isInsecure()) {
				CitadelUtility.sendAndLog(e.getPlayer(), ChatColor.YELLOW,
						e.getClickedBlock().getType().name() + " is now insecure");
			} else {
				CitadelUtility.sendAndLog(e.getPlayer(), ChatColor.GREEN,
						e.getClickedBlock().getType().name() + " is now secure");
			}
		} else {
			CitadelUtility.sendAndLog(e.getPlayer(), ChatColor.RED, "You are not allowed to make this reinforcement insecure");
		}
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof InsecureState;
	}

	@Override
	public String getOverlayText() {
		return ChatColor.GREEN + "CTIN";
	}

}
