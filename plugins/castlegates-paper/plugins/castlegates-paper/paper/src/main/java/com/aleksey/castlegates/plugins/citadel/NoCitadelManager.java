/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.plugins.citadel;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.aleksey.castlegates.CastleGates;
import com.aleksey.castlegates.database.ReinforcementInfo;

public class NoCitadelManager implements ICitadelManager {
	@Override
	public void init() {

	}

	@Override
	public void close() {

	}

	@Override
	public double getMaxRedstoneDistance() {
		return CastleGates.getConfigManager().getMaxRedstoneDistance();
	}

	@Override
	public ICitadel getCitadel(List<Player> players, Location loc) { return new NoCitadel(); }

	@Override
	public boolean isReinforced(Location loc) { return false; }

	@Override
	public boolean canBypass(Player player, Location loc) {
		return true;
	}

	@Override
	public boolean canViewInformation(Player player, Location loc) {
		return true;
	}

	@Override
	public ReinforcementInfo removeReinforcement(Location loc) {
		return null;
	}

	@Override
	public boolean createReinforcement(ReinforcementInfo info, Location loc) {
		return false;
	}

	@Override
	public boolean isReinforcingStateActive(Player player) {
		return false;
	}
}
