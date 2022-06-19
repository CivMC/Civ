package vg.civcraft.mc.civmodcore.world.locations.chunkmeta.stat;

import org.bukkit.World;

import java.util.List;

public class LoadStatistic {
	public static class ThreadTime {
		public final int threadIndex;
		public final long time;

		public ThreadTime(int threadIndex, Long time) {
			this.threadIndex = threadIndex;
			this.time = time;
		}
	}

	public static class WorldThreads {
		public final World world;
		public final Long mainThreadTime;
		public final List<ThreadTime> threadTimes;

		public WorldThreads(World world, Long mainThreadTime, List<ThreadTime> threadTimes) {
			this.world = world;
			this.mainThreadTime = mainThreadTime;
			this.threadTimes = threadTimes;
		}
	}

	public final int worldCount;
	public final int threadCount;
	public final List<WorldThreads> worldThreadsList;
	public final List<PluginStatistic> pluginStatistics;

	public LoadStatistic(int worldCount, int threadCount, List<WorldThreads> worldThreadsList, List<PluginStatistic> pluginStatistics) {
		this.worldCount = worldCount;
		this.threadCount = threadCount;
		this.worldThreadsList = worldThreadsList;
		this.pluginStatistics = pluginStatistics;
	}
}
