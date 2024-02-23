package vg.civcraft.mc.civduties.external;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.minelink.ctplus.event.CombatLogEvent;
import vg.civcraft.mc.civduties.CivDuties;

public class CombatTagHandler implements Listener {
	
	@EventHandler
	public void disableCombatTag(CombatLogEvent event) {
		if (CivDuties.getInstance().getModeManager().isInDuty(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

}
