package cr0s.warpdrive.config.structures;

import java.util.ArrayList;
import java.util.Random;

import org.w3c.dom.Element;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.InvalidXmlException;
import cr0s.warpdrive.config.MetaBlock;
import cr0s.warpdrive.data.VectorI;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class Asteroid extends Orb {
	private static final int CORE_MAX_TRIES = 10;
	
	private Block coreBlock;
	
	private int maxCoreCount;
	private int minCoreCount;
	private double relativeCoreRadius;
	
	public Asteroid(final String name) {
		super(name);
	}
	
	@Override
	public void loadFromXmlElement(Element element) throws InvalidXmlException {
		super.loadFromXmlElement(element);
		
		String coreBlockName = element.getAttribute("coreBlock");
		if (coreBlockName.isEmpty()) {
			throw new InvalidXmlException("Asteroid " + name + " is missing a coreBlock!");
		}
		
		coreBlock = Block.getBlockFromName(coreBlockName);
		if (coreBlock == null) {
			throw new InvalidXmlException("Asteroid " + name + " has an invalid/missing coreBlock " + coreBlockName);
		}
		
		try {
			minCoreCount = Integer.parseInt(element.getAttribute("minCoreCount"));
		} catch (NumberFormatException exception) {
			throw new InvalidXmlException("Asteroid " + name + " has an invalid minCoreCount " + element.getAttribute("minCoreCount") + ", expecting an integer");
		}
		
		if (minCoreCount < 1) {
			throw new InvalidXmlException("Asteroid " + name + " has an invalid minCoreCount " + minCoreCount + ", expecting greater then 0");
		}
		
		try {
			maxCoreCount = Integer.parseInt(element.getAttribute("maxCoreCount"));
		} catch (NumberFormatException exception) {
			throw new InvalidXmlException("Asteroid " + name + " has an invalid maxCoreCount " + element.getAttribute("maxCoreCount") + ", expecting an integer");
		}
		
		if (maxCoreCount < minCoreCount) {
			throw new InvalidXmlException("Asteroid " + name + " has an invalid maxCoreCount " + maxCoreCount + ", expecting greater than or equal to minCoreCount " + minCoreCount);
		}
		
		try {
			String stringCoreRad = element.getAttribute("relativeCoreRadius");
			if (stringCoreRad.isEmpty()) {
				relativeCoreRadius = 0.1;
			} else {
				relativeCoreRadius = Double.parseDouble(element.getAttribute("relativeCoreRadius"));
			}
		} catch (NumberFormatException gdbg) {
			throw new InvalidXmlException("Asteroid " + name + " has an invalid relativeCoreRadius " + element.getAttribute("relativeCoreRadius") + ", expecting a double");
		}
		
		if (relativeCoreRadius < 0.0D || relativeCoreRadius > 1.0D) {
			throw new InvalidXmlException("Asteroid " + name + " has an invalid relativeCoreRadius " + relativeCoreRadius + ", expecting a value between 0.0 and 1.0 included");
		}
	}
	
	@Override
	public boolean generate(World world, Random random, int x, int y, int z) {
		int[] thicknesses = randomize(random);
		int totalThickness = 0;
		for (int thickness : thicknesses) {
			totalThickness += thickness;
		}
		int coreBlocksCount = minCoreCount + ((maxCoreCount > minCoreCount) ? random.nextInt(maxCoreCount - minCoreCount) : 0);
		
		WarpDrive.logger.info("Generating asteroid " + name + " as radius " + totalThickness + " coreBlocksCount " + coreBlocksCount + " coreRad " + relativeCoreRadius);
		
		// use this to generate an abstract form for the core.
		double coreRadius = relativeCoreRadius * totalThickness;
		ArrayList<VectorI> coreLocations = generateCore(world, random, x, y, z, coreBlocksCount, coreBlock, coreBlocksCount, coreRadius);
		
		for (VectorI coreLocation: coreLocations) {
			// Calculate minimum distance to borders of generation area
			int maxRadX = totalThickness - Math.abs(x - coreLocation.x);
			int maxRadY = totalThickness - Math.abs(y - coreLocation.y);
			int maxRadZ = totalThickness - Math.abs(z - coreLocation.z);
			int maxLocalRadius = Math.max(maxRadX, Math.max(maxRadY, maxRadZ));
			
			// Generate shell 
			addShell(thicknesses, world, coreLocation, maxLocalRadius);
		}
		
		return true;
	}
	
	/**
	 * Creates a shell sphere around given core location.
	 * 
	 * @param thicknesses Random generator
	 * @param world World to place shell
	 * @param location Location of core block
	 * @param maxRad Maximum radius of asteroid
	 */
	private void addShell(int[] thicknesses, World world, VectorI location, int radius) {
		// iterate all blocks within cube with side 2 * radius
		for(int x = location.x - radius; x <= location.x + radius; x++) {
			int dX2 = (x - location.x) * (x - location.x);
			for(int y = location.y - radius; y <= location.y + radius; y++) {
				int dX2Y2 = dX2 + (y - location.y) * (y - location.y);
				for(int z = location.z - radius; z <= location.z + radius; z++) {
					// current radius
					int range = (int)Math.round(Math.sqrt(dX2Y2 + (location.z - z) * (location.z - z)));
					
					// if inside radius
					if(range <= radius && isReplaceableOreGen(world, x, y, z)) {
						OrbShell shell = getShellForRadius(thicknesses, range);
						MetaBlock metaBlock = shell.getRandomBlock(world.rand);
						world.setBlock(x, y, z, metaBlock.block, metaBlock.metadata, 0);
					}
				}
			}
		}
	}
	
	/**
	 * Checks if given coordinate empty (air in terms of MC).
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private static boolean isReplaceableOreGen(World world, int x, int y, int z) {
		return world.getBlock(x, y, z).isReplaceableOreGen(world, x, y, z, Blocks.air);
	}
	
	/**
	 * Generates core with simplified algorithm that tries to place numberOfBlocks cores within a core sphere (specified in config by "coreRad" value from 0.0 to 1.0).
	 * 
	 * Note: CORE_MAX_TRIES defines how many tries the method applies before giving up placing a core. 
	 * 
	 * @param world - World where to place cores
	 * @param rand - Random generator
	 * @param x - center X coord
	 * @param y - center Y coord
	 * @param z - center Z coord
	 * @param numberOfBlocks - number of core blocks to place
	 * @param block - type of block to place
	 * @param metadata - metadata of bloeck to place
	 * @param coreRadius - max radius of asteroid
	 * @return List of placed locations of cores
	 */
	private ArrayList<VectorI> generateCore(World world, Random rand, int x, int y, int z,
			int numberOfBlocks, Block block, int metadata, double coreRadius) {
		ArrayList<VectorI> addedBlocks = new ArrayList<VectorI>();
		int coreDiameter = Math.max(1, (int)Math.round(2 * coreRadius));
		int xMin = x - (int)Math.round(coreRadius);
		int yMin = y - (int)Math.round(coreRadius);
		int zMin = z - (int)Math.round(coreRadius);
		
		for (int coreBlockIndex = 0; coreBlockIndex < numberOfBlocks; coreBlockIndex++) {
			int curX = x;
			int curY = y;
			int curZ = z;
			boolean found = false;
			
			for(int step = 0; step < CORE_MAX_TRIES && !found; step++) {
				curX = xMin + rand.nextInt(coreDiameter);
				curY = yMin + rand.nextInt(coreDiameter);
				curZ = zMin + rand.nextInt(coreDiameter);
				
				if (isReplaceableOreGen(world, curX, curY, curZ)) {
					world.setBlock(curX, curY, curZ, block, metadata, 2);
					addedBlocks.add(new VectorI(curX, curY, curZ));
					found = true;
				}
			}
		}
		
		return addedBlocks;
	}
}
