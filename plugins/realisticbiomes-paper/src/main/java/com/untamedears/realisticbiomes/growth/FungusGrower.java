package com.untamedears.realisticbiomes.growth;

import com.untamedears.realisticbiomes.model.Plant;
import java.util.Random;
import net.minecraft.core.BlockPosition;
import net.minecraft.data.worldgen.BiomeDecoratorGroups;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;

/**
 * We need to differentiate fungus from other types of saplings thanks
 * to the peculiarities with how Spigot handles tree generation. If you
 * call {@code world.generateTree()} with the {@link TreeType}, it will
 * generate the tree as if it were populating a newly generated chunk.
 * Therefore we need to bypass that and pretend the sapling is being
 * bone mealed so ensure a singular tree is generated.
 */
public class FungusGrower extends AgeableGrower {

	private final Random random = new Random();

	public FungusGrower(final Material material) {
		super(material, 1, 1);
	}

	@Override
	public int getStage(final Plant plant) {
		final Block block = plant.getLocation().getBlock();
		if (block.getType() != this.material) {
			return -1;
		}
		return 0;
	}

	@Override
	public void setStage(final Plant plant, final int stage) {
		if (stage < 1) {
			return;
		}
		final Block block = plant.getLocation().getBlock();
		final Material material = block.getType();
		final var growth =
				material == Material.CRIMSON_FUNGUS ? BiomeDecoratorGroups.cy :
				material == Material.WARPED_FUNGUS ? BiomeDecoratorGroups.cA :
				material == Material.FLOWERING_AZALEA ? BiomeDecoratorGroups.cS :
				null;
		if (growth == null) {
			return;
		}
		final WorldServer world = ((CraftWorld) block.getWorld()).getHandle();
		final BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());
		//Taken from CraftWorld.generateTree()
		if (!growth.e.generate(new FeaturePlaceContext(world, world.getChunkProvider().getChunkGenerator(), this.random, position, growth.f))) {
			block.setType(material);
		}
	}

	@Override
	public boolean deleteOnFullGrowth() {
		return true;
	}

}
