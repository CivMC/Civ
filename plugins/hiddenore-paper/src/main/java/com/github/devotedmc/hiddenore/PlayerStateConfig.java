package com.github.devotedmc.hiddenore;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Allows altering the chance rate based on the player's state/ effects they are under
 * 
 * format --
 * <code>
 * &lt;name&gt;:
 *   haste: [0.8, 0.5]
 *   fatigue: [1.0, 1.0, 0.8]
 *   nausea: ...
 *   luck: ...
 *   blindness: ...
 *   badluck: ...
 * </code>
 * each list of numbers is applied to each "level" so first number is for haste I, second for haste II; in this way custom effect levels
 * can be accomodated. At present only effects that already "logically" impact mining as present, you can add others I suppose.
 * 
 * See doLoad function for where these are initialized. Usable configs are set as named sections under top-level section "states". 
 * Then, individual DropLimits as defined at any level can apply a state using a "state" attribute decorator.
 * 
 * @author ProgrammerDan
 *
 */
public class PlayerStateConfig {
	public List<Double> hasteRates;
	public List<Double> fatigueRates;
	public List<Double> nauseaRates;
	public List<Double> luckRates;
	public List<Double> blindnessRates;
	public List<Double> badluckRates;
	
	/**
	 * All states are applied successively
	 * 
	 * @param player the player to apply adjustments to
	 * @return value 0+ indicating adjustment to chance
	 */
	public double statusRate(Player player) {
		double presentRate = 1.0d;
		for (PotionEffect effect : player.getActivePotionEffects()) {
			int idx = effect.getAmplifier();
			if (effect.getType().equals(PotionEffectType.FAST_DIGGING)) { // haste
				if (hasteRates != null && idx < hasteRates.size()) {
					presentRate *= hasteRates.get(idx);
				}
			} else if (effect.getType().equals(PotionEffectType.SLOW_DIGGING)) { // fatigue
				if (fatigueRates != null && idx < fatigueRates.size()) {
					presentRate *= fatigueRates.get(idx);
				}
			} else if (effect.getType().equals(PotionEffectType.CONFUSION)) { // nausea
				if (nauseaRates != null && idx < nauseaRates.size()) {
					presentRate *= nauseaRates.get(idx);
				}
			} else if (effect.getType().equals(PotionEffectType.LUCK)) { // luck
				if (luckRates != null && idx < luckRates.size()) {
					presentRate *= luckRates.get(idx);
				}
			} else if (effect.getType().equals(PotionEffectType.BLINDNESS)) { // blindness
				if (blindnessRates != null && idx < blindnessRates.size()) {
					presentRate *= blindnessRates.get(idx);
				}
			} else if (effect.getType().equals(PotionEffectType.UNLUCK)) { // unluck
				if (badluckRates != null && idx < badluckRates.size()) {
					presentRate *= badluckRates.get(idx);
				}
			}
		}
		return presentRate;
	}
}
