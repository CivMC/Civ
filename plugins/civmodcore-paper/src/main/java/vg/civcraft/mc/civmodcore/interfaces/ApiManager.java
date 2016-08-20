package vg.civcraft.mc.civmodcore.interfaces;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class ApiManager {

	public List<IDisabler> disablerApis = new ArrayList<>();

	public boolean isDisabledFor(Player player) {
		boolean disabled = false;
		for (IDisabler disabler : disablerApis) {
			disabled |= disabler.isBergecraftDisabledFor(player);
		}
		return disabled;
	}

}
