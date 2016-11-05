package com.untamedears.ItemExchange.metadata;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.untamedears.ItemExchange.utility.ExchangeRule;

public class BookMetadata implements AdditionalMetadata {
	private String title;
	private String author;
	private boolean hasPages;
	private int bookHash;
	
	public BookMetadata(BookMeta bookMeta) {
		this.title = bookMeta.hasTitle() ? bookMeta.getTitle() : null;
		this.author = bookMeta.hasAuthor() ? bookMeta.getAuthor() : null;
		this.hasPages = bookMeta.hasPages();
		this.bookHash = this.hasPages ? bookHash(bookMeta.getPages()) : 0;
	}
	
	private BookMetadata() {
		
	}
	
	public boolean hasTitle() {
		return title != null;
	}
	
	public boolean hasAuthor() {
		return author != null;
	}
	
	@Override
	public String getDisplayedInfo() {
		StringBuilder info = new StringBuilder();
		
		info.append(ChatColor.DARK_AQUA).append("Title: ").append(ChatColor.WHITE).append(title != null ? title : "<none>").append("\n");
		info.append(ChatColor.DARK_AQUA).append("Author: ").append(ChatColor.GRAY).append(author != null ? author : "<none>");
		
		return info.toString();
	}
	
	@Override
	public String serialize() {
		StringBuilder serialized = new StringBuilder();
		
		if(title != null && author != null && hasPages) {
			serialized.append(title).append(ExchangeRule.secondarySpacer).append(author).append(ExchangeRule.secondarySpacer).append(bookHash);
		}
		else if(hasPages) {
			serialized.append(bookHash);
		}
		
		return serialized.toString();
	}
	
	@Override
	public boolean matches(ItemStack item) {
		if(item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			
			if(meta instanceof BookMeta) {
				BookMeta bookMeta = (BookMeta) meta;
				
				if(bookMeta.hasTitle() == hasTitle() && bookMeta.hasAuthor() == hasAuthor() && bookMeta.hasPages() == hasPages) {
					if(hasPages) {
						return bookHash(bookMeta.getPages()) == bookHash;
					}
					else {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public static BookMetadata deserialize(String s) {
		BookMetadata meta = new BookMetadata();
		
		String[] parts = s.split(ExchangeRule.secondarySpacer);
		
		if(parts.length == 3) {
			meta.title = parts[0];
			meta.author = parts[1];
			meta.hasPages = true;
			meta.bookHash = Integer.parseInt(parts[2]);
		}
		else if(parts.length == 1) {
			meta.title = null;
			meta.author = null;
			meta.hasPages = true;
			meta.bookHash = Integer.parseInt(parts[0]);
		}
		else {
			meta.title = null;
			meta.author = null;
			meta.hasPages = false;
			meta.bookHash = 0;
		}
		
		return meta;
	}
	
	private static int bookHash(List<String> pages) {
		StringBuilder all = new StringBuilder();
		
		for(String page : pages) {
			all.append(page).append("\n");
		}
		
		return all.toString().hashCode();
	}
}
