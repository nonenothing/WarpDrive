package cr0s.warpdrive.compat;

import cr0s.warpdrive.api.IBlockTransformer;
import cr0s.warpdrive.api.ITransformation;
import cr0s.warpdrive.config.WarpDriveConfig;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CompatPneumaticCraft implements IBlockTransformer {
	
	private static Class<?> classTileEntityBase;
	
	public static void register() {
		try {
			classTileEntityBase = Class.forName("pneumaticCraft.common.tileentity.TileEntityBase");
			WarpDriveConfig.registerBlockTransformer("PneumaticCraft", new CompatPneumaticCraft());
		} catch(final ClassNotFoundException exception) {
			exception.printStackTrace();
		}
	}
	
	@Override
	public boolean isApplicable(final Block block, final int metadata, final TileEntity tileEntity) {
		return classTileEntityBase.isInstance(tileEntity);
	}
	
	@Override
	public boolean isJumpReady(final Block block, final int metadata, final TileEntity tileEntity, final StringBuilder reason) {
		return true;
	}
	
	@Override
	public NBTBase saveExternals(final World world, final int x, final int y, final int z, final Block block, final int blockMeta, final TileEntity tileEntity) {
		// nothing to do
		return null;
	}
	
	@Override
	public void removeExternals(final World world, final int x, final int y, final int z,
	                            final Block block, final int blockMeta, final TileEntity tileEntity) {
		// nothing to do
	}
	
	private static final byte[] mrotForgeDirection = {  0,  1,  5,  4,  2,  3,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15 };
	private static final byte[] mrotTextRotation   = {  1,  2,  3,  0 };
	private static final byte[] mrotDoor           = {  0,  1,  5,  4,  2,  3,  6,  7, 11, 10,  8,  9, 12, 13, 14, 15 };
	
	@Override
	public int rotate(final Block block, final int metadata, final NBTTagCompound nbtTileEntity, final ITransformation transformation) {
		final byte rotationSteps = transformation.getRotationSteps();
		if ( rotationSteps == 0
		  && !nbtTileEntity.hasKey("valveX")
		  && !nbtTileEntity.hasKey("multiBlockX")) {
			return metadata;
		}
		
		// hoppers
		if (nbtTileEntity.hasKey("inputDir")) {
			final int inputDir = nbtTileEntity.getInteger("inputDir");
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
			final int textRotation = nbtTileEntity.getInteger("textRotation");
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
			final int orientation = nbtTileEntity.getInteger("orientation");
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
			final BlockPos target = transformation.apply(
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
			final BlockPos sourceMin = new BlockPos(
				nbtTileEntity.getInteger("multiBlockX"),
				nbtTileEntity.getInteger("multiBlockY"),
				nbtTileEntity.getInteger("multiBlockZ"));
			final int multiBlockSize = nbtTileEntity.getInteger("multiBlockSize");
			final BlockPos sourceMax = new BlockPos(
				sourceMin.getX() + multiBlockSize - 1,
				sourceMin.getY() + multiBlockSize - 1,
				sourceMin.getZ() + multiBlockSize - 1);
			final BlockPos target1 = transformation.apply(sourceMin);
			final BlockPos target2 = transformation.apply(sourceMax);
			nbtTileEntity.setInteger("multiBlockX", Math.min(target1.getX(), target2.getX()));
			nbtTileEntity.setInteger("multiBlockY", Math.min(target1.getY(), target2.getY()));
			nbtTileEntity.setInteger("multiBlockZ", Math.min(target1.getZ(), target2.getZ()));
			
			final NBTTagList tagListOld = nbtTileEntity.getTagList("Valves", 10);
			final NBTTagList tagListNew = new NBTTagList();
			for (int index = 0; index < tagListOld.tagCount(); index++) {
				final NBTTagCompound tagCompound = tagListOld.getCompoundTagAt(index);
				if (tagCompound != null) {
					final BlockPos coordinates = transformation.apply(
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
	public void restoreExternals(final World world, final int x, final int y, final int z,
	                             final Block block, final int blockMeta, final TileEntity tileEntity,
	                             final ITransformation transformation, final NBTBase nbtBase) {
		// nothing to do
	}
}
