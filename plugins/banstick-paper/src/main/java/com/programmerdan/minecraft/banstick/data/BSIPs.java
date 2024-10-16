package com.programmerdan.minecraft.banstick.data;

import com.programmerdan.minecraft.banstick.BanStick;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * BSIPs management structure. Helper / support methods.
 * 
 * @author <a href="mailto:programmerdan@gmail.com">ProgrammerDan</a>
 *
 */
public final class BSIPs {

	private BSPlayer forPlayer;
	
	private List<Long> ipList;
	private Set<Long> ipSet;
	
	private BSIPs() { }
	
	/**
	 * Get a BSIPs for a specific player, which provides access to an IP list for a player.
	 * 
	 * @param newPlayer 
	 * @return
	 */
	public static BSIPs onlyFor(BSPlayer newPlayer) {
		BSIPs bsips = new BSIPs();
		bsips.forPlayer = newPlayer;
		bsips.ipList = null;
		bsips.ipSet = null;
		return bsips;
	}
	
	/**
	 * Release (perhaps on logout) all the ips for a particular BSIPs object.
	 * @param bsips the BSIPs to release.
	 */
	public static void release(BSIPs bsips) {
		if (bsips != null) {
			if (bsips.ipList != null) {
				bsips.ipList.clear();
			}
			bsips.ipList = null;
			if (bsips.ipSet != null) {
				bsips.ipSet.clear();
			}
			bsips.ipSet = null;
			bsips.forPlayer = null;
		}
	}
	
	/**
	 * @return the latest BSIP from this BSIPs
	 */
	public BSIP getLatest() {
		if (ipList == null) {
			fill();
		}
		if (ipList.isEmpty()) {
			return null;
		}
		return BSIP.byId(ipList.get(ipList.size() - 1));
	}
	
	/**
	 * @return the full set of BSIP for a particular individual.
	 */
	public List<BSIP> getAll() {
		if (ipList == null) {
			fill();
		}
		List<BSIP> ret = new ArrayList<>(ipList.size());
		for (Long iid : ipList) {
			ret.add(BSIP.byId(iid));
		}
		
		return ret;
	}
	
	/**
	 * Sets the given IP as latest; reorders if already exists in the list.
	 * 
	 * @param ip The ip to set as latest.
	 */
	public void setLatest(BSIP ip) {
		if (ipList == null) {
			fill();
		}
		if (ipSet.add(ip.getId())) {
			ipList.add(ip.getId());
		} else {
			// find and move.
			int idp = ipList.indexOf(ip.getId());
			if (idp < ipList.size() - 1) { // not last elem already
				ipList.add(ipList.remove(idp));
			}
		}
	}

	/**
	 * Internal fill of BSIP for a particular player.
	 */
	private void fill() {
		if (ipList == null) {
			ipList = new ArrayList<>();
		}
		if (ipSet == null) {
			ipSet = new HashSet<>();
		}

		try (Connection connection = BanStickDatabaseHandler.getInstanceData().getConnection();
			 PreparedStatement getIDs = connection.prepareStatement(// Get all ids only, order by join time.
						"SELECT DISTINCT i.iid FROM bs_ip i JOIN bs_session s ON i.iid = s.iid WHERE s.pid = ? ORDER BY s.join_time;");) {
			// TODO: replace statement w/ view.
			getIDs.setLong(1, forPlayer.getId());
			BanStick.getPlugin().debug("Filling IPs for {0}", forPlayer.getUUID());
			try (ResultSet rs = getIDs.executeQuery()) {
				while (rs.next()) {
					Long id = rs.getLong(1);
					if (ipSet.add(id)) {
						ipList.add(rs.getLong(1));
					}
				}
				if (ipList.isEmpty() || ipSet.isEmpty()) {
					BanStick.getPlugin().warning("No IPs for " + forPlayer.getName());
				}
			}
		} catch (SQLException se) {
			BanStick.getPlugin().severe("Failed to get list of IP ids", se);
		}
	}
	
}
