package com.programmerdan.minecraft.simpleadminhacks.autoload.impl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.programmerdan.minecraft.simpleadminhacks.autoload.InvalidParameterValueException;

public class LocationParameterParser extends AbstractObjectParameterParser<Location> {

	@Override
	public Class<Location> getClassParsed() {
		return Location.class;
	}

	@Override
	public Location parseObject(String value) throws InvalidParameterValueException {
		String[] args = value.split(" ");
		if (args.length != 4) {
			throw new InvalidParameterValueException();
		}
		double[] parsed = new double[3];
		String worldName = args [0];
		World world = Bukkit.getWorld(worldName);
		if (world == null) {
			throw new InvalidParameterValueException(worldName + " is not a known world");
		}
		try {
			for (int i = 0; i < 3; i++) {
				parsed[i] = Double.parseDouble(args[i + 1]);
			}
		} catch (NumberFormatException e) {
			throw new InvalidParameterValueException();
		}
		return new Location(world, parsed[0], parsed[1], parsed[2]);
	}
}
