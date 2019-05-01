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
	private static Map<String, String> tags = new HashMap<String, String>();

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
		string = string.replaceAll("(§([a-z0-9]))", "\u00A7$2");
		string = string.replaceAll("(&([a-z0-9]))", "\u00A7$2");
		string = string.replace("&&", "&");
		return string;
	}

	public static String parseColorAcc(String string) {
		return string.replace("`e", "").replace("`r", ChatColor.RED.toString())
				.replace("`R", ChatColor.DARK_RED.toString()).replace("`y", ChatColor.YELLOW.toString())
				.replace("`Y", ChatColor.GOLD.toString()).replace("`g", ChatColor.GREEN.toString())
				.replace("`G", ChatColor.DARK_GREEN.toString()).replace("`a", ChatColor.AQUA.toString())
				.replace("`A", ChatColor.DARK_AQUA.toString()).replace("`b", ChatColor.BLUE.toString())
				.replace("`B", ChatColor.DARK_BLUE.toString()).replace("`p", ChatColor.LIGHT_PURPLE.toString())
				.replace("`P", ChatColor.DARK_PURPLE.toString()).replace("`k", ChatColor.BLACK.toString())
				.replace("`s", ChatColor.GRAY.toString()).replace("`S", ChatColor.DARK_GRAY.toString())
				.replace("`w", ChatColor.WHITE.toString());
	}

	public static String parseColorTags(String string) {
		return string.replace("<empty>", "").replace("<black>", "\u00A70").replace("<navy>", "\u00A71")
				.replace("<green>", "\u00A72").replace("<teal>", "\u00A73").replace("<red>", "\u00A74")
				.replace("<purple>", "\u00A75").replace("<gold>", "\u00A76").replace("<silver>", "\u00A77")
				.replace("<gray>", "\u00A78").replace("<blue>", "\u00A79").replace("<lime>", "\u00A7a")
				.replace("<aqua>", "\u00A7b").replace("<rose>", "\u00A7c").replace("<pink>", "\u00A7d")
				.replace("<yellow>", "\u00A7e").replace("<white>", "\u00A7f")
				.replace("<lpurple>", ChatColor.LIGHT_PURPLE.toString()).replace("<bold>", ChatColor.BOLD.toString())
				.replace("<it>", ChatColor.ITALIC.toString()).replace("<reset>", ChatColor.RESET.toString());
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
		ArrayList<String> ret = new ArrayList<String>();
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
