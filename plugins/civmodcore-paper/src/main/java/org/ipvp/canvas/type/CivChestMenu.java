package org.ipvp.canvas.type;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.ipvp.canvas.Menu;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;

public class CivChestMenu extends AbstractCivMenu {

	private Dimension dimension;

	public CivChestMenu(final Component title, final int rows) {
		this(title, rows, true);
	}

	public CivChestMenu(final Component title, final int rows, final boolean redraw) {
		this(title, rows, null, redraw);
	}

	public CivChestMenu(final Component title, final int rows, final Menu parent, final boolean redraw) {
		super(title, rows * 9, parent, redraw);
	}

	/**
	 * @return Return this chest menu's row and column counts.
	 */
	@Override
	public Dimension getDimensions() {
		if (this.dimension == null) {
			this.dimension = new Dimension(getSlotCount() / 9, 9);
		}
		return this.dimension;
	}

	/**
	 * Returns a new builder.
	 *
	 * @param rows The amount of rows for the inventory to contain
	 * @throws IllegalArgumentException if rows is not between 1 and 6 inclusive
	 */
	public static Builder builder(final int rows) {
		if (rows < 1 || rows > 6) {
			throw new IllegalArgumentException("rows must be a value from 1 to 6");
		}
		return new Builder(rows);
	}

	/**
	 * A builder for creating a CustomChestMenu instance.
	 */
	public static class Builder extends AbstractMenu.Builder<Builder> {

		private Component title;

		Builder(final int rows) {
			super(new Dimension(rows, 9));
		}

		@Deprecated
		@Override
		public Builder title(final String title) {
			if (StringUtils.isBlank(title)) {
				this.title = null;
				return this;
			}
			this.title = LegacyComponentSerializer.legacyAmpersand().deserialize(title);
			if (ChatUtils.isNullOrEmpty(this.title)) {
				this.title = null;
			}
			return this;
		}

		public Builder title(final Component title) {
			this.title = title;
			return this;
		}

		public Component getComponentTitle() {
			return this.title;
		}

		@Override
		public CivChestMenu build() {
			return new CivChestMenu(getComponentTitle(),
					getDimensions().getArea(), getParent(), isRedraw());
		}

	}

}
