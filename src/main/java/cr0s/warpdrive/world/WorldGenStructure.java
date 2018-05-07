package cr0s.warpdrive.world;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.Dictionary;
import cr0s.warpdrive.config.Filler;
import cr0s.warpdrive.config.GenericSet;
import cr0s.warpdrive.config.Loot;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.JumpBlock;
import cr0s.warpdrive.data.JumpShip;
import cr0s.warpdrive.data.Transformation;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class WorldGenStructure {
	
	private final boolean corrupted;
	private final Random rand;
	private final Filler fillerHullPlain;
	private final Filler fillerHullGlass;
	private final Filler fillerSolarPanel;
	private final Filler fillerWiring;
	private final Filler fillerPropulsion;
	
	public WorldGenStructure(final boolean corrupted, final Random rand) {
		this.corrupted = corrupted;
		this.rand = rand;
		
		// hull plain and glass are linked by same name
		final GenericSet<Filler> fillerSetHull_plain = WarpDriveConfig.FillerManager.getRandomSetFromGroup(rand, "hull_plain");
		if (fillerSetHull_plain == null) {
			WarpDrive.logger.warn(String.format("No FillerSet found with group %s during world generation: check your configuration",
			                                    "hull_plain"));
			fillerHullPlain = new Filler();
			fillerHullPlain.block = Blocks.stone;
			fillerHullGlass = new Filler();
			fillerHullGlass.block = Blocks.glass;
		} else {
			fillerHullPlain = fillerSetHull_plain.getRandomUnit(rand);
			
			final String nameFillerGlass = "hull_glass:" + fillerSetHull_plain.getName();
			final GenericSet<Filler> fillerSetHull_glass = WarpDriveConfig.FillerManager.getGenericSet(nameFillerGlass);
			if (fillerSetHull_glass == null) {
				WarpDrive.logger.warn(String.format("No FillerSet found with group %s during world generation: check your configuration",
				                                    nameFillerGlass));
				fillerHullGlass = new Filler();
				fillerHullGlass.block = Blocks.glass;
			} else {
				fillerHullGlass = fillerSetHull_glass.getRandomUnit(rand);
			}
		}
		
		// solarPanel and wiring are linked by same name
		final GenericSet<Filler> fillerSetSolarPanel = WarpDriveConfig.FillerManager.getRandomSetFromGroup(rand, "ship_solarPanel");
		if (fillerSetSolarPanel == null) {
			WarpDrive.logger.warn(String.format("No FillerSet found with group %s during world generation: check your configuration",
			                                    "ship_solarPanel"));
			fillerSolarPanel = new Filler();
			fillerSolarPanel.block = Blocks.sandstone;
			fillerWiring = new Filler();
			fillerWiring.block = Blocks.fence;
		} else {
			fillerSolarPanel = fillerSetSolarPanel.getRandomUnit(rand);
			
			final String nameFillerWiring = "ship_wiring:" + fillerSetSolarPanel.getName();
			final GenericSet<Filler> fillerSetWiring = WarpDriveConfig.FillerManager.getGenericSet(nameFillerWiring);
			if (fillerSetWiring == null) {
				WarpDrive.logger.warn(String.format("No FillerSet found with group %s during world generation: check your configuration",
				                                    nameFillerWiring));
				fillerWiring = new Filler();
				fillerWiring.block = Blocks.fence;
			} else {
				fillerWiring = fillerSetWiring.getRandomUnit(rand);
			}
		}
		
		// propulsion is on it's own
		final GenericSet<Filler> fillerSetPropulsion = WarpDriveConfig.FillerManager.getRandomSetFromGroup(rand, "ship_propulsion");
		if (fillerSetPropulsion == null) {
			WarpDrive.logger.warn(String.format("No FillerSet found with group %s during world generation: check your configuration",
			                                    "ship_propulsion"));
			fillerPropulsion = new Filler();
			fillerPropulsion.block = Blocks.log;
		} else {
			fillerPropulsion = fillerSetPropulsion.getRandomUnit(rand);
		}
	}
	
	public void setHullPlain(final World world, final int x, final int y, final int z) {
		if (corrupted && (rand.nextInt(400) == 1)) {
			world.newExplosion(null, x + 0.5D, y + 0.5D, z + 0.5D, 17, false, true);
		} else if (corrupted && (rand.nextInt(10) == 1)) {
			world.setBlock(x, y, z, Blocks.air, 0, 2);
		} else {
			fillerHullPlain.setBlock(world, x, y, z);
		}
	}
	
	public void setHullGlass(final World world, final int x, final int y, final int z) {
		if (corrupted && (rand.nextInt(5) == 1)) {
			world.setBlock(x, y, z, Blocks.air, 0, 2);
		} else {
			fillerHullGlass.setBlock(world, x, y, z);
		}
	}
	
	public void setSolarPanel(final World world, final int x, final int y, final int z) {
		if (corrupted && (rand.nextInt(3) == 1)) {
			world.setBlock(x, y, z, Blocks.air, 0, 2);
		} else {
			fillerSolarPanel.setBlock(world, x, y, z);
		}
	}
	
	public void setWiring(final World world, final int x, final int y, final int z) {
		if (corrupted && (rand.nextInt(3) == 1)) {
			world.setBlock(x, y, z, Blocks.air, 0, 2);
		} else {
			fillerWiring.setBlock(world, x, y, z);
		}
	}
	
	public void setPropulsion(final World world, final int x, final int y, final int z) {
		fillerPropulsion.setBlock(world, x, y, z);
	}
	
	public void fillInventoryWithLoot(final World worldObj, final Random rand, final int x, final int y, final int z, final String group) {
		final TileEntity tileEntity = worldObj.getTileEntity(x, y, z);
		if (tileEntity instanceof IInventory) {
			final IInventory inventory = (IInventory) tileEntity;
			final int size = inventory.getSizeInventory();
			final int countLoots = Math.min(rand.nextInt(3) + rand.nextInt(4), size);
			
			final GenericSet<Loot> lootSet = WarpDriveConfig.LootManager.getRandomSetFromGroup(rand, group);
			if (lootSet == null) {
				WarpDrive.logger.warn(String.format("No LootSet found with group %s for inventory @ %s (%d %d %d): check your configuration",
				                                    group,
				                                    worldObj.provider.getDimensionName(),
				                                    x, y, z));
				return;
			}
			
			// for all loots to add
			int indexSlot;
			ItemStack itemStackLoot;
			boolean isAdded;
			for (int i = 0; i < countLoots; i++) {
				isAdded = false;
				for (int countLootRetries = 0; countLootRetries < 3 && !isAdded; countLootRetries++) {
					// pick a loot
					itemStackLoot = lootSet.getRandomUnit(rand).getItemStack(rand);
					
					// find a valid slot for it
					for (int countSlotRetries = 0; countSlotRetries < 5 && !isAdded; countSlotRetries++) {
						indexSlot = rand.nextInt(size);
						if (inventory.isItemValidForSlot(indexSlot, itemStackLoot)) {
							inventory.setInventorySlotContents(indexSlot, itemStackLoot);
							isAdded = true;
						}
					}
				}
				if (!isAdded) {
					WarpDrive.logger.info(String.format("Unable to find a valid loot from LootSet %s for inventory %s in @ %s (%d %d %d): check your configuration",
					                                    lootSet.getFullName(),
					                                    inventory.getInventoryName() == null ? "-null name-" : inventory.getInventoryName(),
					                                    worldObj.provider.getDimensionName(),
					                                    x, y, z));
				}
			}
		}
	}
	
	public void generateFromFile(final World world, final String filename, final int targetX, final int targetY, final int targetZ, final byte rotationSteps) {
		final StringBuilder reason = new StringBuilder();
		final JumpShip jumpShip = JumpShip.createFromFile(filename, reason);
		if (jumpShip == null) {
			WarpDrive.logger.error(String.format("%s Failed to read schematic %s: %s", this, filename, reason.toString()));
			return;
		}
		deployShip(world, jumpShip, targetX, targetY, targetZ, rotationSteps);
	}
	
	public void deployShip(final World world, final JumpShip jumpShip, final int targetX, final int targetY, final int targetZ, final byte rotationSteps) {
		
		final Transformation transformation = new Transformation(jumpShip, world, targetX - jumpShip.coreX, targetY - jumpShip.coreY, targetZ - jumpShip.coreZ, rotationSteps);
		for (int index = 0; index < jumpShip.jumpBlocks.length; index++) {
			// Deploy single block
			final JumpBlock jumpBlock = jumpShip.jumpBlocks[index];
			
			if (jumpBlock == null) {
				if (WarpDriveConfig.LOGGING_BUILDING) {
					WarpDrive.logger.info("At index " + index + ", skipping undefined block");
				}
			} else if (jumpBlock.block == Blocks.air) {
				if (WarpDriveConfig.LOGGING_BUILDING) {
					WarpDrive.logger.info("At index " + index + ", skipping air block");
				}
			} else if (Dictionary.BLOCKS_ANCHOR.contains(jumpBlock.block)) {
				if (WarpDriveConfig.LOGGING_BUILDING) {
					WarpDrive.logger.info("At index " + index + ", skipping anchor block " + jumpBlock.block);
				}
			} else {
				index++;
				if (WarpDriveConfig.LOGGING_WORLD_GENERATION && WarpDrive.isDev) {
					WarpDrive.logger.info(String.format("At index %d, deploying %s ",
					                                    index, jumpBlock));
				}
				final ChunkCoordinates targetLocation = transformation.apply(jumpBlock.x, jumpBlock.y, jumpBlock.z);
				final Block blockAtTarget = world.getBlock(targetLocation.posX, targetLocation.posY, targetLocation.posZ);
				if (blockAtTarget == Blocks.air || Dictionary.BLOCKS_EXPANDABLE.contains(blockAtTarget)) {
					jumpBlock.deploy(world, transformation);
				} else {
					if (WarpDriveConfig.LOGGING_WORLD_GENERATION && WarpDrive.isDev) {
						WarpDrive.logger.info("Deployment collision detected at " + (targetX + jumpBlock.x) + " " + (targetY + jumpBlock.y) + " " + (targetZ + jumpBlock.z));
					}
				}
			}
		}
	}
}
