package vg.civcraft.mc.civmodcore.world.locations;

import org.bukkit.Location;

public class QTBoxImpl implements QTBox {

	private final int lowerXBound;
	private final int upperXBound;
	private final int lowerZBound;
	private final int upperZBound;

	public QTBoxImpl(int lowerXBound, int upperXBound, int lowerZBound, int upperZBound) {
		this.lowerXBound = lowerXBound;
		this.upperXBound = upperXBound;
		this.lowerZBound = lowerZBound;
		this.upperZBound = upperZBound;
	}

	public QTBoxImpl(Location center, int range) {
		this(center.getBlockX() - range, center.getBlockX() + range, center.getBlockZ() - range,
				center.getBlockZ() + range);
	}

	@Override
	public int qtXMin() {
		return lowerXBound;
	}

	@Override
	public int qtXMid() {
		return (lowerXBound + upperXBound / 2);
	}

	@Override
	public int qtXMax() {
		return upperXBound;
	}

	@Override
	public int qtZMin() {
		return lowerZBound;
	}

	@Override
	public int qtZMid() {
		return (lowerZBound + upperZBound / 2);
	}

	@Override
	public int qtZMax() {
		return upperZBound;
	}

}
