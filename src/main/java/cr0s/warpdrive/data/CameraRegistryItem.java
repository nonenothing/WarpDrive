package cr0s.warpdrive.data;

import cr0s.warpdrive.api.IVideoChannel;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

public class CameraRegistryItem {
	
	public int dimensionId;
	public ChunkPosition position;
	public int videoChannel;
	public EnumCameraType type;
	
	public CameraRegistryItem(final World world, final ChunkPosition position, final int videoChannel, final EnumCameraType enumCameraType) {
		this.videoChannel = videoChannel;
		this.position = position;
		this.dimensionId = world.provider.dimensionId;
		this.type = enumCameraType;
	}
	
	public boolean isTileEntity(final TileEntity tileEntity) {
		return tileEntity instanceof IVideoChannel
			&& dimensionId == tileEntity.getWorldObj().provider.dimensionId
			&& position.chunkPosX == tileEntity.xCoord
			&& position.chunkPosY == tileEntity.yCoord
			&& position.chunkPosZ == tileEntity.zCoord
			&& videoChannel == ((IVideoChannel) tileEntity).getVideoChannel();
	}
}