package com.programmerdan.minecraft.simpleadminhacks.configs;

import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;

public class SanityHackConfig extends SimpleHackConfig {

	private boolean trackPlace; //Track block placed
	private boolean trackBreak; //Track block broken
	//private boolean trackOpen; //Track block opened (chest)
	private int belowYLevel; //Track things below this y level
	
	public SanityHackConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		this.trackPlace = config.getBoolean("trackPlace", true);
		plugin().log(Level.INFO, "Tracking placement: {0}", this.trackPlace);
		
		this.trackBreak = config.getBoolean("trackBreak", true);
		plugin().log(Level.INFO, "Tracking breaking: {0}", this.trackBreak);
		
		/*this.trackOpen = config.getBoolean("trackOpen", false);
		plugin().log(Level.INFO, "Tracking opening: {0}", this.trackOpen);*/
		
		this.belowYLevel = config.getInt("belowYLevel", 7);
		plugin().log(Level.INFO, "Tracking below Y: {0}", this.belowYLevel);
	}
	
	public boolean isTrackingPlace(){
		return this.trackPlace;
	}
	
	public boolean isTrackingBreak(){
		return this.trackBreak;
	}
	
	/*public boolean isTrackingOpen(){
		return this.trackOpen;
	}*/
	
	public int getTrackingLevel(){
		return this.belowYLevel;
	}

}
