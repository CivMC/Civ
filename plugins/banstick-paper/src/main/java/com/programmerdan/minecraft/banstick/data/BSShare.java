package com.programmerdan.minecraft.banstick.data;

import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BSShare {

	private static Map<Long, BSShare> allShareID = new HashMap<Long, BSShare>();
	private static ConcurrentLinkedQueue<WeakReference<BSShare>> dirtyShares = new ConcurrentLinkedQueue<WeakReference<BSShare>>();
	private boolean dirty;
	
	private long sid;
	private BSPlayer firstPlayer;
	private BSPlayer secondPlayer;
	
	private BSSession firstSession;
	private BSSession secondSession;
	
	private Timestamp createTime;
	private Timestamp pardonTime;
	
	private BSShare() {}
	
	public Date getCreateTime() {
		return createTime;
	}
	
	public Date getPardonTime() {
		return pardonTime;
	}
	
	public void setPardonTime(Date pardonTime) {
		setPardonTime(new Timestamp(pardonTime.getTime()));
	}
	
	public void setPardonTime(Timestamp pardonTime) {
		this.pardonTime = pardonTime;
		this.dirty = true;
		dirtyShares.offer(new WeakReference<BSShare>(this));
	}
	
	public boolean isPardoned() {
		return this.pardonTime != null;
	}

	public static BSShare byId(long sid) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void saveDirty() {
		// TODO Auto-generated method stub
		
	}

	public static long preload(long offset, int limit) {
		// TODO Auto-generated method stub
		return -1;
	}
	
	@Override
	public String toString() {
		return "";
	}
	
	public String toFullString(boolean showIPs) {
		return "";
	}
	
}
