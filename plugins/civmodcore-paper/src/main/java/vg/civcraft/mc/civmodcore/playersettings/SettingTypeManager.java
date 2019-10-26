package vg.civcraft.mc.civmodcore.playersettings;

import java.util.HashMap;
import java.util.Map;

import vg.civcraft.mc.civmodcore.playersettings.impl.BooleanSetting;
import vg.civcraft.mc.civmodcore.playersettings.impl.DoubleSetting;
import vg.civcraft.mc.civmodcore.playersettings.impl.FloatSetting;
import vg.civcraft.mc.civmodcore.playersettings.impl.IntegerSetting;
import vg.civcraft.mc.civmodcore.playersettings.impl.LongSetting;
import vg.civcraft.mc.civmodcore.playersettings.impl.StringSetting;

public class SettingTypeManager {
	
	private static Map<Class<?>, PlayerSetting<?>> settings; 
	
	@SuppressWarnings("unchecked")
	public static <T> PlayerSetting<T> getSetting(Class <T> clazz) {
		return (PlayerSetting<T>) settings.get(clazz);
	}
	
	static {
		settings = new HashMap<>();
		registerType(Boolean.class, new BooleanSetting(null, false, null, null, null));
		registerType(Integer.class, new IntegerSetting(null, 0, null, null, null, null, false));
		registerType(Float.class, new FloatSetting(null, 0.0F, null, null, null, null));
		registerType(Double.class, new DoubleSetting(null, 0.0, null, null, null, null));
		registerType(Long.class, new LongSetting(null, 0L, null, null, null, null));
		registerType(String.class, new StringSetting(null, null, null, null, null, null));
	}
	
	public static <T> void registerType(Class<T> clazz, PlayerSetting<T> setting) {
		settings.put(clazz, setting);
	}

}
