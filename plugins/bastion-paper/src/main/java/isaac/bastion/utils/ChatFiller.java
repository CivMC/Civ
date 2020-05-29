/**
 * Created by Aleksey on 13.08.2017.
 */
// Adapted from https://github.com/andfRa/Saga

package isaac.bastion.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;

public class ChatFiller {
	/**
	 * Default character length.
	 */
	public final static Double DEFAULT_LENGTH = 3.0 / 2.0;

	/**
	 * Maximum character length.
	 */
	public final static Double MAX_LENGTH = 3.0 / 2.0;

	/**
	 * Gap fill string maximum size.
	 */
	private final static Double MAX_GAP = 1.25;

	/**
	 * Chat width.
	 */
	public final static Double CHAT_WIDTH = 80.0;

	/**
	 * Size map.
	 */
	private final static HashMap<Character, Double> SIZE_MAP = new HashMap<Character, Double>() {

		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		{
			put('i', 0.5);
			put('k', 5.0 / 4.0);
			put('t', 1.0);
			put('f', 5.0 / 4.0);
			put('(', 5.0 / 4.0);
			put(')', 5.0 / 4.0);
			put('<', 5.0 / 4.0);
			put('>', 5.0 / 4.0);
			put('{', 5.0 / 4.0);
			put('}', 5.0 / 4.0);
			put(',', 1.0 / 2.0);
			put('.', 1.0 / 2.0);
			put('[', 1.0);
			put(']', 1.0);
			put('I', 1.0);
			put('|', 1.0 / 2.0);
			put('*', 5.0 / 4.0);
			put('"', 5.0 / 4.0);
			put('|', 0.5);
			put('!', 0.5);
			put(':', 0.5);
			put('l', 3.0 / 4.0);
			put('.', 1.0 / 2.0);
			put('\'', 3.0 / 4.0);
			put(' ', 1.0 / 1.0);
			put('\"', 5.0 / 4.0);
			put('`', 0.5);
			put('\0', 0.0);

			put(' ', 1.0);

			put('\u2500', 5.0 / 4.0);
			put('\u2502', 1.0 / 4.0);
			put('\u250C', 3.0 / 4.0);
			put('\u2510', 3.0 / 4.0);
			put('\u2514', 3.0 / 4.0);
			put('\u2518', 3.0 / 4.0);

			put('\u2550', 5.0 / 4.0);
			put('\u2551', 1.0 / 2.0);

			put('\u2554', 3.0 / 4.0);
			put('\u2560', 3.0 / 4.0);
			put('\u255A', 3.0 / 4.0);

			put('\u2557', 4.0 / 4.0);
			put('\u2563', 4.0 / 4.0);
			put('\u255D', 4.0 / 4.0);

			put('\u2591', 2.0);
		}
	};

	/**
	 * Gap fill chars.
	 */
	private final static HashSet<Character> FILL_CHARS = new HashSet<Character>() {

		private static final long serialVersionUID = 1L;
		{
			add(' ');
		}
	};

	/**
	 * Trims and/or fills a string to be as close to the required length without exceeding it.
	 * If the string is trimmed, the supplied suffix is added (provided that won't exceed reqLength)
	 * @param str string to trim/fill
	 * @param reqLength required length
	 * @return the trimmed/filled string
	 */
	public static String fillString(String str, Double reqLength, String suffix) {

		double suffixLength = calcLength(suffix);
		char[] chars = str.toCharArray();
		StringBuffer result = new StringBuffer();
		StringBuffer suffixResult = new StringBuffer();
		Double length = 0.0;

		// Cut size and add suffix if necessary
		boolean suffixFilled = false;
		for (int i = 0; i < chars.length; i++) {
			Double charLength = SIZE_MAP.get(chars[i]);
			if (charLength == null) {
				charLength = DEFAULT_LENGTH;
			}

			if (!suffixFilled && length + charLength + suffixLength > reqLength) {
				suffixFilled = true;
				suffixResult.append(suffix);
			}

			if (length + charLength > reqLength) {
				result = suffixResult;
				break;
			}

			result.append(chars[i]);
			if (!suffixFilled) {
				suffixResult.append(chars[i]);
			}

			if (!(chars[i] == ChatColor.COLOR_CHAR || (i > 0 && chars[i - 1] == ChatColor.COLOR_CHAR))) {
				length += charLength;
			}
		}

		// Add spaces:
		Character fillChar = ' ';
		Double fillLength = 1.0;
		while (true) {
			Double gapLength = reqLength - length;

			// Gap filled:
			if (gapLength <= 0) {
				break;
			}

			// Add custom fillers:
			if (gapLength <= MAX_GAP) {

				fillChar = findCustom(gapLength, reqLength);
				if (fillChar != null) {
					result.append(fillChar);
					fillLength = SIZE_MAP.get(fillChar);
				}
				break;
			}
			result.append(fillChar);
			length += fillLength;
		}
		return result.toString();
	}

	/**
	 * Trims and/or fills a string to be as close to the required length without exceeding it.
	 * If the string is trimmed, no suffix is added
	 * @param str string to trim/fill
	 * @param reqLength required length
	 * @return the trimmed/filled string equal to or bigger the required length
	 */
	public static String fillString(String str, Double reqLength) {

		return fillString(str, reqLength, "");
	}

	/**
	 * Finds a custom character with the best fit.
	 *
	 * @param gapLen gap length
	 * @param reqLength required length
	 * @return char that best fits the gap, null if none
	 */
	private static Character findCustom(Double gapLen, Double reqLength) {

		Set<Character> gapStrs = new HashSet<Character>(FILL_CHARS);
		Double bestFitLen = -1.0;
		Character bestFitStr = null;

		for (Character gapStr : gapStrs) {

			Double gapStrLen = SIZE_MAP.get(gapStr);

			if (gapLen - gapStrLen >= 0 && gapStrLen > bestFitLen) {
				bestFitLen = gapStrLen;
				bestFitStr = gapStr;
			}
		}
		return bestFitStr;
	}

	/**
	 * Calculates the length of a string.
	 *
	 * @param str string
	 * @return string length
	 */
	public static Double calcLength(String str) {

		char[] chars = str.toCharArray();

		Double length = 0.0;

		for (int i = 0; i < chars.length; i++) {
			Double charLength = SIZE_MAP.get(chars[i]);
			if (charLength == null) {
				charLength = DEFAULT_LENGTH;
			}

			if (!(chars[i] == ChatColor.COLOR_CHAR || (i > 0 && chars[i - 1] == ChatColor.COLOR_CHAR))) {
				length += charLength;
			}
		}
		return length;
	}

	/**
	 * Adjusts filler characters.
	 *
	 * @param str string
	 * @return adjusted string
	 */
	public static String adjustFillers(String str) {

		str = str.replace("\u278A", ChatColor.DARK_GRAY + "`");

		str = str.replace("\u278B", ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "`");
		str = str.replace("\u278C", ChatColor.DARK_GRAY + "" + ChatColor.BOLD + " ");

		return str;
	}
}
