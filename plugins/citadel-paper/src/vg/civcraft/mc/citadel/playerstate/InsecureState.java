package vg.civcraft.mc.citadel.playerstate;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.citadel.model.Reinforcement;

public class InsecureState extends IPlayerState {

	public InsecureState(Player p, boolean bypass) {
		super(p, bypass);
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
		if (rein.hasPermission(e.getPlayer(), Citadel.insecurePerm)) {
			rein.toggleInsecure();
			if (rein.isInsecure()) {
				Utility.sendAndLog(e.getPlayer(), ChatColor.YELLOW, e.getClickedBlock().getType().name() + " is now insecure");
			} else {
				Utility.sendAndLog(e.getPlayer(), ChatColor.GREEN, e.getClickedBlock().getType().name() + " is now secure");
			}
		} else {
			Utility.sendAndLog(e.getPlayer(), ChatColor.RED, "You are not allowed to make this reinforcement insecure");
		}
	}

}
