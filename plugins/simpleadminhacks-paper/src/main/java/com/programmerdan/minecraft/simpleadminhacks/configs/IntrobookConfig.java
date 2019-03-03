package com.programmerdan.minecraft.simpleadminhacks.configs;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;

import net.md_5.bungee.api.ChatColor;

/**
 * Simple Config wrapper for Introbook specification.
 * 
 * @author ProgrammerDan <programmerdan@gmail.com>
 */
public class IntrobookConfig extends SimpleHackConfig {

	/**
	 * The title of the book
	 */
	private String title;
	/**
	 * The author of the book (optional)
	 */
	private String author;
	/**
	 *  One or more pages of content for the book. No cleansing is applied.
	 */
	private List<String> pages;
	/**
	 * Determines how "sticky" the introbook is; if it follows them through respawn or
	 * just drops.
	 */
	private boolean follow;

	public IntrobookConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	public IntrobookConfig(ConfigurationSection base) {
		super(SimpleAdminHacks.instance(), base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		this.title = config.getString("contents.title");
		this.author = config.getString("contents.author");
		List<String> tPages = config.getStringList("contents.pages");

		if (this.title != null) {
			this.title = ChatColor.translateAlternateColorCodes('&', this.title);
		} else {
			this.setEnabled(false);
			plugin().log("Introbook disabled, no title given");
			return;
		}

		if (this.author != null) {
			this.author = ChatColor.translateAlternateColorCodes('&', this.author);
		}

		if (tPages != null && tPages.size() > 0) {
			this.pages = new ArrayList<String>(tPages.size());
			for (String page : tPages) {
				this.pages.add(ChatColor.translateAlternateColorCodes('&', page));
			}
		} else {
			this.setEnabled(false);
			plugin().log("Introbook disabled, no pages");
			return;
		}
	}

	/**
	 * Generate a new copy of the book based on the configured parameters.
	 * 
	 * @return ItemStack containing the book.
	 */
	public ItemStack getIntroBook(Player p) {
		ItemStack is = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta bm = (BookMeta) is.getItemMeta();
		if (p == null) {
			bm.setTitle(this.title.replaceAll("\\$\\{player\\}", ""));
		} else {
			bm.setTitle(this.title.replaceAll("\\$\\{player\\}", p.getName()));
		}
		bm.setAuthor(this.author);
		ArrayList<String> alist = null;
		if (p == null) {
			alist = new ArrayList<String>(this.pages);
		} else {
			alist = new ArrayList<String>();
			for (String page : this.pages) {
				alist.add(page.replaceAll("\\$\\{player\\}", p.getName()));
			}
		}
		bm.setPages(alist);
		is.setItemMeta(bm);
		return is;
	}

	public boolean isIntroBook(ItemStack is) {
		if (Material.WRITTEN_BOOK != is.getType()) return false;

		ItemMeta meta = is.getItemMeta();
		if (!(meta instanceof BookMeta)) return false;

		BookMeta book = (BookMeta) meta;
		if (!(getTitle().contains(
				book.getTitle().replaceAll("\\$\\{player\\}", "")
			))) return false;
		if (!(getAuthor().equals(book.getAuthor()))) return false;

		return true;
	}

	public boolean doesFollow() {
		return this.follow;
	}

	public String getTitle(){
		return this.title;
	}

	public String getAuthor() {
		return this.author;
	}

	public List<String> getPages() {
		return new ArrayList<String>(this.pages);
	}
}
