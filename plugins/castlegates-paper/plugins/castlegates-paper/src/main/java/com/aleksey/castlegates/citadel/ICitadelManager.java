/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.citadel;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.aleksey.castlegates.database.ReinforcementInfo;

public interface ICitadelManager {
	void init();
	void close();
	double getMaxRedstoneDistance();
	boolean canInteract(List<Player> players, Location loc);
	boolean canChange(Player player, Location loc);
	ReinforcementInfo removeReinforcement(Location loc);
	boolean createReinforcement(ReinforcementInfo info, Location loc);
}
