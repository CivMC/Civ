package com.devotedmc.ExilePearl.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;

public class TextUtil
{	
	private static TextUtil instance;
	
	public Map<String, String> tags;
	
	public TextUtil() {
		this.tags = new HashMap<String, String>();
		
		createTags();
	}
	
	public static TextUtil instance() {
		if (instance == null) {
			instance = new TextUtil();
		}
		return instance;
	}
	
	private void createTags()
	{
		tags.put("l", TextUtil.parseColor("<green>"));		// logo
		tags.put("a", TextUtil.parseColor("<gold>"));		// art
		tags.put("n", TextUtil.parseColor("<silver>"));		// notice
		tags.put("i", TextUtil.parseColor("<yellow>"));		// info
		tags.put("g", TextUtil.parseColor("<lime>"));		// good
		tags.put("b", TextUtil.parseColor("<rose>"));		// bad
		tags.put("h", TextUtil.parseColor("<pink>"));		// highlight
		tags.put("c", TextUtil.parseColor("<aqua>"));		// parameter
		tags.put("p", TextUtil.parseColor("<teal>"));		// parameter
		tags.put("w", TextUtil.parseColor("<white>"));		// parameter
		tags.put("lp", TextUtil.parseColor("<lpurple>"));
	}
	
	// -------------------------------------------- //
	// Top-level parsing functions.
	// -------------------------------------------- //
	
	public String parse(String str, Object... args)
	{
		return String.format(this.parse(str), args);
	}
	
	public String parse(String str)
	{
		return this.parseTags(parseColor(str));
	}
	
	// -------------------------------------------- //
	// Tag parsing
	// -------------------------------------------- //
	
	public String parseTags(String str)
	{
		return replaceTags(str, this.tags);
	}
	
	public static final transient Pattern patternTag = Pattern.compile("<([a-zA-Z0-9_]*)>");
	public static String replaceTags(String str, Map<String, String> tags)
	{
		StringBuffer ret = new StringBuffer();
		Matcher matcher = patternTag.matcher(str);
		while (matcher.find())
		{
			String tag = matcher.group(1);
			String repl = tags.get(tag);
			if (repl == null)
			{
				matcher.appendReplacement(ret, "<"+tag+">");
			}
			else
			{
				matcher.appendReplacement(ret, repl);
			}
		}
		matcher.appendTail(ret);
		return ret.toString();
	}
	
	// -------------------------------------------- //
	// Color parsing
	// -------------------------------------------- //
	
	public static String parseColor(String string)
	{
		string = parseColorAmp(string);
		string = parseColorAcc(string);
		string = parseColorTags(string);
		return string;
	}
	
	public static String parseColorAmp(String string)
	{
		string = string.replaceAll("(§([a-z0-9]))", "\u00A7$2");
		string = string.replaceAll("(&([a-z0-9]))", "\u00A7$2");
		string = string.replace("&&", "&");
		return string;
	}
	
	public static String parseColorAcc(String string)
	{
		return string.replace("`e", "")
		.replace("`r", ChatColor.RED.toString()) .replace("`R", ChatColor.DARK_RED.toString())
		.replace("`y", ChatColor.YELLOW.toString()) .replace("`Y", ChatColor.GOLD.toString())
		.replace("`g", ChatColor.GREEN.toString()) .replace("`G", ChatColor.DARK_GREEN.toString())
		.replace("`a", ChatColor.AQUA.toString()) .replace("`A", ChatColor.DARK_AQUA.toString())
		.replace("`b", ChatColor.BLUE.toString()) .replace("`B", ChatColor.DARK_BLUE.toString())
		.replace("`p", ChatColor.LIGHT_PURPLE.toString()) .replace("`P", ChatColor.DARK_PURPLE.toString())
		.replace("`k", ChatColor.BLACK.toString()) .replace("`s", ChatColor.GRAY.toString())
		.replace("`S", ChatColor.DARK_GRAY.toString()) .replace("`w", ChatColor.WHITE.toString());
	}
	
	public static String parseColorTags(String string)
	{
		return string.replace("<empty>", "")
		.replace("<black>", "\u00A70")
		.replace("<navy>", "\u00A71")
		.replace("<green>", "\u00A72")
		.replace("<teal>", "\u00A73")
		.replace("<red>", "\u00A74")
		.replace("<purple>", "\u00A75")
		.replace("<gold>", "\u00A76")
		.replace("<silver>", "\u00A77")
		.replace("<gray>", "\u00A78")
		.replace("<blue>", "\u00A79")
		.replace("<lime>", "\u00A7a")
		.replace("<aqua>", "\u00A7b")
		.replace("<rose>", "\u00A7c")
		.replace("<pink>", "\u00A7d")
		.replace("<yellow>", "\u00A7e")
		.replace("<white>", "\u00A7f")
		.replace("<lpurple>", ChatColor.LIGHT_PURPLE.toString())
		.replace("<bold>", ChatColor.BOLD.toString())
		.replace("<it>", ChatColor.ITALIC.toString())
		.replace("<reset>", ChatColor.RESET.toString());
	}
}
