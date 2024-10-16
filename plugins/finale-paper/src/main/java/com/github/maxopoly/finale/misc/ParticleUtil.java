package com.github.maxopoly.finale.misc;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.function.Consumer;
import java.util.function.Function;

public class ParticleUtil {

	public static boolean line(Location p1, Location p2, Function<Location, Boolean> particleSpawnLogic, int particles) {
		Vector subDir = p2.clone().subtract(p1).toVector();
		double length = subDir.length();
		double ratio = length / particles;
		Vector newDir = subDir.normalize().multiply(ratio);
		Location subLoc = p1.clone();

		for (int i = 0; i < particles; i++) {
			subLoc.add(newDir);
			if (particleSpawnLogic.apply(subLoc)) {
				return true;
			}
		}
		return false;
	}

}
