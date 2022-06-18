package com.untamedears.itemexchange.glues.namelayer;

import com.untamedears.itemexchange.ItemExchangePlugin;
import com.untamedears.itemexchange.events.BrowseOrPurchaseEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.utilities.DependencyGlue;
import vg.civcraft.mc.civmodcore.utilities.Validation;
import vg.civcraft.mc.namelayer.GroupManager;

public final class NameLayerGlue extends DependencyGlue {

	static NameLayerGlue instance;

	public NameLayerGlue(final @NotNull ItemExchangePlugin plugin) {
		super(plugin, "NameLayer");
		instance = this;
	}

	private final Listener listener = new Listener() {
		@EventHandler(ignoreCancelled = true)
		public void denyPurchaseIfNotGotPerms(final BrowseOrPurchaseEvent event) {
			final GroupModifier modifier = event.getTrade().getInput().getModifiers().get(GroupModifier.class);
			if (!Validation.checkValidity(modifier)
					|| PermissionsGlue.PURCHASE_PERMISSION.testPermission(
							GroupManager.getGroup(modifier.getGroupId()), event.getBrowser())) {
				return;
			}
			event.limitToBrowsing();
		}
	};

	@Override
	protected void onDependencyEnabled() {
		PermissionsGlue.init();
		ItemExchangePlugin.modifierRegistrar().registerModifier(GroupModifier.TEMPLATE);
		ItemExchangePlugin.getInstance().registerListener(this.listener);
	}

	@Override
	protected void onDependencyDisabled() {
		HandlerList.unregisterAll(this.listener);
		ItemExchangePlugin.modifierRegistrar().deregisterModifier(GroupModifier.TEMPLATE);
		PermissionsGlue.reset();
	}

}
