package cr0s.warpdrive.config.structures;

import java.util.ArrayList;
import java.util.Random;

import org.w3c.dom.Element;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.InvalidXmlException;
import cr0s.warpdrive.config.MetaBlock;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class Asteroid extends Orb {

	private static final int MIN_RADIUS = 1;
	private static final int CORE_MAX_TRIES = 10;
	
	private Block coreBlock;

	private int maxCoreSize, minCoreSize;
	private double coreRad;
	
	public Asteroid() {
		super(0); //Diameter not relevant
	}

	@Override
	public void loadFromXmlElement(Element e) throws InvalidXmlException {

		super.loadFromXmlElement(e);

		String coreBlockName = e.getAttribute("coreBlock");
		if (coreBlockName.isEmpty())
			throw new InvalidXmlException("Asteroid is missing a coreBlock!");

		coreBlock = Block.getBlockFromName(coreBlockName);
		if (coreBlock == null)
			throw new InvalidXmlException("Asteroid coreBlock doesnt exist!");

		try {

			maxCoreSize = Integer.parseInt(e.getAttribute("maxCoreSize"));
			minCoreSize = Integer.parseInt(e.getAttribute("minCoreSize"));

		} catch (NumberFormatException gdbg) {
			throw new InvalidXmlException("Asteroid core size dimensions are NaN!");
		}
		
		try {
			String coreRadStr = e.getAttribute("coreRad");
			if(coreRadStr.isEmpty()) {
				coreRad = 0.1;
			} else {
				coreRad = Double.parseDouble(e.getAttribute("coreRad"));
			}
		} catch (NumberFormatException gdbg) {
			throw new InvalidXmlException("Asteroid core rad must be double!");
		}

	}
	
	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		int randRadius = MIN_RADIUS + rand.nextInt(Math.max(1, getRadius() - MIN_RADIUS));
		int numberCoreBlocks = minCoreSize + rand.nextInt(maxCoreSize - minCoreSize);
				
		WarpDrive.logger.info("Asteroid generation: radius=" + randRadius + ", numCoreBlocks=" + numberCoreBlocks + ", coreRad=" + coreRad);

		//Use this to generate a abstract form for the core.
		ArrayList<Location> coreLocations = generateCore(world, rand, x, y, z, numberCoreBlocks, coreBlock, numberCoreBlocks, randRadius);

		for (Location coreLocation: coreLocations) {
			// Calculate mininum distance to borders of generation area
			int maxRadX = Math.min(x+randRadius-coreLocation.x, coreLocation.x - (x - randRadius));
			int maxRadY = Math.min(y+randRadius-coreLocation.y, coreLocation.y - (y - randRadius));
			int maxRadZ = Math.min(z+randRadius-coreLocation.z, coreLocation.z - (z - randRadius));
			int maxLocalRadius = Math.min(maxRadX, Math.min(maxRadY, maxRadZ));
		
			// Generate shell 
			addShell(world, rand, coreLocation, maxLocalRadius);
		}

		return true;
	}

	/**
	 * Creates a shell sphere around given core location.
	 * 
	 * @param world World to place shell
	 * @param rand Random generator
	 * @param l Location of core block
	 * @param maxRad Maximum radius of asteroid
	 */
	private void addShell(World world, Random rand, Location l, int maxRad) {
		//int rad = MIN_RADIUS + rand.nextInt(Math.max(1, maxRad - MIN_RADIUS));
		int rad = maxRad;
		
		// Iterate all blocks withing cube with side 2*rad
		for(int x = l.x - rad; x <= l.x + rad; ++x) {
			for(int y = l.y - rad; y <= l.y + rad; ++y) {
				for(int z = l.z - rad; z <= l.z + rad; ++z) {
					// current radius
					int r = (int)Math.round(Math.sqrt((l.x - x)*(l.x - x) + (l.y - y)*(l.y - y) + (l.z - z)*(l.z - z)));
					// if inside radius
					if(r <= rad && isBlockEmpty(world, x, y, z)) {
						OrbShell shell = getShellForRadius(r);
						MetaBlock blType = shell.getRandomBlock(rand);
						world.setBlock(x, y, z, blType.block, blType.metadata, 0);
					}
				}
			}
		}
	}

	/**
	 * Represents a single point in space
	 *
	 */
	private class Location {

		public int x, y, z;

		public Location(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
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
	private static boolean isBlockEmpty(World world, int x, int y, int z) {
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
	 * @param maxRange - max radius of asteroid
	 * @return List of placed locations of cores
	 */
	private ArrayList<Location> generateCore(World world, Random rand, int x, int y, int z, int numberOfBlocks, Block block, int metadata,
			int maxRange) {

		ArrayList<Location> addedBlocks = new ArrayList<Location>();
		int coreRange = (int)Math.round(coreRad * maxRange);
		int maxX = x + coreRange;
		int minX = x - coreRange;
		int maxY = y + coreRange;
		int minY = y - coreRange;
		int maxZ = z + coreRange;
		int minZ = z - coreRange;
		
		for (int i = 0; i < numberOfBlocks; ++i) {
			int curX = x;
			int curY = y;
			int curZ = z;
			boolean stopWalk = false;
			
			for(int step = 0; step <= CORE_MAX_TRIES && !stopWalk; ++step) {
				curX = rand.nextInt(Math.max(1, maxX - minX)) + minX;
				curY = rand.nextInt(Math.max(1, maxY - minY)) + minY;
				curZ = rand.nextInt(Math.max(1, maxZ - minZ)) + minZ;
				
				if (isBlockEmpty(world, curX, curY, curZ)) {
					world.setBlock(curX, curY, curZ, block, metadata, 2);
					addedBlocks.add(new Location(curX, curY, curZ));
					stopWalk = true;
				}
			}
		}
		
		return addedBlocks;
	}

}
