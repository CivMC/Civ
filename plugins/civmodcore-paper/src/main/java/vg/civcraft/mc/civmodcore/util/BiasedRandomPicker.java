package vg.civcraft.mc.civmodcore.util;

import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Allows you to assign chances to objects and then randomly pick some
 * 
 * @param <E>
 *            Type of object which should be picked
 */
public class BiasedRandomPicker<E> {

	private Map<E, Double> originalChances;
	private TreeMap<Double, E> chances;
	private Random rng;

	/**
	 * Constructor
	 * 
	 * The given map should contain all objects that should be available in this
	 * instance, where each key is an object to pick and the respective value
	 * for that key is the chance to pick that item. Chances must be within
	 * [0,1], where 0 means 0 % chance to occur and 1 means 100 %. All chances
	 * summed up must equal one with a maximum inaccuracy of 10^(-6)
	 * 
	 * @param chances
	 *            Map containing all available objects and their chances to
	 *            occur
	 * 
	 */
	public BiasedRandomPicker(Map<E, Double> chances) {
		if (chances == null) {
			throw new IllegalArgumentException("Can not instantiate BiasedRandomPicker with null chance map");
		}
		this.originalChances = chances;
		this.rng = new Random();
		this.chances = new TreeMap<Double, E>();
		double totalChance = 0.0;
		for (Entry<E, Double> entry : chances.entrySet()) {
			if (entry.getValue() == null) {
				throw new IllegalArgumentException("You may not specify null as spawn chance");
			}
			double chance = entry.getValue();
			if (chance == 0.0) {
				continue;
			}
			chances.put(entry.getKey(), totalChance);
			totalChance += chance;
		}
		if (Math.abs(totalChance - 1.0) > Math.pow(10, -6)) {
			throw new IllegalArgumentException("Chances did not sum up to 1.0, total sum was: " + totalChance);
		}
	}

	/**
	 * Randomly picks an element according to the chances specified for this
	 * instance. In the case of malformed configs, this method may be unable to
	 * pick an element and return null, but the chance for that is guaranteed to
	 * be less than 10^(-120)
	 * 
	 * @return A randomly picked element
	 */
	public E getRandom() {
		int retries = 0;
		Entry<Double, E> entry = null;
		while (entry == null && retries < 20) {
			entry = chances.floorEntry(rng.nextDouble());
			retries++;
		}
		if (entry == null) {
			return null;
		}
		return entry.getValue();
	}

	/**
	 * Gets the chance for specific object to be picked by this instance. If
	 * they object doesnt have an explicit chance set, 0.0 will be returned
	 * 
	 * @param e
	 *            Object to check chance for
	 * @return Chance for this object to be picked by this instance
	 */
	public double getChance(E e) {
		Double chance = originalChances.get(e);
		if (chance == null) {
			return 0.0;
		}
		return chance;
	}
}
