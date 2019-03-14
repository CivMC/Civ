package vg.civcraft.mc.citadel.playerstate;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class NormalState extends AbstractPlayerState {

	public NormalState(Player p, boolean bypass) {
		super(p, bypass);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getName() {
		return "Normal mode";
	}

	@Override
	public void handleBlockPlace(BlockPlaceEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleInteractBlock(PlayerInteractEvent e) {

	}

}
