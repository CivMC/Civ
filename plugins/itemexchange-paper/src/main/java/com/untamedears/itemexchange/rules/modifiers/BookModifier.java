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
import vg.civcraft.mc.civmodcore.nbt.NBTType;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;

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
	public BookModifier construct(final ItemStack item) {
		if (!(item.getItemMeta() instanceof BookMeta bookMeta)) {
			return null;
		}
		final var modifier = new BookModifier();
		if (bookMeta.hasTitle()) {
			modifier.setTitle(bookMeta.getTitle());
		}
		if (bookMeta.hasAuthor()) {
			modifier.setAuthor(bookMeta.getAuthor());
		}
		if (bookMeta.hasGeneration()) {
			modifier.setGeneration(Objects.requireNonNull(bookMeta.getGeneration()));
		}
		if (bookMeta.hasPages()) {
			modifier.setHasPages(true);
			modifier.setBookHash(bookHash(bookMeta.getPages()));
		}
		return modifier;
	}

	@Override
	public boolean isBroken() {
		return false;
	}

	@Override
	public boolean conforms(ItemStack item) {
		if (!(item.getItemMeta() instanceof BookMeta bookMeta)) {
			return false;
		}
		if (bookMeta.hasTitle() != hasTitle()) {
			return false;
		}
		if (bookMeta.hasAuthor() != hasAuthor()) {
			return false;
		}
		if (hasGeneration()) {
			if (bookMeta.getGeneration() != getGeneration()) {
				return false;
			}
		}
		if (bookMeta.hasPages() != hasPages()) {
			return false;
		}
		if (bookMeta.hasPages()) {
			return bookHash(bookMeta.getPages()) == getBookHash();
		}
		return true;
	}

	@Override
	public void toNBT(@Nonnull final NBTCompound nbt) {
		if (hasTitle()) {
			nbt.setString(TITLE_KEY, this.title);
		}
		if (hasAuthor()) {
			nbt.setString(AUTHOR_KEY, this.author);
		}
		if (hasGeneration()) {
			nbt.setString(GENERATION_KEY, this.generation.name());
		}
		nbt.setBoolean(HAS_PAGES_KEY, this.hasPages);
		if (hasPages()) {
			nbt.setInt(BOOK_HASH_KEY, this.bookHash);
		}
	}

	@Nonnull
	public static BookModifier fromNBT(@Nonnull final NBTCompound nbt) {
		final var modifier = new BookModifier();
		if (nbt.hasKeyOfType(TITLE_KEY, NBTType.STRING)) {
			modifier.setTitle(nbt.getString(TITLE_KEY));
		}
		if (nbt.hasKeyOfType(AUTHOR_KEY, NBTType.STRING)) {
			modifier.setTitle(nbt.getString(AUTHOR_KEY));
		}
		if (nbt.hasKeyOfType(GENERATION_KEY, NBTType.STRING)) {
			modifier.setGeneration(EnumUtils.getEnum(Generation.class, nbt.getString(GENERATION_KEY)));
		}
		modifier.setHasPages(nbt.getBoolean(HAS_PAGES_KEY));
		if (modifier.hasPages()) {
			modifier.setBookHash(nbt.getInt(BOOK_HASH_KEY));
		}
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
		final var lines = new ArrayList<String>(2);
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

	public void setTitle(final String title) {
		this.title = title;
	}

	public boolean hasAuthor() {
		return !Strings.isNullOrEmpty(this.title);
	}

	public String getAuthor() {
		return this.author;
	}

	public void setAuthor(final String author) {
		this.author = author;
	}

	public boolean hasGeneration() {
		return this.generation != null;
	}

	public Generation getGeneration() {
		return this.generation;
	}

	public void setGeneration(final Generation generation) {
		this.generation = generation;
	}

	public boolean hasPages() {
		return this.hasPages;
	}

	public void setHasPages(final boolean hasPages) {
		this.hasPages = hasPages;
	}

	public int getBookHash() {
		return this.bookHash;
	}

	public void setBookHash(final int bookHash) {
		this.bookHash = bookHash;
	}

	private static int bookHash(final List<String> pages) {
		if (CollectionUtils.isEmpty(pages)) {
			return 0;
		}
		return String.join("/r", pages).hashCode();
	}

}
