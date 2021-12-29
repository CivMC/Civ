package vg.civcraft.mc.namelayer.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.configuration.ConfigurationSection;

public class NameCleanser {

	private static boolean enabled = true;
	private static boolean cleanNames;
	private static boolean alertOps;
	private static String alertPerm;
	private static List<String> cleanWords;
	private static Pattern wordPattern;
	private static Random rng;
	
	public static void load(ConfigurationSection config) {
		if(config == null) {
			enabled = false;
			return;
		}
		try {
			List<String> badWords = config.getStringList("bad_words");
			ConfigurationSection opts = config.getConfigurationSection("opts");
			List<List<Character>> options = new ArrayList<>();
			for(String key : opts.getKeys(false)) {
				options.add(opts.getCharacterList(key));
			}
			StringBuilder patternBuilder = new StringBuilder();
			patternBuilder.append("(");
			for(String word : badWords) {
				for(char c : word.toCharArray()) {
					patternBuilder.append("[");
					if(Character.isAlphabetic(c)) {
						patternBuilder.append(Character.toUpperCase(c));
						patternBuilder.append(Character.toLowerCase(c));
					}
					for(List<Character> list : options) {
						if(list.contains(c)) {
							for(Character other : list) {
								if(!other.equals(c)) {
									if(Character.isAlphabetic(other)) {
										patternBuilder.append(Character.toUpperCase(other));
										patternBuilder.append(Character.toLowerCase(other));
									} else {
										patternBuilder.append(other);
									}
								}
							}
						}
					}
					patternBuilder.append("]");
				}
				patternBuilder.append("|");
			}
			patternBuilder.setCharAt(patternBuilder.lastIndexOf("|"), ')');
			wordPattern = Pattern.compile(patternBuilder.toString());
			cleanWords = config.getStringList("clean_words");
			cleanNames = config.getBoolean("clean_names");
			alertOps = config.getBoolean("alert_ops");
			alertPerm = config.getString("alert_perm", "namelayer.alert_dirty_name");
			rng = new Random();
		} catch (Exception e) {
			enabled = false;
		}
	}
	
	public static boolean isDirty(String name) {
		return enabled && wordPattern.matcher(name).find();
	}
	
	public static String cleanName(String name) {
		if(!enabled) return name;
		Matcher matcher = wordPattern.matcher(name);
		while(matcher.find()) {
			name = matcher.replaceFirst(cleanWords.get(rng.nextInt(cleanWords.size())));
			matcher.reset(name);
		}
		return name;
	}
	
	public static boolean isCleanNames() {
		return cleanNames;
	}
	
	public static boolean isAlertOps() {
		return alertOps;
	}
	
	public static String getAlertPerm() {
		return alertPerm;
	}
	
	public static boolean isEnabled() {
		return enabled;
	}
}
