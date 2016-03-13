package cr0s.warpdrive.world;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;

public class WorldGenStructure {
	private Block hullPlain_block;
	private int hullPlain_metadata;
	private Block hullGlass_block;
	private int hullGlass_metadata;
	private Block solarPanel_block;
	private int solarPanel_metadata;
	private Block cable_block;
	private int cable_metadata;
	private boolean corrupted;
	private Random rand;
	
	public WorldGenStructure(final boolean corrupted, Random rand) {
		this.corrupted = corrupted;
		this.rand = rand;
		
		// choose a hull block
		switch (rand.nextInt(4)) {
		default:
		case 0:
			hullPlain_block = Blocks.stained_hardened_clay;
			hullPlain_metadata = rand.nextInt(16);
			hullGlass_block = Blocks.stained_glass;
			hullGlass_metadata = hullPlain_metadata;
			break;
			
		case 1:
		case 2:
			hullPlain_block = WarpDrive.blockHulls_plain[0];
			hullPlain_metadata = rand.nextInt(16);
			hullGlass_block = WarpDrive.blockHulls_glass[0];
			hullGlass_metadata = hullPlain_metadata;
			break;
			
		case 3:
			hullPlain_block = WarpDrive.blockHulls_plain[1];
			hullPlain_metadata = rand.nextInt(16);
			hullGlass_block = WarpDrive.blockHulls_glass[1];
			hullGlass_metadata = hullPlain_metadata;
			break;
			
		case 10:	// disabled since it's tier3
			if (WarpDriveConfig.isIndustrialCraft2loaded) {
				hullPlain_block = WarpDriveConfig.getModBlock("IC2", "blockAlloy");
				hullPlain_metadata = 0;
				hullGlass_block = WarpDriveConfig.getModBlock("IC2", "blockAlloyGlass");
				hullGlass_metadata = 0;
			}
			break;
		}
		
		// choose a solar panel
		if (WarpDriveConfig.isIndustrialCraft2loaded) {
			if (WarpDriveConfig.isAdvancedSolarPanelLoaded) {
				solarPanel_block = WarpDriveConfig.getModBlock("AdvancedSolarPanel", "BlockAdvSolarPanel");
				solarPanel_metadata = rand.nextInt(2);
			} else {
				solarPanel_block = WarpDriveConfig.getModBlock("IC2", "blockGenerator");
				solarPanel_metadata = 3;
			}
		} else if (WarpDriveConfig.isEnderIOloaded) {
			solarPanel_block = WarpDriveConfig.getModBlock("EnderIO", "blockSolarPanel");
			solarPanel_metadata = 0;
		} else {
			solarPanel_block = Blocks.air;
			solarPanel_metadata = 0;
		}
		
		// choose a wiring
		cable_block = Blocks.air;
		cable_metadata = 0;
		if (WarpDriveConfig.isIndustrialCraft2loaded) {
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
	}
	
	public void setHullPlain(World world, final int x, final int y, final int z) {
		if (corrupted && (rand.nextInt(15) == 1)) {
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
	
	public void setCable(World world, final int x, final int y, final int z) {
		if (corrupted && (rand.nextInt(5) == 1)) {
			world.setBlock(x, y, z, Blocks.air, 0, 2);
		} else {
			world.setBlock(x, y, z, cable_block, cable_metadata, 2);
		}
	}
	
	public void setSolarPanel(World world, final int x, final int y, final int z) {
		if (corrupted && (rand.nextInt(5) == 1)) {
			world.setBlock(x, y, z, Blocks.air, 0, 2);
		} else {
			world.setBlock(x, y, z, solarPanel_block, solarPanel_metadata, 2);
		}
	}
}
