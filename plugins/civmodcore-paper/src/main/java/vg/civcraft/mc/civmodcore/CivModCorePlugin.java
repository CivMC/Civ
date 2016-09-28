package vg.civcraft.mc.civmodcore;

/**
 * The sole purpose of this class is to make Spigot recognize this library as a plugin and automatically
 * load the classes onto the classpath for us.
 * <p>
 * Replaces Dummy class.
 */
public class CivModCorePlugin extends ACivMod {
	
	private static CivModCorePlugin instance;
	
	public void onEnable() {
		super.onEnable();
		//needed for some of the apis
		instance = this;
	}

	public static CivModCorePlugin getInstance() {
		return instance;
	}

	@Override
	protected String getPluginName() {
		return "CivModCore";
	}

}
