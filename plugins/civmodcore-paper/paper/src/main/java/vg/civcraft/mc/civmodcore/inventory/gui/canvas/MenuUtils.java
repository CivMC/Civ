package vg.civcraft.mc.civmodcore.inventory.gui.canvas;

import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.type.AbstractCivMenu;
import org.ipvp.canvas.type.OpenHandler;
import org.ipvp.canvas.type.UpdateHandler;

public final class MenuUtils {

	/**
	 * @return Returns an empty, unmodifiable slot.
	 */
	public static SlotSettings createEmptySlot() {
		return SlotSettings.builder().build();
	}

	/**
	 * Overrides a given menu's open event handler, in lieu of just replacing it, meaning that you can add handlers to
	 * a menu without fear of removing any previously added functionality.
	 *
	 * @param menu The menu to add the handler for.
	 * @param handler The handler to add.
	 * @param callSuperBefore Whether or not to call the existing handler before or after the given handler.
	 */
	public static void overrideOpenHandler(final AbstractCivMenu menu,
										   final OpenHandler handler,
										   final boolean callSuperBefore) {
		final var findExisting = menu.getOpenHandler();
		if (findExisting.isEmpty()) {
			menu.setOpenHandler(handler);
			return;
		}
		final var existing = findExisting.get();
		menu.setOpenHandler((viewer, openedMenu) -> {
			if (callSuperBefore) {
				existing.handle(viewer, openedMenu);
			}
			handler.handle(viewer, openedMenu);
			if (!callSuperBefore) {
				existing.handle(viewer, openedMenu);
			}
		});
	}

	/**
	 * Overrides a given menu's update event handler, in lieu of just replacing it, meaning that you can add handlers
	 * to a menu without fear of removing any previously added functionality.
	 *
	 * @param menu The menu to add the handler for.
	 * @param handler The handler to add.
	 * @param callSuperBefore Whether or not to call the existing handler before or after the given handler.
	 */
	public static void overrideUpdateHandler(final AbstractCivMenu menu,
											 final UpdateHandler handler,
											 final boolean callSuperBefore) {
		final var findExisting = menu.getUpdateHandler();
		if (findExisting.isEmpty()) {
			menu.setUpdateHandler(handler);
			return;
		}
		final var existing = findExisting.get();
		menu.setUpdateHandler((viewer, openedMenu) -> {
			if (callSuperBefore) {
				existing.handle(viewer, openedMenu);
			}
			handler.handle(viewer, openedMenu);
			if (!callSuperBefore) {
				existing.handle(viewer, openedMenu);
			}
		});
	}

	/**
	 * Overrides a given menu's close event handler, in lieu of just replacing it, meaning that you can add handlers to
	 * a menu without fear of removing any previously added functionality.
	 *
	 * @param menu The menu to add the handler for.
	 * @param handler The handler to add.
	 * @param callSuperBefore Whether or not to call the existing handler before or after the given handler.
	 */
	public static void overrideCloseHandler(final AbstractCivMenu menu,
											final Menu.CloseHandler handler,
											final boolean callSuperBefore) {
		final var findExisting = menu.getCloseHandler();
		if (findExisting.isEmpty()) {
			menu.setCloseHandler(handler);
			return;
		}
		final var existing = findExisting.get();
		menu.setCloseHandler((viewer, openedMenu) -> {
			if (callSuperBefore) {
				existing.close(viewer, openedMenu);
			}
			handler.close(viewer, openedMenu);
			if (!callSuperBefore) {
				existing.close(viewer, openedMenu);
			}
		});
	}

}
