package cr0s.warpdrive.compat;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBlockTransformer;
import cr0s.warpdrive.api.ITransformation;
import cr0s.warpdrive.config.WarpDriveConfig;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class CompatPneumaticCraft implements IBlockTransformer {
	
	private static Class<?> classTileEntityBase;
	
	public static void register() {
		try {
			classTileEntityBase = Class.forName("pneumaticCraft.common.tileentity.TileEntityBase");
			WarpDriveConfig.registerBlockTransformer("PneumaticCraft", new CompatPneumaticCraft());
		} catch(ClassNotFoundException exception) {
			exception.printStackTrace();
		}
	}
	
	@Override
	public boolean isApplicable(final Block block, final int metadata, final TileEntity tileEntity) {
		WarpDrive.logger.info("isApplicable " + classTileEntityBase.isInstance(tileEntity));
		return classTileEntityBase.isInstance(tileEntity);
	}
	
	@Override
	public boolean isJumpReady(final Block block, final int metadata, final TileEntity tileEntity, StringBuilder reason) {
		return true;
	}
	
	@Override
	public NBTBase saveExternals(final TileEntity tileEntity) {
		return null;
	}
	
	@Override
	public void remove(TileEntity tileEntity) {
		// nothing to do
	}
	
	private static final byte[] mrotForgeDirection = {  0,  1,  5,  4,  2,  3,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15 };
	private static final byte[] mrotTextRotation   = {  1,  2,  3,  0 };
	private static final byte[] mrotDoor           = {  0,  1,  5,  4,  2,  3,  6,  7, 11, 10,  8,  9, 12, 13, 14, 15 };
	
	@Override
	public int rotate(final Block block, final int metadata, NBTTagCompound nbtTileEntity, final ITransformation transformation) {
		byte rotationSteps = transformation.getRotationSteps();
		if ( rotationSteps == 0
		  && !nbtTileEntity.hasKey("valveX")
		  && !nbtTileEntity.hasKey("multiBlockX")) {
			return metadata;
		}
		
		// hoppers
		if (nbtTileEntity.hasKey("inputDir")) {
			int inputDir = nbtTileEntity.getInteger("inputDir");
			switch (rotationSteps) {
			case 1:
				nbtTileEntity.setInteger("inputDir", mrotForgeDirection[inputDir]);
				return mrotForgeDirection[metadata];
			case 2:
				nbtTileEntity.setInteger("inputDir", mrotForgeDirection[mrotForgeDirection[inputDir]]);
				return mrotForgeDirection[mrotForgeDirection[metadata]];
			case 3:
				nbtTileEntity.setInteger("inputDir", mrotForgeDirection[mrotForgeDirection[mrotForgeDirection[inputDir]]]);
				return mrotForgeDirection[mrotForgeDirection[mrotForgeDirection[metadata]]];
			default:
				return metadata;
			}
		}
		
		// Aphorism signs
		if (nbtTileEntity.hasKey("textRotation")) {
			int textRotation = nbtTileEntity.getInteger("textRotation");
			switch (rotationSteps) {
			case 1:
				nbtTileEntity.setInteger("textRotation", mrotTextRotation[textRotation]);
				return mrotForgeDirection[metadata];
			case 2:
				nbtTileEntity.setInteger("textRotation", mrotTextRotation[mrotTextRotation[textRotation]]);
				return mrotForgeDirection[mrotForgeDirection[metadata]];
			case 3:
				nbtTileEntity.setInteger("textRotation", mrotTextRotation[mrotTextRotation[mrotTextRotation[textRotation]]]);
				return mrotForgeDirection[mrotForgeDirection[mrotForgeDirection[metadata]]];
			default:
				return metadata;
			}
		}
		
		// door base
		if (nbtTileEntity.hasKey("orientation")) {
			int orientation = nbtTileEntity.getInteger("orientation");
			switch (rotationSteps) {
			case 1:
				nbtTileEntity.setInteger("orientation", mrotTextRotation[orientation]);
				return metadata;
			case 2:
				nbtTileEntity.setInteger("orientation", mrotTextRotation[mrotTextRotation[orientation]]);
				return metadata;
			case 3:
				nbtTileEntity.setInteger("orientation", mrotTextRotation[mrotTextRotation[mrotTextRotation[orientation]]]);
				return metadata;
			default:
				return metadata;
			}
		}
		
		// door
		if (nbtTileEntity.getString("id").equals("TileEntityPneumaticDoor")) {
			switch (rotationSteps) {
			case 1:
				return mrotDoor[metadata];
			case 2:
				return mrotDoor[mrotDoor[metadata]];
			case 3:
				return mrotDoor[mrotDoor[mrotDoor[metadata]]];
			default:
				return metadata;
			}
		}
		
		// pressure chamber wall, pressure chamber window, pressure chamber interface
		if (nbtTileEntity.hasKey("valveX")) {
			WarpDrive.logger.info("hasKey valveX");
			BlockPos target = transformation.apply(
				nbtTileEntity.getInteger("valveX"),
				nbtTileEntity.getInteger("valveY"),
				nbtTileEntity.getInteger("valveZ"));
			nbtTileEntity.setInteger("valveX", target.getX());
			nbtTileEntity.setInteger("valveY", target.getY());
			nbtTileEntity.setInteger("valveZ", target.getZ());
			// use default metadata rotation
		}
		
		// pressure chamber valve
		if (nbtTileEntity.hasKey("multiBlockX")) {
			BlockPos sourceMin = new BlockPos(
				nbtTileEntity.getInteger("multiBlockX"),
				nbtTileEntity.getInteger("multiBlockY"),
				nbtTileEntity.getInteger("multiBlockZ"));
			int multiBlockSize = nbtTileEntity.getInteger("multiBlockSize");
			BlockPos sourceMax = new BlockPos(
				sourceMin.getX() + multiBlockSize - 1,
				sourceMin.getY() + multiBlockSize - 1,
				sourceMin.getZ() + multiBlockSize - 1);
			BlockPos target1 = transformation.apply(sourceMin);
			BlockPos target2 = transformation.apply(sourceMax);
			nbtTileEntity.setInteger("multiBlockX", Math.min(target1.getX(), target2.getX()));
			nbtTileEntity.setInteger("multiBlockY", Math.min(target1.getY(), target2.getY()));
			nbtTileEntity.setInteger("multiBlockZ", Math.min(target1.getZ(), target2.getZ()));
			
			NBTTagList tagListOld = nbtTileEntity.getTagList("Valves", 10);
			NBTTagList tagListNew = new NBTTagList();
			for (int index = 0; index < tagListOld.tagCount(); index++) {
				NBTTagCompound tagCompound = tagListOld.getCompoundTagAt(index);
				if (tagCompound != null) {
					BlockPos coordinates = transformation.apply(
						tagCompound.getInteger("xCoord"),
						tagCompound.getInteger("yCoord"),
						tagCompound.getInteger("zCoord"));
					tagCompound.setInteger("xCoord", coordinates.getX());
					tagCompound.setInteger("yCoord", coordinates.getY());
					tagCompound.setInteger("zCoord", coordinates.getZ());
					tagListNew.appendTag(tagCompound);
				}
			}
			nbtTileEntity.setTag("Valves", tagListNew);
			// use default metadata rotation
		}
		
		// all other tile entities: security station, programmer, pneumatic dynamo, charging station, air cannon, elevator caller, air compressor
		switch (rotationSteps) {
		case 1:
			return mrotForgeDirection[metadata];
		case 2:
			return mrotForgeDirection[mrotForgeDirection[metadata]];
		case 3:
			return mrotForgeDirection[mrotForgeDirection[mrotForgeDirection[metadata]]];
		default:
			return metadata;
		}
	}
	
	@Override
	public void restoreExternals(TileEntity tileEntity, ITransformation transformation, NBTBase nbtBase) {
		// nothing to do
	}
}
