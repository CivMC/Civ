package vg.civcraft.mc.civmodcore.world.locations.chunkmeta.stat;

public class PluginStatistic {
	public final short pluginId;
	public final String pluginName;
	public long chunkLoadCount;
	public long chunkLoadSumNanoSec;
	public long chunkLoadMinTimeNanoSec;
	public long chunkLoadMaxTimeNanoSec;

	public boolean isInitialized;

	public PluginStatistic(short pluginId, String pluginName) {
		this.pluginId = pluginId;
		this.pluginName = pluginName;
	}

	private PluginStatistic(PluginStatistic original) {
		this (original.pluginId, original.pluginName);
		chunkLoadCount = original.chunkLoadCount;
		chunkLoadSumNanoSec = original.chunkLoadSumNanoSec;
		chunkLoadMinTimeNanoSec = original.chunkLoadMinTimeNanoSec;
		chunkLoadMaxTimeNanoSec = original.chunkLoadMaxTimeNanoSec;
		isInitialized = original.isInitialized;
	}

	public PluginStatistic clone() {
		return new PluginStatistic(this);
	}
}
