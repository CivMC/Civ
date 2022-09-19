package vg.civcraft.mc.citadel.acidtypes;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;

import java.util.List;

public record AcidType(
		Material material,
		double modifier,
		List<BlockFace> blockFaces
) { }
