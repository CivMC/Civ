package vg.civcraft.mc.civmodcore.util;

import java.io.IOException;
import java.util.ArrayList;

import vg.civcraft.mc.civmodcore.ACivMod;

import com.google.common.reflect.ClassPath;

public class ClassUtility {
	public static ArrayList<Class<?>> GetClassesForPlugin(ACivMod plugin){
		return GetClassesForPlugin(plugin, null);
	}
	public  static ArrayList<Class<?>> GetClassesForPlugin(ACivMod plugin, Class<?> ofType){
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		try {
	    	Package pack= plugin.getClass().getPackage();
	    	if(pack==null){
	    		plugin.info("Package name REQUIRED for annotation loading");
	    	}
	    	else{

	    		String packageName = pack.getName();		        
		    	ClassLoader classloader = plugin.classLoader;
		        ClassPath path = ClassPath.from(classloader);

		        for (ClassPath.ClassInfo info : path.getTopLevelClassesRecursive(packageName)) {
		        	Class clazz = Class.forName(info.getName(), true, classloader);
		        	if(ofType==null || ofType.isAssignableFrom(clazz)){
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