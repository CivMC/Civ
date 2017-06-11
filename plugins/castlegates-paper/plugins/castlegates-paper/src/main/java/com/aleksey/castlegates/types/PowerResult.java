package com.aleksey.castlegates.types;

import org.bukkit.block.Block;

public class PowerResult {
	public static enum Status {
		Unchanged,
		Unpowered,
		Blocked,
		Broken,
		Drawn,
		Undrawn,
		CannotDrawGear,
		NotInCitadelGroup,
		DifferentCitadelGroup,
		BastionBlocked,
		Locked,
		Allowed
		}

	public Status status;
	public Block block;

	public PowerResult(Status status, Block block) {
		this.status = status;
		this.block = block;
	}

	public static final PowerResult Unchanged = new PowerResult(Status.Unchanged, null);
	public static final PowerResult Unpowered = new PowerResult(Status.Unpowered, null);
	public static final PowerResult Drawn = new PowerResult(Status.Drawn, null);
	public static final PowerResult Undrawn = new PowerResult(Status.Undrawn, null);
	public static final PowerResult NotInCitadelGroup = new PowerResult(Status.NotInCitadelGroup, null);
	public static final PowerResult DifferentCitadelGroup = new PowerResult(Status.DifferentCitadelGroup, null);
	public static final PowerResult BastionBlocked = new PowerResult(Status.BastionBlocked, null);
	public static final PowerResult Allowed = new PowerResult(Status.Allowed, null);
	public static final PowerResult Locked = new PowerResult(Status.Locked, null);
}
