package vg.civcraft.mc.civmodcore.util;

import com.google.common.base.Strings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import vg.civcraft.mc.civmodcore.ACivMod;

public final class ResourceUtils {

	/**
	 * Iterates through the lines of a resource file. Good for CSV, TSV, LSV.. etc
	 *
	 * @param plugin The plugin to get the resource from.
	 * @param resource The resource path.
	 * @param parser The line parser.
	 * @return Returns true if the resource was iterated.
	 */
	public static boolean iterateResourceLines(ACivMod plugin, String resource, Consumer<String> parser) {
		if (plugin == null) {
			throw new IllegalArgumentException("Cannot iterate resource with a null plugin.");
		}
		if (Strings.isNullOrEmpty(resource)) {
			throw new IllegalArgumentException("Resource path must be valid.");
		}
		if (parser == null) {
			throw new IllegalArgumentException("Not parser was given.");
		}
		InputStream input = plugin.getClass().getResourceAsStream(resource);
		if (input == null) {
			return false;
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		String line = null;
		while (true) {
			try {
				line = reader.readLine();
			}
			catch (IOException error) {
				plugin.warning("Could not read line.", error);
				continue;
			}
			if (line == null) {
				break;
			}
			parser.accept(line);
		}
		return true;
	}

}
