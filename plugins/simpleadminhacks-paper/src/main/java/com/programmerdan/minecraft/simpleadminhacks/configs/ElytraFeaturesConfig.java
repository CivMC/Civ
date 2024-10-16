package com.programmerdan.minecraft.simpleadminhacks.configs;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHackConfig;
import javax.annotation.Nonnull;
import org.bukkit.configuration.ConfigurationSection;
import vg.civcraft.mc.civmodcore.utilities.CivLogger;

public final class ElytraFeaturesConfig extends SimpleHackConfig {
	private final CivLogger logger;

	private boolean disableFlight;
	private boolean disableFlightInCombat;

	private boolean disableFireworkBoosting;
	private boolean disableFireworkBoostingInCombat;
	private boolean disableSafeFireworkBoosting;

	private int heightDamage;
	private double heightDamageScaling;
	private int heightBuffer;
	private long heightDamageInterval;

	public ElytraFeaturesConfig(@Nonnull final SimpleAdminHacks plugin,
								@Nonnull final ConfigurationSection base) {
		super(plugin, base, false);
		this.logger = CivLogger.getLogger(getClass());
		wireup(base);
	}

	@Override
	protected void wireup(@Nonnull final ConfigurationSection config) {
		this.disableFlight = config.getBoolean("disableFlight", false);

		this.disableFlightInCombat = config.getBoolean("disableFlightInCombat", false);

		this.disableFireworkBoosting = config.getBoolean("disableFireworkBoosting", false);

		this.disableFireworkBoostingInCombat = config.getBoolean("disableFireworkBoostingInCombat", false);

		this.disableSafeFireworkBoosting = config.getBoolean("disableSafeFireworkBoosting", false);

		this.heightDamage = config.getInt("heightDamage.damage", 1);
		if (this.heightDamage < 0) {
			this.logger.warning("[heightDamage.damage] was set to [" + this.heightDamage + "], " +
					"which is invalid, defaulting to: 1");
			this.heightDamage = 1;
		}

		this.heightDamageScaling = config.getDouble("heightDamage.scales", 1d);
		if (this.heightDamageScaling <= 0d) {
			this.logger.warning("[heightDamage.scales] was set to [" + this.heightDamageScaling + "], " +
					"which is invalid, defaulting to: 1.0d");
			this.heightDamageScaling = 1d;
		}

		this.heightBuffer = config.getInt("heightDamage.buffer", 5);

		this.heightDamageInterval = config.getLong("heightDamage.interval", 1000L);
		if (this.heightDamageInterval < 1L) {
			this.logger.warning("[heightDamage.interval] was set to [" + this.heightDamageInterval + "], " +
					"which is invalid, defaulting to: 1000L");
			this.heightDamageInterval = 1000L;
		}
	}

	public boolean isFlightDisabled() {
		return this.disableFlight;
	}

	public boolean isFlightDisabledInCombat() {
		return this.disableFlightInCombat;
	}

	public boolean isBoostingDisabled() {
		return this.disableFireworkBoosting;
	}

	public boolean isBoostingDisabledInCombat() {
		return this.disableFireworkBoostingInCombat;
	}

	public boolean isSafeBoostingDisabled() {
		return this.disableSafeFireworkBoosting;
	}

	public int getHeightDamage() {
		return this.heightDamage;
	}

	public double isHeightDamageScaling() {
		return this.heightDamageScaling;
	}

	public int getHeightBuffer() {
		return this.heightBuffer;
	}

	public long getHeightDamageInterval() {
		return this.heightDamageInterval;
	}

}
