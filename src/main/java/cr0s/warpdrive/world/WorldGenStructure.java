package cr0s.warpdrive.world;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.Dictionary;
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
	
	private Block hullPlain_block;
	private int hullPlain_metadata;
	private Block hullGlass_block;
	private int hullGlass_metadata;
	private Block solarPanel_block;
	private int solarPanel_metadata;
	private Block cable_block;
	private int cable_metadata;
	private Block resource_block;
	private int resource_metadata;
	private final boolean corrupted;
	private final Random rand;
	
	public WorldGenStructure(final boolean corrupted, Random rand) {
		this.corrupted = corrupted;
		this.rand = rand;
		
		// choose a hull block
		switch (rand.nextInt(7)) {
		default:
		case 0:
		case 1:
			hullPlain_block = Blocks.stained_hardened_clay;
			hullPlain_metadata = rand.nextInt(16);
			hullGlass_block = Blocks.stained_glass;
			hullGlass_metadata = hullPlain_metadata;
			break;
			
		case 2:
		case 3:
		case 4:
		case 5:
			hullPlain_block = WarpDrive.blockHulls_plain[0][0];
			hullPlain_metadata = rand.nextInt(16);
			hullGlass_block = WarpDrive.blockHulls_glass[0];
			hullGlass_metadata = hullPlain_metadata;
			break;
			
		case 6:
			hullPlain_block = WarpDrive.blockHulls_plain[1][0];
			hullPlain_metadata = rand.nextInt(16);
			hullGlass_block = WarpDrive.blockHulls_glass[1];
			hullGlass_metadata = hullPlain_metadata;
			break;
			
		case 10:	// disabled since it's tier3
			if (WarpDriveConfig.isIndustrialCraft2Loaded) {
				hullPlain_block = WarpDriveConfig.getModBlock("IC2", "blockAlloy");
				hullPlain_metadata = 0;
				hullGlass_block = WarpDriveConfig.getModBlock("IC2", "blockAlloyGlass");
				hullGlass_metadata = 0;
			}
			break;
		}
		
		// choose a solar panel
		if (WarpDriveConfig.isIndustrialCraft2Loaded) {
			if (WarpDriveConfig.isAdvancedSolarPanelLoaded) {
				solarPanel_block = WarpDriveConfig.getModBlock("AdvancedSolarPanel", "BlockAdvSolarPanel");
				solarPanel_metadata = rand.nextInt(2);
			} else {
				solarPanel_block = WarpDriveConfig.getModBlock("IC2", "blockGenerator");
				solarPanel_metadata = 3;
			}
		} else if (WarpDriveConfig.isEnderIOLoaded) {
			solarPanel_block = WarpDriveConfig.getModBlock("EnderIO", "blockSolarPanel");
			solarPanel_metadata = 0;
		} else {
			solarPanel_block = Blocks.air;
			solarPanel_metadata = 0;
		}
		
		// choose a wiring
		cable_block = Blocks.air;
		cable_metadata = 0;
		if (WarpDriveConfig.isIndustrialCraft2Loaded) {
			cable_block = WarpDriveConfig.getModBlock("IC2", "blockCable");
			cable_metadata = 0;
			
			switch (rand.nextInt(4)) {
			case 0:
				cable_metadata = 0;
				break;
			
			case 1:
				cable_metadata = 3;
				break;
			
			case 2:
				cable_metadata = 6;
				break;
			
			case 3:
				cable_metadata = 9;
				break;
			
			default:
				break;
			}
		}
		
		// choose a resource block
		resource_block = Blocks.redstone_block;
		resource_metadata = 0;
		switch (rand.nextInt(10)) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
			resource_block = Blocks.redstone_block;
			break;
		
		case 6:
		case 7:
			resource_block = Blocks.lapis_block;
			break;
		
		case 8:
		case 9:
			resource_block = Blocks.coal_block;
			break;
		
		default:
			break;
		}
	}
	
	public void setHullPlain(World world, final int x, final int y, final int z) {
		if (corrupted && (rand.nextInt(400) == 1)) {
			world.newExplosion(null, x + 0.5D, y + 0.5D, z + 0.5D, 17, false, true);
		} else if (corrupted && (rand.nextInt(10) == 1)) {
			world.setBlock(x, y, z, Blocks.air, 0, 2);
		} else {
			world.setBlock(x, y, z, hullPlain_block, hullPlain_metadata, 2);
		}
	}
	
	public void setHullGlass(World world, final int x, final int y, final int z) {
		if (corrupted && (rand.nextInt(5) == 1)) {
			world.setBlock(x, y, z, Blocks.air, 0, 2);
		} else {
			world.setBlock(x, y, z, hullGlass_block, hullGlass_metadata, 2);
		}
	}
	
	public void setSolarPanel(World world, final int x, final int y, final int z) {
		if (corrupted && (rand.nextInt(3) == 1)) {
			world.setBlock(x, y, z, Blocks.air, 0, 2);
		} else {
			world.setBlock(x, y, z, solarPanel_block, solarPanel_metadata, 2);
		}
	}
	
	public void setCable(World world, final int x, final int y, final int z) {
		if (corrupted && (rand.nextInt(3) == 1)) {
			world.setBlock(x, y, z, Blocks.air, 0, 2);
		} else {
			world.setBlock(x, y, z, cable_block, cable_metadata, 2);
		}
	}
	
	public void setResource(World world, final int x, final int y, final int z) {
		world.setBlock(x, y, z, resource_block, resource_metadata, 2);
	}
	
	public void fillInventoryWithLoot(final World worldObj, final Random rand, final int x, final int y, final int z, final String group) {
		final TileEntity tileEntity = worldObj.getTileEntity(x, y, z);
		if (tileEntity instanceof IInventory) {
			final IInventory inventory = (IInventory) tileEntity;
			final int size = inventory.getSizeInventory();
			final int countLoots = Math.min(3 + rand.nextInt(3), size);
			
			final GenericSet<Loot> lootSet = WarpDriveConfig.LootManager.getRandomSetFromGroup(rand, group);
			if (lootSet == null) {
				WarpDrive.logger.warn(String.format("No LootSet found with group %s for inventory @ DIM%d (%d %d %d): check your configuration",
				                                    group, worldObj.provider.dimensionId, x, y, z));
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
					WarpDrive.logger.info(String.format("Unable to find a valid loot from LootSet %s for inventory %s in @ DIM%d (%d %d %d): check your configuration",
					                                    lootSet.getFullName(),
					                                    inventory.getInventoryName() == null ? "-null name-" : inventory.getInventoryName(),
					                                    worldObj.provider.dimensionId, x, y, z));
				}
			}
		}
	}
	
	public void generateFromFile(final World world, final String filename, final int targetX, final int targetY, final int targetZ, final byte rotationSteps) {
		StringBuilder reason = new StringBuilder();
		final JumpShip jumpShip = JumpShip.createFromFile(filename, reason);
		if (jumpShip == null) {
			WarpDrive.logger.error(String.format("%s Failed to read schematic %s: %s", this, filename, reason.toString()));
			return;
		}
		deployShip(world, jumpShip, targetX, targetY, targetZ, rotationSteps);
	}
	
	public void deployShip(final World world, final JumpShip jumpShip, final int targetX, final int targetY, final int targetZ, final byte rotationSteps) {
		
		Transformation transformation = new Transformation(jumpShip, world, targetX - jumpShip.coreX, targetY - jumpShip.coreY, targetZ - jumpShip.coreZ, rotationSteps);
		for (int index = 0; index < jumpShip.jumpBlocks.length; index++) {
			// Deploy single block
			JumpBlock jumpBlock = jumpShip.jumpBlocks[index];
			
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
					WarpDrive.logger.info("At index " + index + ", deploying block " + Block.blockRegistry.getNameForObject(jumpBlock.block) + ":" + jumpBlock.blockMeta
					                      + " tileEntity " + jumpBlock.blockTileEntity + " NBT " + jumpBlock.blockNBT);
				}
				ChunkCoordinates targetLocation = transformation.apply(jumpBlock.x, jumpBlock.y, jumpBlock.z);
				Block blockAtTarget = world.getBlock(targetLocation.posX, targetLocation.posY, targetLocation.posZ);
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
