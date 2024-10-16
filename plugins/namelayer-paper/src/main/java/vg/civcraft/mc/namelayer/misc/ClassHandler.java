package vg.civcraft.mc.namelayer.misc;

import org.bukkit.Server;

/*
 * Thank you orbfuscator for showing me how to do this.
 */
public class ClassHandler {

	public static ClassHandler ch;

	private String version;

	public static boolean properlyEnabled;

	public static boolean Initialize(Server server) {
		ch = new ClassHandler();
		String packageName = server.getClass().getPackage().getName();
		ch.version = packageName.substring(packageName.lastIndexOf('.') + 1);
		try {
			Class.forName("vg.civcraft.mc.namelayer.misc.ProfileModifier");
			vg.civcraft.mc.namelayer.gui.AbstractGroupGUI.matsInit();
			return properlyEnabled = true;
		} catch (Exception e) {
			e.printStackTrace();
			return properlyEnabled = false;
		}
	}

	public ProfileInterface getProfileClass() {
		return (ProfileInterface) getObject(ProfileInterface.class, "ProfileModifier");
	}

	public MaterialInterface getMaterialClass() {
		return (MaterialInterface) getObject(MaterialInterface.class, "MaterialMap");
	}

	private Object getObject(Class<? extends Object> Class, String name) {
		try {
			Class<?> internalClass = Class.forName("vg.civcraft.mc.namelayer.misc." + name);
			if (internalClass.isAssignableFrom(internalClass))
				return internalClass.getConstructor().newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
