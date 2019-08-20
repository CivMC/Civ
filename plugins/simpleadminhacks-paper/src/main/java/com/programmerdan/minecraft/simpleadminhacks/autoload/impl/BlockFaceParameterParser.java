package com.programmerdan.minecraft.simpleadminhacks.autoload.impl;
import org.bukkit.block.BlockFace;

import com.programmerdan.minecraft.simpleadminhacks.autoload.InvalidParameterValueException;

public class BlockFaceParameterParser extends AbstractObjectParameterParser<BlockFace> {

	@Override
	public Class<BlockFace> getClassParsed() {
		return BlockFace.class;
	}

	@Override
	public BlockFace parseObject(String value) throws InvalidParameterValueException {
		try {
			return BlockFace.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new InvalidParameterValueException();
		}
	}
}
