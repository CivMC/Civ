package vg.civcraft.mc.civmodcore.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TextUtil {
	private static Map<String, String> tags = new HashMap<>();

	static {
		tags.put("g", TextUtil.parseColor("<lime>")); // good
		tags.put("b", TextUtil.parseColor("<rose>")); // bad
		tags.put("i", TextUtil.parseColor("<yellow>")); // info
		tags.put("a", TextUtil.parseColor("<gold>")); // art
		tags.put("l", TextUtil.parseColor("<green>")); // logo
		tags.put("n", TextUtil.parseColor("<silver>")); // notice
		tags.put("h", TextUtil.parseColor("<pink>")); // highlight
		tags.put("c", TextUtil.parseColor("<aqua>")); // parameter
		tags.put("p", TextUtil.parseColor("<teal>")); // parameter
		tags.put("w", TextUtil.parseColor("<white>")); // parameter
		tags.put("lp", TextUtil.parseColor("<lpurple>"));
	}

	public static Map<String, String> getTags() {
		return tags;
	}
	
	public static String formatDuration(long time) {
		return formatDuration(time, TimeUnit.MILLISECONDS);
	}
	
	public static String formatDuration(long time, TimeUnit unit) {
		long totalSeconds = TimeUnit.SECONDS.convert(time, unit);
		long seconds = totalSeconds % 60;
		long totalMinutes = totalSeconds / 60;
		long minutes = totalMinutes % 60;
		long totalHours = totalMinutes / 60;
		StringBuilder sb = new StringBuilder();
		if (totalHours > 0) {
			sb.append(totalHours);
			sb.append(" h ");
		}
		if (minutes > 0) {
			sb.append(minutes);
			sb.append(" min ");
		}
		if (seconds > 0) {
			sb.append(seconds);
			sb.append(" sec");
		}
		return sb.toString().trim();
	}

	// -------------------------------------------- //
	// Top-level parsing functions.
	// -------------------------------------------- //

	public static String parse(String str, Object... args) {
		return String.format(parse(str), args);
	}

	public static String parse(String str) {
		return parseTags(parseColor(str));
	}

	// -------------------------------------------- //
	// Tag parsing
	// -------------------------------------------- //

	public static String parseTags(String str) {
		return replaceTags(str, tags);
	}

	public static final transient Pattern patternTag = Pattern.compile("<([a-zA-Z0-9_]*)>");

	public static String replaceTags(String str, Map<String, String> tags) {
		StringBuffer ret = new StringBuffer();
		Matcher matcher = patternTag.matcher(str);
		while (matcher.find()) {
			String tag = matcher.group(1);
			String repl = tags.get(tag);
			if (repl == null) {
				matcher.appendReplacement(ret, "<" + tag + ">");
			} else {
				matcher.appendReplacement(ret, repl);
			}
		}
		matcher.appendTail(ret);
		return ret.toString();
	}

	// -------------------------------------------- //
	// Color parsing
	// -------------------------------------------- //

	public static String parseColor(String string) {
		string = parseColorAmp(string);
		string = parseColorAcc(string);
		string = parseColorTags(string);
		return string;
	}

	public static String parseColorAmp(String string) {
		string = string.replaceAll("(§([a-zA-Z0-9]))", "\u00A7$2");
		string = string.replaceAll("(&([a-zA-Z0-9]))", "\u00A7$2");
		string = string.replace("&&", "&");
		return string;
	}

	public static String parseColorAcc(String string) {
		return string.
				replace("`0", ChatColor.BLACK.toString()).
				replace("`1", ChatColor.DARK_BLUE.toString()).
				replace("`2", ChatColor.DARK_GREEN.toString()).
				replace("`3", ChatColor.DARK_AQUA.toString()).
				replace("`4", ChatColor.DARK_RED.toString()).
				replace("`5", ChatColor.DARK_PURPLE.toString()).
				replace("`6", ChatColor.GOLD.toString()).
				replace("`7", ChatColor.GRAY.toString()).
				replace("`8", ChatColor.DARK_GRAY.toString()).
				replace("`9", ChatColor.BLUE.toString()).
				replace("`A", ChatColor.GREEN.toString()).
				replace("`a", ChatColor.GREEN.toString()).
				replace("`B", ChatColor.AQUA.toString()).
				replace("`b", ChatColor.AQUA.toString()).
				replace("`C", ChatColor.RED.toString()).
				replace("`c", ChatColor.RED.toString()).
				replace("`D", ChatColor.LIGHT_PURPLE.toString()).
				replace("`d", ChatColor.LIGHT_PURPLE.toString()).
				replace("`E", ChatColor.YELLOW.toString()).
				replace("`e", ChatColor.YELLOW.toString()).
				replace("`F", ChatColor.WHITE.toString()).
				replace("`f", ChatColor.WHITE.toString()).
				replace("`L", ChatColor.BOLD.toString()).
				replace("`l", ChatColor.BOLD.toString()).
				replace("`M", ChatColor.STRIKETHROUGH.toString()).
				replace("`m", ChatColor.STRIKETHROUGH.toString()).
				replace("`N", ChatColor.UNDERLINE.toString()).
				replace("`n", ChatColor.UNDERLINE.toString()).
				replace("`O", ChatColor.ITALIC.toString()).
				replace("`o", ChatColor.ITALIC.toString()).
				replace("`R", ChatColor.RESET.toString()).
				replace("`r", ChatColor.RESET.toString());
	}

	public static String parseColorTags(String string) {
		return string.
				replace("<black>", ChatColor.BLACK.toString()).
				replace("<dblue>", ChatColor.DARK_BLUE.toString()).
				replace("<dgreen>", ChatColor.DARK_GREEN.toString()).
				replace("<daqua>", ChatColor.DARK_AQUA.toString()).
				replace("<dred>", ChatColor.DARK_RED.toString()).
				replace("<dpurple>", ChatColor.DARK_PURPLE.toString()).
				replace("<gold>", ChatColor.GOLD.toString()).
				replace("<lgray>", ChatColor.GRAY.toString()).
				replace("<dgray>", ChatColor.DARK_GRAY.toString()).
				replace("<blue>", ChatColor.BLUE.toString()).
				replace("<green>", ChatColor.GREEN.toString()).
				replace("<aqua>", ChatColor.AQUA.toString()).
				replace("<red>", ChatColor.RED.toString()).
				replace("<lpurple>", ChatColor.LIGHT_PURPLE.toString()).
				replace("<yellow>", ChatColor.YELLOW.toString()).
				replace("<white>", ChatColor.WHITE.toString()).
				replace("<b>", ChatColor.BOLD.toString()).
				replace("<s>", ChatColor.STRIKETHROUGH.toString()).
				replace("<u>", ChatColor.UNDERLINE.toString()).
				replace("<i>", ChatColor.ITALIC.toString()).
				replace("<r>", ChatColor.RESET.toString()).
				// Legacy support
				replace("<empty>", "").
				replace("<navy>", ChatColor.DARK_BLUE.toString()).
				replace("<teal>", ChatColor.DARK_AQUA.toString()).
				replace("<silver>", ChatColor.GRAY.toString()).
				replace("<gray>", ChatColor.DARK_GRAY.toString()). // REEE why name this gray?
				replace("<lime>", ChatColor.GREEN.toString()).
				replace("<lime>", ChatColor.GREEN.toString()).
				replace("<rose>", ChatColor.RED.toString()).
				replace("<pink>", ChatColor.LIGHT_PURPLE.toString()).
				replace("<bold>", ChatColor.BOLD.toString()).
				replace("<it>", ChatColor.ITALIC.toString()).
				replace("<reset>", ChatColor.RESET.toString());
	}

	// -------------------------------------------- //
	// Standard utils like UCFirst, implode and repeat.
	// -------------------------------------------- //

	public static String upperCaseFirst(String string) {
		Guard.ArgumentNotNull(string, "string");
		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}

	public static String repeat(String string, int times) {
		Guard.ArgumentNotNull(string, "string");
		if (times <= 0) {
			return "";
		} else {
			return string + repeat(string, times - 1);
		}
	}

	public static String implode(List<String> list, String glue) {
		Guard.ArgumentNotNull(list, "list");
		Guard.ArgumentNotNull(glue, "glue");
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			if (i != 0) {
				ret.append(glue);
			}
			ret.append(list.get(i));
		}
		return ret.toString();
	}

	public static String implodeCommaAnd(List<String> list, String comma, String and) {
		if (list.size() == 0) {
			return "";
		}
		if (list.size() == 1) {
			return list.get(0);
		}

		String lastItem = list.get(list.size() - 1);
		String nextToLastItem = list.get(list.size() - 2);
		String merge = nextToLastItem + and + lastItem;
		list.set(list.size() - 2, merge);
		list.remove(list.size() - 1);

		return implode(list, comma);
	}

	public static String implodeCommaAnd(List<String> list) {
		return implodeCommaAnd(list, ", ", " and ");
	}

	// -------------------------------------------- //
	// Paging and chrome-tools like titleize
	// -------------------------------------------- //

	private static final String titleizeLine = repeat("_", 52);
	private static final int titleizeBalance = -1;

	public static String titleize(String str) {
		return titleize("<a>", str);
	}

	public static String titleize(String colorCode, String str) {
		String center = ".[ " + parseTags("<l>") + str + parseTags(colorCode) + " ].";
		int centerlen = ChatColor.stripColor(center).length();
		int pivot = titleizeLine.length() / 2;
		int eatLeft = (centerlen / 2) - titleizeBalance;
		int eatRight = (centerlen - eatLeft) + titleizeBalance;

		if (eatLeft < pivot) {
			return parseTags(colorCode) + titleizeLine.substring(0, pivot - eatLeft) + center
					+ titleizeLine.substring(pivot + eatRight);
		} else {
			return parseTags(colorCode) + center;
		}
	}

	public static ArrayList<String> getPage(List<String> lines, int pageHumanBased, String title) {
		ArrayList<String> ret = new ArrayList<>();
		int pageZeroBased = pageHumanBased - 1;
		int pageheight = 9;
		int pagecount = (lines.size() / pageheight) + 1;

		ret.add(titleize(title + " " + pageHumanBased + "/" + pagecount));

		if (pagecount == 0) {
			ret.add(parseTags("<i>Sorry. No Pages available."));
			return ret;
		} else if (pageZeroBased < 0 || pageHumanBased > pagecount) {
			ret.add(parseTags("<i>Invalid page. Must be between 1 and " + pagecount));
			return ret;
		}

		int from = pageZeroBased * pageheight;
		int to = from + pageheight;
		if (to > lines.size()) {
			to = lines.size();
		}

		ret.addAll(lines.subList(from, to));

		return ret;
	}

	/**
	 * Static utility method for easily sending formatting strings to players Import statically for easier use import
	 * static vg.civcraft.mc.civmodcore.util.TextUtil.*;
	 * 
	 * @param player
	 *            The player
	 * @param message
	 *            The message
	 */
	public static void msg(Player player, String message) {
		if (player != null && player.isOnline()) {
			player.sendMessage(parse(message));
		}
	}

	/**
	 * Static utility method for easily sending formatting strings to players Import statically for easier use import
	 * static vg.civcraft.mc.civmodcore.util.TextUtil.*;
	 * 
	 * @param player
	 *            The player
	 * @param message
	 *            The message
	 */
	public static void msg(Player player, String message, Object... args) {
		if (player != null && player.isOnline()) {
			player.sendMessage(parse(message, args));
		}
	}
}
