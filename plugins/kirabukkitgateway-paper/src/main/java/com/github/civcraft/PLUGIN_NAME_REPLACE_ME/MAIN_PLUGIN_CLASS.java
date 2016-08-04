package com.github.civcraft.PLUGIN_NAME_REPLACE_ME;

import com.github.civcraft.PLUGIN_NAME_REPLACE_ME.commands.CustomCommandHandler;

import vg.civcraft.mc.civmodcore.ACivMod;

public class MAIN_PLUGIN_CLASS extends ACivMod {

    private static MAIN_PLUGIN_CLASS instance;

    public void onEnable() {
	super.onEnable();
	instance = this;
	handle = new CustomCommandHandler();
	handle.registerCommands();
	
	
    }

    public void onDisable() {

    }

    @Override
    protected String getPluginName() {
	return "PLUGIN_NAME_REPLACE_ME";
    }

    public MAIN_PLUGIN_CLASS getInstance() {
	return instance;
    }

}
