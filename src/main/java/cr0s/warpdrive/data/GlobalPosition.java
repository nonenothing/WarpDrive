package cr0s.warpdrive.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;

import net.minecraftforge.common.DimensionManager;

public class GlobalPosition {
	public final int dimensionId;
	public final int x, y, z;
	
	public GlobalPosition(final int dimensionId, final int x, final int y, final int z) {
		this.dimensionId = dimensionId;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public GlobalPosition(final int dimensionId, final BlockPos blockPos) {
		this.dimensionId = dimensionId;
		this.x = blockPos.getX();
		this.y = blockPos.getY();
		this.z = blockPos.getZ();
	}
	
	public GlobalPosition(TileEntity tileEntity) {
		this(tileEntity.getWorld().provider.getDimension(), tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ());
	}
	
	public WorldServer getWorldServerIfLoaded() {
		WorldServer world = DimensionManager.getWorld(dimensionId);
		// skip unloaded worlds
		if (world == null) {
			return null;
		}
		
		boolean isLoaded = false;
		if (world.getChunkProvider() instanceof ChunkProviderServer) {
			ChunkProviderServer chunkProviderServer = world.getChunkProvider();
			try {
				long i = ChunkPos.chunkXZ2Int(x >> 4, z >> 4);
				Chunk chunk = chunkProviderServer.id2ChunkMap.get(i);
				if (chunk != null) {
					isLoaded = !chunk.unloaded;
				}
			} catch (NoSuchFieldError exception) {
				isLoaded = chunkProviderServer.chunkExists(x >> 4, z >> 4);
			}
		} else {
			isLoaded = world.getChunkProvider().chunkExists(x >> 4, z >> 4);
		}
		// skip unloaded chunks
		if (!isLoaded) {
			return null;
		}
		return world;
	}
	
	public boolean isLoaded() {
		return getWorldServerIfLoaded() != null;
	}
	
	public Vector3 getUniversalCoordinates(final boolean isRemote) {
		CelestialObject celestialObject = CelestialObjectManager.get(isRemote, dimensionId, x, z);
		return StarMapRegistry.getUniversalCoordinates(celestialObject, x, y, z);
	}
	
	public GlobalPosition(final NBTTagCompound tagCompound) {
		dimensionId = tagCompound.getInteger("dimensionId");
		x = tagCompound.getInteger("x");
		y = tagCompound.getInteger("y");
		z = tagCompound.getInteger("z");
	}
	
	public void writeToNBT(final NBTTagCompound tagCompound) {
		tagCompound.setInteger("dimensionId", dimensionId);
		tagCompound.setInteger("x", x);
		tagCompound.setInteger("y", y);
		tagCompound.setInteger("z", z);
	}
	
	public boolean equals(final TileEntity tileEntity) {
		return dimensionId == tileEntity.getWorld().provider.getDimension()
			&& x == tileEntity.getPos().getX() && y == tileEntity.getPos().getY() && z == tileEntity.getPos().getZ();
	}
	
	@Override
	public int hashCode() {
		return dimensionId << 24 + (x >> 10) << 12 + y << 10 + (z >> 10);
	}
}