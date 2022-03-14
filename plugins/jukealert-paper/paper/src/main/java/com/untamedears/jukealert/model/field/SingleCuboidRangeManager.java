package com.untamedears.jukealert.model.field;

import com.google.common.collect.Lists;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.SnitchQTEntry;
import java.util.Collection;
import org.bukkit.Location;

public class SingleCuboidRangeManager extends VariableSizeCuboidRangeManager {

	public SingleCuboidRangeManager(int range, Snitch snitch) {
		super(range, range, snitch);
	}

}
