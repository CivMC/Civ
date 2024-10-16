package com.untamedears.jukealert.model.actions;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.LoggableAction;
import com.untamedears.jukealert.model.actions.impl.BlockBreakAction;
import com.untamedears.jukealert.model.actions.impl.BlockPlaceAction;
import com.untamedears.jukealert.model.actions.impl.DestroyVehicleAction;
import com.untamedears.jukealert.model.actions.impl.DismountEntityAction;
import com.untamedears.jukealert.model.actions.impl.EmptyBucketAction;
import com.untamedears.jukealert.model.actions.impl.EnterFieldAction;
import com.untamedears.jukealert.model.actions.impl.EnterVehicleAction;
import com.untamedears.jukealert.model.actions.impl.ExitVehicleAction;
import com.untamedears.jukealert.model.actions.impl.FillBucketAction;
import com.untamedears.jukealert.model.actions.impl.IgniteBlockAction;
import com.untamedears.jukealert.model.actions.impl.KillLivingEntityAction;
import com.untamedears.jukealert.model.actions.impl.KillPlayerAction;
import com.untamedears.jukealert.model.actions.impl.LeaveFieldAction;
import com.untamedears.jukealert.model.actions.impl.LoginAction;
import com.untamedears.jukealert.model.actions.impl.LogoutAction;
import com.untamedears.jukealert.model.actions.impl.MountEntityAction;
import com.untamedears.jukealert.model.actions.impl.OpenContainerAction;
import it.unimi.dsi.fastutil.objects.Object2IntAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.bukkit.Location;
import org.bukkit.Material;

public class LoggedActionFactory {

	private final Map<String, LoggedActionProvider> providers;
	private final Object2IntMap<String> identifierToInternal;

	public LoggedActionFactory() {
		this.providers = new Object2ObjectAVLTreeMap<>();
		this.identifierToInternal = new Object2IntAVLTreeMap<>();
		this.identifierToInternal.defaultReturnValue(-1);
		registerInternalProviders();
	}

	public void registerProvider(@Nonnull final String identifier,
								 @Nonnull final LoggedActionProvider provider) {
		final int internal = JukeAlert.getInstance().getDAO().getOrCreateActionID(Objects.requireNonNull(identifier));
		if (internal != -1) {
			this.providers.put(identifier, Objects.requireNonNull(provider));
			this.identifierToInternal.put(identifier, internal);
		}
	}

	public LoggableAction produce(@Nonnull final Snitch snitch,
								  @Nonnull final String identifier,
								  final UUID player,
								  final Location location,
								  final long time,
								  final String victim) {
		final LoggedActionProvider provider = this.providers.get(Objects.requireNonNull(identifier));
		return provider == null ? null : provider.get(Objects.requireNonNull(snitch), player, location, time, victim);
	}

	public int getInternalID(@Nonnull final String name) {
		return this.identifierToInternal.getInt(Objects.requireNonNull(name));
	}

	private void registerInternalProviders() {
		registerProvider(BlockBreakAction.ID,
				(snitch, player, loc, time, victim) -> new BlockBreakAction(
						time, snitch, player, loc, victim));

		registerProvider(BlockPlaceAction.ID,
				(snitch, player, loc, time, victim) -> new BlockPlaceAction(
						time, snitch, player, loc, victim));

		registerProvider(DestroyVehicleAction.ID,
				(snitch, player, loc, time, victim) -> new DestroyVehicleAction(
						time, snitch, player, loc, victim));

		registerProvider(DismountEntityAction.ID,
				(snitch, player, loc, time, victim) -> new DismountEntityAction(
						time, snitch, player, loc, victim));

		registerProvider(EmptyBucketAction.ID,
				(snitch, player, loc, time, victim) -> new EmptyBucketAction(
						time, snitch, player, loc, Material.valueOf(victim)));

		registerProvider(EnterFieldAction.ID,
				(snitch, player, loc, time, victim) -> new EnterFieldAction(
						time, snitch, player));

		registerProvider(EnterVehicleAction.ID,
				(snitch, player, loc, time, victim) -> new EnterVehicleAction(
						time, snitch, player, loc, victim));

		registerProvider(ExitVehicleAction.ID,
				(snitch, player, loc, time, victim) -> new ExitVehicleAction(
						time, snitch, player, loc, victim));

		registerProvider(FillBucketAction.ID,
				(snitch, player, loc, time, victim) -> new FillBucketAction(
						time, snitch, player, loc, Material.valueOf(victim)));

		registerProvider(IgniteBlockAction.ID,
				(snitch, player, loc, time, victim) -> new IgniteBlockAction(
						time, snitch, player, loc));

		registerProvider(KillLivingEntityAction.ID,
				(snitch, player, loc, time, victim) -> new KillLivingEntityAction(
						time, snitch, player, loc, victim));

		registerProvider(KillPlayerAction.ID,
				(snitch, player, loc, time, victim) -> new KillPlayerAction(
						time, snitch, player, loc, UUID.fromString(victim)));

		registerProvider(LeaveFieldAction.ID,
				(snitch, player, loc, time, victim) -> new LeaveFieldAction(
						time, snitch, player));

		registerProvider(LoginAction.ID,
				(snitch, player, loc, time, victim) -> new LoginAction(
						time, snitch, player));

		registerProvider(LogoutAction.ID,
				(snitch, player, loc, time, victim) -> new LogoutAction(
						time, snitch, player));

		registerProvider(MountEntityAction.ID,
				(snitch, player, loc, time, victim) -> new MountEntityAction(
						time, snitch, player, loc, victim));

		registerProvider(OpenContainerAction.ID,
				(snitch, player, loc, time, victim) -> new OpenContainerAction(
						time, snitch, player, loc,  Material.valueOf(victim)));
	}

}
