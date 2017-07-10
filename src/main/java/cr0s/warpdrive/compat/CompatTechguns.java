package cr0s.warpdrive.compat;

import cr0s.warpdrive.api.IBlockTransformer;
import cr0s.warpdrive.api.ITransformation;
import cr0s.warpdrive.config.WarpDriveConfig;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class CompatTechguns implements IBlockTransformer {
	
	private static Class<?> classBlockLamp;
	private static Class<?> classBlockBasicMachine;
	
	public static void register() {
		try {
			classBlockLamp = Class.forName("techguns.blocks.BlockTGLamp");
			classBlockBasicMachine = Class.forName("techguns.blocks.machines.BasicMachine");
			WarpDriveConfig.registerBlockTransformer("Techguns", new CompatTechguns());
		} catch(ClassNotFoundException exception) {
			exception.printStackTrace();
		}
	}
	
	@Override
	public boolean isApplicable(final Block block, final int metadata, final TileEntity tileEntity) {
		return classBlockLamp.isInstance(block)
			|| classBlockBasicMachine.isInstance(block);
	}
	
	@Override
	public boolean isJumpReady(final Block block, final int metadata, final TileEntity tileEntity, StringBuilder reason) {
		return true;
	}
	
	@Override
	public NBTBase saveExternals(final TileEntity tileEntity) {
		// nothing to do
		return null;
	}
	
	@Override
	public void remove(TileEntity tileEntity) {
		// nothing to do
	}
	
	// Transformation handling required:
	// Tile lamp: (metadata) 0 / 1 / 2 / 3 6 4 5 / 7 / 8 / 9 / 10 / 11 14 12 13 / 15    mrotLamp            techguns.blocks.BlockTGLamp
	// Tile basic machine: (metadata 0/1/4/9), rotation (byte) 0 1 2 3                  rotBasicMachine 	techguns.blocks.machines.BasicMachine:0/1/4/9
	// Tile basic machine: (metadata 2)                                                 -none-              techguns.blocks.machines.BasicMachine:2
	// Tile basic machine: (metadata 3 or 5), rotation (byte) 0 -1 -2 1                 rotRepairCamoBench  techguns.blocks.machines.BasicMachine:3/5
	// Tile basic machine: (metadata 6 or 8),
	//                      orientation (byte)   0 / 1 / 2 5 3 4                        orientExplosiveCharge	techguns.blocks.machines.BasicMachine:6/8
	//                      rotation (byte)      0 3 1 2                                rotExplosiveCharge
	
	
	// -----------------------------------------          {  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15 };
	private static final int[]   mrotLamp               = {  0,  1,  2,  6,  5,  3,  4,  7,  8,  9, 10, 14, 13, 11, 12, 15 };
	private static final byte[]  rotBasicMachine        = {  1,  2,  3,  0,  5,  3,  4,  7,  8,  9, 10, 14, 13, 11, 12, 15 };
	private static final byte[]  orientExplosiveCharge  = {  0,  1,  5,  4,  2,  3,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15 };
	private static final byte[]  rotExplosiveCharge     = {  3,  2,  0,  1,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15 };
	
	private static byte rotRepairCamoBench(final byte value) {
		switch (value) {
		case  1: return (byte)  0;
		case  0: return (byte) -1;
		case -1: return (byte) -2;
		case -2: return (byte)  1;
		default: return value;
		}
	}
	
	@Override
	public int rotate(final Block block, final int metadata, NBTTagCompound nbtTileEntity, final ITransformation transformation) {
		final byte rotationSteps = transformation.getRotationSteps();
		if (rotationSteps == 0) {
			return metadata;
		}
		
		if (classBlockLamp.isInstance(block)) {
			switch (rotationSteps) {
			case 1:
				return mrotLamp[metadata];
			case 2:
				return mrotLamp[mrotLamp[metadata]];
			case 3:
				return mrotLamp[mrotLamp[mrotLamp[metadata]]];
			default:
				return metadata;
			}
		}
		if (classBlockBasicMachine.isInstance(block)) {
			switch(metadata) {
			case 0:
			case 1:
			case 4:
			case 9:
				if (nbtTileEntity.hasKey("rotation")) {
					final byte rotation = nbtTileEntity.getByte("rotation");
					switch (rotationSteps) {
					case 1:
						nbtTileEntity.setByte("rotation", rotBasicMachine[rotation]);
						return metadata;
					case 2:
						nbtTileEntity.setByte("rotation", rotBasicMachine[rotBasicMachine[rotation]]);
						return metadata;
					case 3:
						nbtTileEntity.setByte("rotation", rotBasicMachine[rotBasicMachine[rotBasicMachine[rotation]]]);
						return metadata;
					default:
						return metadata;
					}
				}
				break;
			
			case 2:
				// no rotation
				return metadata;
			
			case 3:
			case 5:
				if (nbtTileEntity.hasKey("rotation")) {
					final byte rotation = nbtTileEntity.getByte("rotation");
					switch (rotationSteps) {
					case 1:
						nbtTileEntity.setByte("rotation", rotRepairCamoBench(rotation));
						return metadata;
					case 2:
						nbtTileEntity.setByte("rotation", rotRepairCamoBench(rotRepairCamoBench(rotation)));
						return metadata;
					case 3:
						nbtTileEntity.setByte("rotation", rotRepairCamoBench(rotRepairCamoBench(rotRepairCamoBench(rotation))));
						return metadata;
					default:
						return metadata;
					}
				}
				break;
			
			case 6:
			case 8:
				if (nbtTileEntity.hasKey("orientation")) {
					final byte orientation = nbtTileEntity.getByte("orientation");
					final byte rotation = nbtTileEntity.getByte("rotation");
					if (orientation == 0 || orientation == 1) {
						return metadata;
					}
					switch (rotationSteps) {
					case 1:
						nbtTileEntity.setByte("orientation", orientExplosiveCharge[orientation]);
						nbtTileEntity.setByte("rotation", rotExplosiveCharge[rotation]);
						return metadata;
					case 2:
						nbtTileEntity.setByte("orientation", orientExplosiveCharge[orientExplosiveCharge[orientation]]);
						nbtTileEntity.setByte("rotation", rotExplosiveCharge[rotExplosiveCharge[rotation]]);
						return metadata;
					case 3:
						nbtTileEntity.setByte("orientation", orientExplosiveCharge[orientExplosiveCharge[orientExplosiveCharge[orientation]]]);
						nbtTileEntity.setByte("rotation", rotExplosiveCharge[rotExplosiveCharge[rotExplosiveCharge[rotation]]]);
						return metadata;
					default:
						return metadata;
					}
				}
				break;
			
			case 7:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
			default:
				return metadata;
			}
		}
		
		return metadata;
	}
	
	@Override
	public void restoreExternals(TileEntity tileEntity, ITransformation transformation, NBTBase nbtBase) {
		// nothing to do
	}
}
