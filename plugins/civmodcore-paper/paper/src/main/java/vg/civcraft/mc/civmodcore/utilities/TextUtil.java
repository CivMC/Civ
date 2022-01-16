package vg.civcraft.mc.civmodcore.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;

@UtilityClass
public class TextUtil {

	public static String formatDuration(long time) {
		return formatDuration(time, TimeUnit.MILLISECONDS);
	}

	public static String formatDuration(long time, TimeUnit unit) {
		long totalSeconds = TimeUnit.SECONDS.convert(time, unit);
		long seconds = totalSeconds % 60;
		long totalMinutes = totalSeconds / 60;
		long minutes = totalMinutes % 60;
		long totalHours = totalMinutes / 60;
		long totalDays = totalHours / 24;
		long totalWeeks = totalDays / 7;
		long totalYears = totalWeeks / 52;
		StringBuilder sb = new StringBuilder();
		if (totalYears > 0) {
			sb.append(totalYears);
			sb.append(" y ");
		}
		if (totalWeeks > 0) {
			sb.append(totalWeeks);
			sb.append(" w ");
		}
		if (totalDays > 0) {
			sb.append(totalDays);
			sb.append(" d ");
		}
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
	// Paging and chrome-tools like titleize
	// -------------------------------------------- //

	private static final String titleizeLine = StringUtils.repeat("_", 52);
	private static final int titleizeBalance = -1;

	public static String titleize(String str) {
		return titleize("<a>", str);
	}

	public static String titleize(String colorCode, String str) {
		String center = ".[ " + ChatUtils.parseColor("<l>") + str + ChatUtils.parseColor(colorCode) + " ].";
		int centerlen = ChatColor.stripColor(center).length();
		int pivot = titleizeLine.length() / 2;
		int eatLeft = (centerlen / 2) - titleizeBalance;
		int eatRight = (centerlen - eatLeft) + titleizeBalance;

		if (eatLeft < pivot) {
			return ChatUtils.parseColor(colorCode) + titleizeLine.substring(0, pivot - eatLeft) + center + titleizeLine.substring(pivot + eatRight);
		}
		else {
			return ChatUtils.parseColor(colorCode) + center;
		}
	}

	public static ArrayList<String> getPage(List<String> lines, int pageHumanBased, String title) {
		ArrayList<String> ret = new ArrayList<>();
		int pageZeroBased = pageHumanBased - 1;
		int pageheight = 9;
		int pagecount = (lines.size() / pageheight) + 1;

		ret.add(titleize(title + " " + pageHumanBased + "/" + pagecount));

		if (pagecount == 0) {
			ret.add(ChatUtils.parseColor("<i>Sorry. No Pages available."));
			return ret;
		}
		else if (pageZeroBased < 0 || pageHumanBased > pagecount) {
			ret.add(ChatUtils.parseColor("<i>Invalid page. Must be between 1 and " + pagecount));
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

}
