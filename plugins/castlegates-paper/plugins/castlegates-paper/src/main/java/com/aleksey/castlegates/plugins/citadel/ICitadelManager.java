/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.plugins.citadel;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.aleksey.castlegates.database.ReinforcementInfo;

public interface ICitadelManager {
	void init();
	void close();
	double getMaxRedstoneDistance();
	ICitadel getCitadel(List<Player> players, Location loc);
	boolean canBypass(Player player, Location loc);
	boolean canViewInformation(Player player, Location loc);
	ReinforcementInfo removeReinforcement(Location loc);
	boolean createReinforcement(ReinforcementInfo info, Location loc);
}
