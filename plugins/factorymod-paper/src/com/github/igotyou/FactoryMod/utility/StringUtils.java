package com.github.igotyou.FactoryMod.utility;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

public class StringUtils {
	
	/**
	 * Formats a Location's coordinates as "world (x y z)"
	 */
	public static String formatCoords(Location loc) {
		return String.format("%s (%d %d %d)", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
	
	public static List<String> splitLines(String paragraph, int lineLength) {
		List<String> lines = new ArrayList<String>();
		int lineStart = 0;
		int lastSpace = -1;
		while (true) {
			int nextSpace = paragraph.indexOf(' ', lastSpace + 1);
			if (nextSpace == -1) {
				// End of paragraph
				lines.add(paragraph.substring(lineStart, paragraph.length()));
				break;
			} else if (nextSpace - lineStart > lineLength) {
				if (lastSpace + 1 == lineStart) {
					// Block of more than lineLength without a space in it - has to be one line
					lastSpace = nextSpace;
				}

				// End of line at last space
				lines.add(paragraph.substring(lineStart, lastSpace));
				lineStart = lastSpace + 1;
			} else {
				lastSpace = nextSpace;
			}
		}
		return lines;
	}

	public static String limitLengthEllipsis(String in, int lengthLimit) {
		return limitLengthEllipsis(in, lengthLimit, "...");
	}
	
	public static String limitLengthEllipsis(String in, int lengthLimit, String ellipsisText) {
		if (in.length() > lengthLimit) {
			return in.substring(0, lengthLimit - ellipsisText.length()) + ellipsisText;
		} else {
			return in;
		}
	}
	
	public static String combineLines(List<String> lines) {
		StringBuilder sb = new StringBuilder();
		boolean firstLine = true;
		for (String line : lines) {
			if (firstLine) {
				sb.append("\n");
				firstLine = false;
			}
			sb.append(line);
		}
		return sb.toString();
	}
}
