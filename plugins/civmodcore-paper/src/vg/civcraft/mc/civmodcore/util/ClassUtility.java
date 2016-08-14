package vg.civcraft.mc.civmodcore.util;

import static vg.civcraft.mc.civmodcore.CivModCorePlugin.log;

import java.io.IOException;
import java.util.ArrayList;

import com.google.common.reflect.ClassPath;
import vg.civcraft.mc.civmodcore.ACivMod;

public class ClassUtility {

	public static ArrayList<Class<?>> GetClassesForPlugin(ACivMod plugin) {
		return GetClassesForPlugin(plugin, null);
	}

	public static ArrayList<Class<?>> GetClassesForPlugin(ACivMod plugin, Class<?> ofType) {
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		try {
			Package pack = plugin.getClass().getPackage();
			if (pack == null) {
				log().info("Package name REQUIRED for annotation loading");
			} else {

				String packageName = pack.getName();
				ClassLoader classloader = plugin.classLoader;
				ClassPath path = ClassPath.from(classloader);

				for (ClassPath.ClassInfo info : path.getTopLevelClassesRecursive(packageName)) {
					Class clazz = null;
					try {
						clazz = Class.forName(info.getName(), true, classloader);
					} catch (NoClassDefFoundError e) {
						log().info("CivModCore failed to load class " + info.getName()
							+ ", you could be missing a dependency. This message is more of an info "
							+ "message useful for debugging. If you see this message you can usually "
							+ "ignore it unless the plugin itself throws an error.");
						continue;
					}
					if (ofType == null || ofType.isAssignableFrom(clazz)) {
						classes.add(clazz);
					}
				}
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		return classes;
	}
}
