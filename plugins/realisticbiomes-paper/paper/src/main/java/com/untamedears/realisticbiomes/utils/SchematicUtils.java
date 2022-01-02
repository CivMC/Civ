package com.untamedears.realisticbiomes.utils;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.untamedears.realisticbiomes.model.RBSchematic;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SchematicUtils {

	public static List<RBSchematic> loadAll(File folder, Logger logger) {
		List<RBSchematic> result = new ArrayList<>();
		if (!folder.isDirectory()) {
			logger.warning("No schematic directory provided");
			return result;
		}
		for (File subFile : folder.listFiles()) {
			if (subFile.isDirectory()) {
				result.addAll(loadAll(subFile, logger));
				continue;
			}
			if (!subFile.getName().endsWith(".schematic")) {
				continue;
			}
			Clipboard board;
			try {
				board = loadSchematic(subFile);
			}
			catch (IOException e) {
				logger.severe("Failed to load schematic "+ e.toString() );
				continue;
			}
			String name = subFile.getName().replace(".schematic", "");
			result.add(new RBSchematic(name, board));
		}
		return result;
	}

	public static Clipboard loadSchematic(File file) throws IOException {
		ClipboardFormat format = ClipboardFormats.findByFile(file);
		try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
			return reader.read();
		}
	}

}
