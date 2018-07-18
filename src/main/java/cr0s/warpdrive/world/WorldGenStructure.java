package cr0s.warpdrive.world;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.WarpDriveText;
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
import net.minecraft.util.math.BlockPos;
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
		super();
		
		this.corrupted = corrupted;
		this.rand = rand;
		
		// hull plain and glass are linked by same name
		final GenericSet<Filler> fillerSetHull_plain = WarpDriveConfig.FillerManager.getRandomSetFromGroup(rand, "hull_plain");
		if (fillerSetHull_plain == null) {
			WarpDrive.logger.warn(String.format("No FillerSet found within group %s during world generation: check your configuration",
			                                    "hull_plain"));
			fillerHullPlain = new Filler();
			fillerHullPlain.block = Blocks.STONE;
			fillerHullGlass = new Filler();
			fillerHullGlass.block = Blocks.GLASS;
		} else {
			fillerHullPlain = fillerSetHull_plain.getRandomUnit(rand);
			
			final String nameFillerGlass = "hull_glass:" + fillerSetHull_plain.getName();
			final GenericSet<Filler> fillerSetHull_glass = WarpDriveConfig.FillerManager.getGenericSet(nameFillerGlass);
			if (fillerSetHull_glass == null) {
				WarpDrive.logger.warn(String.format("No FillerSet %s found during world generation: check your configuration",
				                                    nameFillerGlass));
				fillerHullGlass = new Filler();
				fillerHullGlass.block = Blocks.GLASS;
			} else {
				fillerHullGlass = fillerSetHull_glass.getRandomUnit(rand);
			}
		}
		
		// solarPanel and wiring are linked by same name
		final GenericSet<Filler> fillerSetSolarPanel = WarpDriveConfig.FillerManager.getRandomSetFromGroup(rand, "ship_solarPanel");
		if (fillerSetSolarPanel == null) {
			WarpDrive.logger.warn(String.format("No FillerSet found within group %s during world generation: check your configuration",
			                                    "ship_solarPanel"));
			fillerSolarPanel = new Filler();
			fillerSolarPanel.block = Blocks.SANDSTONE;
			fillerWiring = new Filler();
			fillerWiring.block = Blocks.OAK_FENCE;
		} else {
			fillerSolarPanel = fillerSetSolarPanel.getRandomUnit(rand);
			
			final String nameFillerWiring = "ship_wiring:" + fillerSetSolarPanel.getName();
			final GenericSet<Filler> fillerSetWiring = WarpDriveConfig.FillerManager.getGenericSet(nameFillerWiring);
			if (fillerSetWiring == null) {
				WarpDrive.logger.warn(String.format("No FillerSet found within group %s during world generation: check your configuration",
				                                    nameFillerWiring));
				fillerWiring = new Filler();
				fillerWiring.block = Blocks.OAK_FENCE;
			} else {
				fillerWiring = fillerSetWiring.getRandomUnit(rand);
			}
		}
		
		// propulsion is on it's own
		final GenericSet<Filler> fillerSetPropulsion = WarpDriveConfig.FillerManager.getRandomSetFromGroup(rand, "ship_propulsion");
		if (fillerSetPropulsion == null) {
			WarpDrive.logger.warn(String.format("No FillerSet found within group %s during world generation: check your configuration",
			                                    "ship_propulsion"));
			fillerPropulsion = new Filler();
			fillerPropulsion.block = Blocks.LOG;
		} else {
			fillerPropulsion = fillerSetPropulsion.getRandomUnit(rand);
		}
	}
	
	public void setHullPlain(final World world, final int x, final int y, final int z) {
		if (corrupted && (rand.nextInt(400) == 1)) {
			world.newExplosion(null, x + 0.5D, y + 0.5D, z + 0.5D, 17, false, true);
		} else if (corrupted && (rand.nextInt(10) == 1)) {
			world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState(), 2);
		} else {
			fillerHullPlain.setBlock(world, new BlockPos(x, y, z));
		}
	}
	
	public void setHullGlass(final World world, final int x, final int y, final int z) {
		if (corrupted && (rand.nextInt(5) == 1)) {
			world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState(), 2);
		} else {
			fillerHullGlass.setBlock(world, new BlockPos(x, y, z));
		}
	}
	
	public void setSolarPanel(final World world, final int x, final int y, final int z) {
		if (corrupted && (rand.nextInt(3) == 1)) {
			world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState(), 2);
		} else {
			fillerSolarPanel.setBlock(world, new BlockPos(x, y, z));
		}
	}
	
	public void setWiring(final World world, final int x, final int y, final int z) {
		if (corrupted && (rand.nextInt(3) == 1)) {
			world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState(), 2);
		} else {
			fillerWiring.setBlock(world, new BlockPos(x, y, z));
		}
	}
	
	public void setPropulsion(final World world, final int x, final int y, final int z) {
		fillerPropulsion.setBlock(world, new BlockPos(x, y, z));
	}
	
	public void fillInventoryWithLoot(final World world, final Random rand, final int x, final int y, final int z, final String group) {
		final TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
		if (tileEntity instanceof IInventory) {
			final IInventory inventory = (IInventory) tileEntity;
			final int size = inventory.getSizeInventory();
			final int countLoots = Math.min(rand.nextInt(3) + rand.nextInt(4), size);
			
			final GenericSet<Loot> lootSet = WarpDriveConfig.LootManager.getRandomSetFromGroup(rand, group);
			if (lootSet == null) {
				WarpDrive.logger.warn(String.format("No LootSet found with group %s for inventory %s: check your configuration",
				                                    group,
				                                    Commons.format(world, x, y, z)));
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
					WarpDrive.logger.info(String.format("Unable to find a valid loot from LootSet %s for inventory %s in %s: check your configuration",
					                                    lootSet.getFullName(),
					                                    inventory.getName() == null ? "-null name-" : inventory.getName(),
					                                    Commons.format(world, x, y, z)));
				}
			}
		}
	}
	
	public void generateFromFile(final World world, final String filename, final int targetX, final int targetY, final int targetZ, final byte rotationSteps) {
		final WarpDriveText reason = new WarpDriveText();
		final JumpShip jumpShip = JumpShip.createFromFile(filename, reason);
		if (jumpShip == null) {
			WarpDrive.logger.error(String.format("%s Failed to read schematic %s: %s", this, filename, reason.toString()));
			return;
		}
		deployShip(world, jumpShip, targetX, targetY, targetZ, rotationSteps);
	}
	
	public void deployShip(final World world, final JumpShip jumpShip, final int targetX, final int targetY, final int targetZ, final byte rotationSteps) {
		
		final Transformation transformation = new Transformation(jumpShip, world,
			targetX - jumpShip.core.getX(),
			targetY - jumpShip.core.getY(),
			targetZ - jumpShip.core.getZ(),
			rotationSteps);
		for (int index = 0; index < jumpShip.jumpBlocks.length; index++) {
			// Deploy single block
			final JumpBlock jumpBlock = jumpShip.jumpBlocks[index];
			
			if (jumpBlock == null) {
				if (WarpDriveConfig.LOGGING_BUILDING) {
					WarpDrive.logger.info(String.format("At index %d, skipping undefined block", index));
				}
			} else if (jumpBlock.block == Blocks.AIR) {
				if (WarpDriveConfig.LOGGING_BUILDING) {
					WarpDrive.logger.info(String.format("At index %d, skipping air block", index));
				}
			} else if (Dictionary.BLOCKS_ANCHOR.contains(jumpBlock.block)) {
				if (WarpDriveConfig.LOGGING_BUILDING) {
					WarpDrive.logger.info(String.format("At index %d, skipping anchor block %s", index, jumpBlock.block));
				}
			} else {
				index++;
				if (WarpDriveConfig.LOGGING_WORLD_GENERATION && WarpDrive.isDev) {
					WarpDrive.logger.info(String.format("At index %d, deploying %s ",
					                                    index, jumpBlock));
				}
				final BlockPos targetLocation = transformation.apply(jumpBlock.x, jumpBlock.y, jumpBlock.z);
				final Block blockAtTarget = world.getBlockState(targetLocation).getBlock();
				if (blockAtTarget == Blocks.AIR || Dictionary.BLOCKS_EXPANDABLE.contains(blockAtTarget)) {
					jumpBlock.deploy(world, transformation);
				} else {
					if (WarpDriveConfig.LOGGING_WORLD_GENERATION && WarpDrive.isDev) {
						WarpDrive.logger.info(String.format("Deployment collision detected %s",
						                                    Commons.format(world, targetX + jumpBlock.x, targetY + jumpBlock.y, targetZ + jumpBlock.z)));
					}
				}
			}
		}
	}
}
