package vg.civcraft.mc.civmodcore.particles;

import javax.annotation.Nonnull;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;

/**
 * @param particle The type of particle to effect, ie: explosion, heart, lava... etc
 * @param offsetX The maximum randomised offset on the X axis.
 * @param offsetY The maximum randomised offset on the Y axis.
 * @param offsetZ The maximum randomised offset on the Z axis.
 * @param speed The speed of the effect.
 * @param particleCount The number (density) of the particle effect.
 */
public record ParticleEffect(@NonNull Particle particle,
							 float offsetX,
							 float offsetY,
							 float offsetZ,
							 float speed,
							 int particleCount) {

	@Deprecated
	public Particle getParticle() {
		return this.particle;
	}

	@Deprecated
	public float getOffsetX() {
		return this.offsetX;
	}

	@Deprecated
	public float getOffsetY() {
		return this.offsetY;
	}

	@Deprecated
	public float getOffsetZ() {
		return this.offsetZ;
	}

	@Deprecated
	public float getSpeed() {
		return this.speed;
	}

	@Deprecated
	public int getParticleCount() {
		return this.particleCount;
	}

	/**
	 * Display an effect defined in the config around a reinforcement.
	 *
	 * @param location the location of the reinforcement.
	 */
	public void playEffect(@Nonnull final Location location) {
		location.getWorld().spawnParticle(this.particle, location, this.particleCount,
				this.offsetX, this.offsetY, this.offsetZ, this.speed, null);
	}

}
