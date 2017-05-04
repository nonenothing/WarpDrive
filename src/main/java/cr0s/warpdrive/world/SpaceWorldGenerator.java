package cr0s.warpdrive.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.config.filler.Filler;
import cr0s.warpdrive.config.structures.AbstractStructureInstance;
import cr0s.warpdrive.config.structures.Orb.OrbShell;
import cr0s.warpdrive.config.structures.OrbInstance;
import cr0s.warpdrive.config.structures.StructureGroup;
import cr0s.warpdrive.data.CelestialObject;
import cr0s.warpdrive.data.StarMapRegistry;

import java.util.Random;

public class SpaceWorldGenerator implements IWorldGenerator {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		try {
			final int x = (chunkX * 16) + (5 - random.nextInt(10));
			final int z = (chunkZ * 16) + (5 - random.nextInt(10));
			CelestialObject celestialObject = StarMapRegistry.getCelestialObject(world, x, z);
			if (celestialObject == null) {
				// as observed on 1.7.10: during world transition, the generator from the previous world is still called
				return;
			}
			if ( celestialObject.borderRadiusX > 0
			  && ( Math.abs(x - celestialObject.dimensionCenterX) > celestialObject.borderRadiusX
			    || Math.abs(z - celestialObject.dimensionCenterZ) > celestialObject.borderRadiusZ ) ) {
				return;
			}
			int y = WarpDriveConfig.SPACE_GENERATOR_Y_MIN_CENTER
			      + random.nextInt(WarpDriveConfig.SPACE_GENERATOR_Y_MAX_CENTER - WarpDriveConfig.SPACE_GENERATOR_Y_MIN_CENTER);
			
			StructureGroup structureGroup = celestialObject.getRandomStructure(random, x, z);
			if (structureGroup == null) {
				return;
			}
			AbstractStructureInstance abstractStructureInstance = structureGroup.instantiate(random);
			abstractStructureInstance.generate(world, random, new BlockPos(x, y, z));
			
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @deprecated reference design for EntitySphereGenerator
	 **/
	@Deprecated
	public static void generateSphereDirect(
	                                       OrbInstance orbInstance, World world, int xCoord, int yCoord, int zCoord) {
		double radiusC = orbInstance.getTotalThickness() + 0.5D; // Radius from center of block
		double radiusSq = radiusC * radiusC; // Optimization to avoid square roots...
		// sphere
		int ceilRadius = (int) Math.ceil(radiusC);
		
		// Pass the cube and check points for sphere equation x^2 + y^2 + z^2 = r^2
		for (int x = 0; x <= ceilRadius; x++) {
			double dX2 = (x + 0.5D) * (x + 0.5D);
			for (int y = 0; y <= ceilRadius; y++) {
				double dX2Y2 = dX2 + (y + 0.5D) * (y + 0.5D);
				for (int z = 0; z <= ceilRadius; z++) {
					double dZ2 = (z + 0.5D) * (z + 0.5D);
					double dSq = dX2Y2 + dZ2; // squared distance from current position
					
					// Skip too far blocks
					if (dSq > radiusSq) {
						continue;
					}
					
					// Place blocks
					// cheat by using axial symmetry so we don't create random numbers too frequently
					
					OrbShell orbShell = orbInstance.getShellForSqRadius(dSq);
					Filler filler = orbShell.getRandomBlock(world.rand);
					filler.setBlock(world, new BlockPos(xCoord + x, yCoord + y, zCoord + z));
					filler.setBlock(world, new BlockPos(xCoord - x, yCoord + y, zCoord + z));
					filler.setBlock(world, new BlockPos(xCoord + x, yCoord - y, zCoord + z));
					filler.setBlock(world, new BlockPos(xCoord + x, yCoord + y, zCoord - z));
					filler.setBlock(world, new BlockPos(xCoord - x, yCoord - y, zCoord + z));
					filler.setBlock(world, new BlockPos(xCoord + x, yCoord - y, zCoord - z));
					filler.setBlock(world, new BlockPos(xCoord - x, yCoord + y, zCoord - z));
					filler.setBlock(world, new BlockPos(xCoord - x, yCoord - y, zCoord - z));
				}
			}
		}
	}
}
