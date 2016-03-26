package cr0s.warpdrive.compat;

import java.util.Collection;

import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import cpw.mods.fml.common.Optional;

import cr0s.warpdrive.api.IBlockTransformer;
import cr0s.warpdrive.api.ITransformation;
import cr0s.warpdrive.config.WarpDriveConfig;

public class CompatImmersiveEngineering implements IBlockTransformer {
	
	private static Class<?> classTileEntityIEBase;
	
	public static void register() {
		try {
			classTileEntityIEBase = Class.forName("blusunrize.immersiveengineering.common.blocks.TileEntityIEBase");
			WarpDriveConfig.registerBlockTransformer("ImmersiveEngineering", new CompatImmersiveEngineering());
		} catch(ClassNotFoundException exception) {
			exception.printStackTrace();
		}
	}
	
	@Override
	public boolean isApplicable(final Block block, final int metadata, final TileEntity tileEntity) {
		return tileEntity instanceof IImmersiveConnectable || classTileEntityIEBase.isInstance(tileEntity);
	}
	
	@Override
	public boolean isJumpReady(TileEntity tileEntity) {
		return true;
	}
	
	@Override
	@Optional.Method(modid = "ImmersiveEngineering")
	public NBTBase saveExternals(final TileEntity tileEntity) {
		if (tileEntity instanceof IImmersiveConnectable) {
			ChunkCoordinates node = new ChunkCoordinates(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
			Collection<Connection> connections = ImmersiveNetHandler.INSTANCE.getConnections(tileEntity.getWorldObj(), node);
			if (connections != null) {
				NBTTagList nbtImmersiveEngineering = new NBTTagList();
				for (Connection connection : connections) {
					nbtImmersiveEngineering.appendTag(connection.writeToNBT());
				}
				ImmersiveNetHandler.INSTANCE.clearConnectionsOriginatingFrom(node, tileEntity.getWorldObj());
				return nbtImmersiveEngineering;
			}
		}
		return null;
	}
	
	@Override
	@Optional.Method(modid = "ImmersiveEngineering")
	public void remove(TileEntity tileEntity) {
		// nothing to do
	}
	
	@Override
	public int rotate(final Block block, final int metadata, NBTTagCompound nbtTileEntity, final ITransformation transformation) {
		byte rotationSteps = transformation.getRotationSteps();
		if (rotationSteps == 0 || !nbtTileEntity.hasKey("facing")) {
			return metadata;
		}
		
		int facing = nbtTileEntity.getInteger("facing");
		final int[] mrot = {  0,  1,  5,  4,  2,  3,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15 };
		switch (rotationSteps) {
		case 1:
			nbtTileEntity.setInteger("facing", mrot[facing]);
			return metadata;
		case 2:
			nbtTileEntity.setInteger("facing", mrot[mrot[facing]]);
			return metadata;
		case 3:
			nbtTileEntity.setInteger("facing", mrot[mrot[mrot[facing]]]);
			return metadata;
		default:
			return metadata;
		}
	}
	
	@Override
	@Optional.Method(modid = "ImmersiveEngineering")
	public void restoreExternals(TileEntity tileEntity, ITransformation transformation, NBTBase nbtBase) {
		NBTTagList nbtImmersiveEngineering = (NBTTagList) nbtBase;
		if (nbtImmersiveEngineering == null) {
			return;
		}
		World targetWorld = transformation.getTargetWorld();
		
		// powerPathList
		for (int connectionIndex = 0; connectionIndex < nbtImmersiveEngineering.tagCount(); connectionIndex++) {
			Connection connection = Connection.readFromNBT(nbtImmersiveEngineering.getCompoundTagAt(connectionIndex));
			connection.start = transformation.apply(connection.start);
			connection.end = transformation.apply(connection.end);
			
			ImmersiveNetHandler.INSTANCE.addConnection(targetWorld, new ChunkCoordinates(connection.start.posX, connection.start.posY, connection.start.posZ), connection);
		}
	}
}
