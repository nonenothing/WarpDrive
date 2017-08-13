package cr0s.warpdrive.compat;


import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBlockTransformer;
import cr0s.warpdrive.api.ITransformation;
import cr0s.warpdrive.block.breathing.BlockAirFlow;
import cr0s.warpdrive.block.breathing.BlockAirSource;
import cr0s.warpdrive.block.energy.TileEntityEnergyBank;
import cr0s.warpdrive.block.hull.BlockHullSlab;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.ChunkData;
import cr0s.warpdrive.data.StateAir;
import cr0s.warpdrive.event.ChunkHandler;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class CompatWarpDrive implements IBlockTransformer {
	
	public static void register() {
		WarpDriveConfig.registerBlockTransformer("WarpDrive", new CompatWarpDrive());
	}
	
	@Override
	public boolean isApplicable(final Block block, final int metadata, final TileEntity tileEntity) {
		return block instanceof BlockHullSlab
		    || tileEntity instanceof TileEntityEnergyBank;
	}
	
	@Override
	public boolean isJumpReady(final Block block, final int metadata, final TileEntity tileEntity, StringBuilder reason) {
		return true;
	}
	
	@Override
	public NBTBase saveExternals(final World world, final int x, final int y, final int z,
	                             final Block block, final int blockMeta, final TileEntity tileEntity) {
		if (block instanceof BlockAirFlow || block instanceof BlockAirSource) {
			final ChunkData chunkData = ChunkHandler.getChunkData(world, x, y, z, false);
			if (chunkData == null) {
				WarpDrive.logger.error(String.format("CompatWarpDrive trying to get data from an non-loaded chunk in %s @ (%d %d %d)",
				                                     world.provider.getDimensionName(), x, y, z));
				assert(false);
				return null;
			}
			final int dataAir = chunkData.getDataAir(x, y, z);
			if (dataAir == StateAir.AIR_DEFAULT) {
				return null;
			}
			final NBTTagCompound tagCompound = new NBTTagCompound();
			tagCompound.setInteger("dataAir", dataAir);
			return tagCompound;
		}
		return null;
	}
	
	@Override
	public void remove(TileEntity tileEntity) {
		// nothing to do
	}
	
	private static final short[] mrotDirection = {  0,  1,  5,  4,  2,  3,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15 };
	private static final short[] mrotHullSlab  = {  0,  1,  5,  4,  2,  3,  6,  7, 11, 10,  8,  9, 12, 13, 15, 14 };
	
	private byte[] rotate_byteArray(final byte rotationSteps, final byte[] data) {
		byte[] newData = data.clone();
		for (int index = 0; index < data.length; index++) {
			switch (rotationSteps) {
			case 1:
				newData[mrotDirection[index]] = data[index];
				break;
			case 2:
				newData[mrotDirection[mrotDirection[index]]] = data[index];
				break;
			case 3:
				newData[mrotDirection[mrotDirection[mrotDirection[index]]]] = data[index];
				break;
			default:
				break;
			}
		}
		return newData;
	}
	
	@Override
	public int rotate(final Block block, final int metadata, NBTTagCompound nbtTileEntity, final ITransformation transformation) {
		byte rotationSteps = transformation.getRotationSteps();
		
		// Hull slabs
		if (block instanceof BlockHullSlab) {
			switch (rotationSteps) {
			case 1:
				return mrotHullSlab[metadata];
			case 2:
				return mrotHullSlab[mrotHullSlab[metadata]];
			case 3:
				return mrotHullSlab[mrotHullSlab[mrotHullSlab[metadata]]];
			default:
				return metadata;
			}
		}
		
		// Energy bank sides
		if (nbtTileEntity != null && nbtTileEntity.hasKey("modeSide")) {
			nbtTileEntity.setByteArray("modeSide", rotate_byteArray(rotationSteps, nbtTileEntity.getByteArray("modeSide")));
		}
		return metadata;
	}
	
	@Override
	public void restoreExternals(final World world, final int x, final int y, final int z,
	                             final Block block, final int blockMeta, final TileEntity tileEntity,
	                             final ITransformation transformation, final NBTBase nbtBase) {
		if (nbtBase == null) {
			return;
		}
		if (!(nbtBase instanceof NBTTagCompound)) {
			return;
		}
		if (((NBTTagCompound) nbtBase).hasKey("dataAir")) {
			final int dataAir = ((NBTTagCompound) nbtBase).getInteger("dataAir");
			final ChunkData chunkData = ChunkHandler.getChunkData(world, x, y, z, false);
			if (chunkData == null) {
				WarpDrive.logger.error(String.format("CompatWarpDrive trying to set data from an non-loaded chunk in %s @ (%d %d %d)",
				                                     world.provider.getDimensionName(), x, y, z));
				assert(false);
				return;
			}
			chunkData.setDataAir(x, y, z, dataAir);
		}
	}
}
