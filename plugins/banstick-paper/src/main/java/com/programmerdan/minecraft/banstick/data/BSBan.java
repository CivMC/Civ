package com.programmerdan.minecraft.banstick.data;

import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BSBan {

	private static Map<Long, BSBan> allBanID = new HashMap<Long, BSBan>();
	private static ConcurrentLinkedQueue<WeakReference<BSBan>> dirtySessions = new ConcurrentLinkedQueue<WeakReference<BSBan>>();
	private boolean dirty;
	
	private BSBan() {}
	
	/*
	 * 					" bid BIGINT AUTOINCREMENT PRIMARY KEY," +
					" ban_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
					" ip_ban REFERENCES bs_ip(iid)," +
					" vpn_ban REFERENCES bs_vpn(vid)," +
					" share_ban REFERENCES bs_share(sid)," +
					" admin_ban BOOLEAN," +
					" message TEXT," +
					" ban_end TIMESTAMP," +
	 */
	
	private long bid; 
	private Timestamp banTime;
	private BSIP ipBan;
	private BSVPN vpnBan;
	private BSShare shareBan;
	private String message; //mutable
	private Timestamp banEnd; //mutable
	
	public static BSBan byId(long bid) {
		return null;
	}

	public long getId() {
		return this.bid;
	}

}
