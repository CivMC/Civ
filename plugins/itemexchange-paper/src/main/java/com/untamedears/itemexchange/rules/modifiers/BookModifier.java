package com.untamedears.itemexchange.rules.modifiers;

import co.aikar.commands.annotation.CommandAlias;
import com.google.common.base.Strings;
import com.untamedears.itemexchange.commands.SetCommand;
import com.untamedears.itemexchange.rules.interfaces.Modifier;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import vg.civcraft.mc.civmodcore.nbt.NBTSerializable;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;
import vg.civcraft.mc.civmodcore.utilities.MoreClassUtils;

@CommandAlias(SetCommand.ALIAS)
@Modifier(slug = "BOOK", order = 1000)
public final class BookModifier extends ModifierData {

	public static final BookModifier TEMPLATE = new BookModifier();

	private static final String TITLE_KEY = "title";
	private static final String AUTHOR_KEY = "author";
	private static final String GENERATION_KEY = "generation";
	private static final String HAS_PAGES_KEY = "hasPages";
	private static final String BOOK_HASH_KEY = "bookHash";

	private String title;
	private String author;
	private Generation generation;
	private boolean hasPages;
	private int bookHash;

	@Override
	public BookModifier construct(ItemStack item) {
		BookMeta meta = MoreClassUtils.castOrNull(BookMeta.class, item.getItemMeta());
		if (meta == null) {
			return null;
		}
		BookModifier modifier = new BookModifier();
		if (meta.hasTitle()) {
			modifier.setTitle(meta.getTitle());
		}
		if (meta.hasAuthor()) {
			modifier.setAuthor(meta.getAuthor());
		}
		if (meta.hasGeneration()) {
			modifier.setGeneration(Objects.requireNonNull(meta.getGeneration()));
		}
		if (meta.hasPages()) {
			modifier.setHasPages(true);
			modifier.setBookHash(bookHash(meta.getPages()));
		}
		return modifier;
	}

	@Override
	public boolean isBroken() {
		return false;
	}

	@Override
	public boolean conforms(ItemStack item) {
		BookMeta meta = MoreClassUtils.castOrNull(BookMeta.class, item.getItemMeta());
		if (meta == null) {
			return false;
		}
		if (meta.hasTitle() != hasTitle()) {
			return false;
		}
		if (meta.hasAuthor() != hasAuthor()) {
			return false;
		}
		if (hasGeneration()) {
			if (meta.getGeneration() != getGeneration()) {
				return false;
			}
		}
		if (meta.hasPages() != hasPages()) {
			return false;
		}
		if (meta.hasPages()) {
			if (bookHash(meta.getPages()) != getBookHash()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void toNBT(@Nonnull final NBTCompound nbt) {
		nbt.setString(TITLE_KEY, this.title);
		nbt.setString(AUTHOR_KEY, this.author);
		nbt.setString(GENERATION_KEY, this.generation.name());
		nbt.setBoolean(HAS_PAGES_KEY, this.hasPages);
		nbt.setInt(BOOK_HASH_KEY, this.bookHash);
	}

	@Nonnull
	public static BookModifier fromNBT(@Nonnull final NBTCompound nbt) {
		final var modifier = new BookModifier();
		modifier.title = nbt.getString(TITLE_KEY);
		modifier.author = nbt.getString(AUTHOR_KEY);
		modifier.generation = EnumUtils.getEnum(Generation.class, nbt.getString(GENERATION_KEY));
		modifier.hasPages = nbt.getBoolean(HAS_PAGES_KEY);
		modifier.bookHash = nbt.getInt(BOOK_HASH_KEY);
		return modifier;
	}

	@Override
	public String getDisplayListing() {
		if (Strings.isNullOrEmpty(this.title)) {
			return null;
		}
		return title;
	}

	@Override
	public List<String> getDisplayInfo() {
		List<String> lines = new ArrayList<>();
		lines.add(ChatColor.DARK_AQUA + "Author: " + ChatColor.GRAY + (hasAuthor() ? getAuthor() : ""));
		if (hasGeneration()) {
			lines.add(ChatColor.DARK_AQUA + "Generation: " + ChatColor.GRAY + getGeneration().name());
		}
		return lines;
	}

	@Override
	public String toString() {
		return getSlug() +
				"{" +
				"title=" + getTitle() + "," +
				"author=" + getAuthor() + "," +
				"generation=" + getGeneration() + "," +
				"hash=" + getBookHash() +
				"}";
	}

	// ------------------------------------------------------------
	// Getters + Setters
	// ------------------------------------------------------------

	public boolean hasTitle() {
		return !Strings.isNullOrEmpty(this.title);
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean hasAuthor() {
		return !Strings.isNullOrEmpty(this.title);
	}

	public String getAuthor() {
		return this.author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public boolean hasGeneration() {
		return this.generation != null;
	}

	public Generation getGeneration() {
		return this.generation;
	}

	public void setGeneration(Generation generation) {
		this.generation = generation;
	}

	public boolean hasPages() {
		return this.hasPages;
	}

	public void setHasPages(boolean hasPages) {
		this.hasPages = hasPages;
	}

	public int getBookHash() {
		return this.bookHash;
	}

	public void setBookHash(int bookHash) {
		this.bookHash = bookHash;
	}

	private static int bookHash(List<String> pages) {
		if (CollectionUtils.isEmpty(pages)) {
			return 0;
		}
		return String.join("/r", pages).hashCode();
	}

}
