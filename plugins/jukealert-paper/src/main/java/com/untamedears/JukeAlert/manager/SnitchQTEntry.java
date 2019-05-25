package com.untamedears.JukeAlert.manager;

import com.untamedears.JukeAlert.model.Snitch;

import vg.civcraft.mc.civmodcore.locations.QTBox;

public class SnitchQTEntry implements QTBox {

	private Snitch snitch;
	private int lowerX;
	private int upperX;
	private int lowerZ;
	private int upperZ;

	public SnitchQTEntry(Snitch snitch, int lowerZ, int upperZ, int lowerX, int upperX) {
		this.snitch = snitch;
		this.lowerZ = lowerZ;
		this.upperZ = upperZ;
		this.lowerX= lowerX;
		this.upperX = upperX;
	}
	
	public Snitch getSnitch() {
		return snitch;
	}

	@Override
	public int qtXMin() {
		return lowerX;
	}

	@Override
	public int qtXMid() {
		return (upperX + lowerX) / 2;
	}

	@Override
	public int qtXMax() {
		return upperX;
	}

	@Override
	public int qtZMin() {
		return lowerZ;
	}

	@Override
	public int qtZMid() {
		return (upperZ + lowerZ) / 2;
	}

	@Override
	public int qtZMax() {
		return upperZ;
	}

}
