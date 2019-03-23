package vg.civcraft.mc.civmodcore;

import vg.civcraft.mc.civmodcore.playersettings.gui.ConfigCommand;

public class CivModCorePlugin extends ACivMod {

	private static CivModCorePlugin instance;

	@Override
	public void onEnable() {
		super.onEnable();
		// needed for some of the apis
		instance = this;
		// we cant use the command annotation thing here, because the annotation
		// processor would not be available yet
		this.newCommandHandler.registerCommand(new ConfigCommand());
	}

	public static CivModCorePlugin getInstance() {
		return instance;
	}

	@Override
	protected String getPluginName() {
		return "CivModCore";
	}

}
