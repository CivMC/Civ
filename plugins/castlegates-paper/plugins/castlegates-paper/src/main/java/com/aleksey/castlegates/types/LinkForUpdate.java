package com.aleksey.castlegates.types;

import java.util.List;

import com.aleksey.castlegates.database.ReinforcementInfo;

public class LinkForUpdate {
	public GearblockLink original;
	public Gearblock gearblock1;
	public Gearblock gearblock2;
	public byte[] blocks;
	public List<ReinforcementInfo> reinforcements;
}
