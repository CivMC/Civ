package vg.civcraft.mc.civmodcore;

/**
 * The sole purpose of this class is to make Spigot recognize this library as a plugin and automatically
 * load the classes onto the classpath for us.
 * <p>
 * Replaces Dummy class.
 */
public class CivModCorePlugin extends ACivMod {

	@Override
	public void onLoad() {
		//Don't want it to load config
	}

	@Override
	public void onEnable() {
		//Don't want it to load commands/events
	}

	@Override
	protected String getPluginName() {
		return "CivModCore";
	}

}
