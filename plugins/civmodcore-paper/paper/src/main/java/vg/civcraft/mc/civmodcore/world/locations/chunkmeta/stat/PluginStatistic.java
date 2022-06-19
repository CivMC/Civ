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

	public PluginStatistic clone() {
		PluginStatistic clone = new PluginStatistic(this.pluginId, this.pluginName);
		clone.chunkLoadCount = this.chunkLoadCount;
		clone.chunkLoadSumNanoSec = this.chunkLoadSumNanoSec;
		clone.chunkLoadMinTimeNanoSec = this.chunkLoadMinTimeNanoSec;
		clone.chunkLoadMaxTimeNanoSec = this.chunkLoadMaxTimeNanoSec;
		clone.isInitialized = this.isInitialized;

		return clone;
	}
}
