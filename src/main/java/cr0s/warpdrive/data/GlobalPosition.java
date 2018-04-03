package cr0s.warpdrive.data;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldServer;
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
	
	public GlobalPosition(final TileEntity tileEntity) {
		this(tileEntity.getWorldObj().provider.dimensionId, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
	}
	
	public GlobalPosition(final Entity entity) {
		this(entity.worldObj.provider.dimensionId,
			(int) Math.floor(entity.posX),
			(int) Math.floor(entity.posY),
			(int) Math.floor(entity.posZ));
	}
	
	public WorldServer getWorldServerIfLoaded() {
		final WorldServer world = DimensionManager.getWorld(dimensionId);
		// skip unloaded worlds
		if (world == null) {
			return null;
		}
		
		boolean isLoaded;
		if (world.getChunkProvider() instanceof ChunkProviderServer) {
			ChunkProviderServer chunkProviderServer = (ChunkProviderServer) world.getChunkProvider();
			try {
				isLoaded = chunkProviderServer.loadedChunkHashMap.containsItem(ChunkCoordIntPair.chunkXZ2Int(x >> 4, z >> 4));
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
	
	public CelestialObject getCelestialObject(final boolean isRemote) {
		return CelestialObjectManager.get(isRemote, dimensionId, x, z);
	}
	
	public Vector3 getUniversalCoordinates(final boolean isRemote) {
		final CelestialObject celestialObject = CelestialObjectManager.get(isRemote, dimensionId, x, z);
		return StarMapRegistry.getUniversalCoordinates(celestialObject, x, y, z);
	}
	
	public VectorI getVectorI() {
		return new VectorI(x, y, z);
	}
	
	public double distance2To(final Entity entity) {
		if (entity.worldObj.provider.dimensionId != dimensionId) {
			return Double.MAX_VALUE;
		}
		final double newX = entity.posX - x;
		final double newY = entity.posY - y;
		final double newZ = entity.posZ - z;
		return newX * newX + newY * newY + newZ * newZ;
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
		return dimensionId == tileEntity.getWorldObj().provider.dimensionId
			&& x == tileEntity.xCoord && y == tileEntity.yCoord && z == tileEntity.zCoord;
	}
	
	@Override
	public boolean equals(final Object object) {
		if (object instanceof GlobalPosition) {
			GlobalPosition globalPosition = (GlobalPosition) object;
			return (dimensionId == globalPosition.dimensionId) && (x == globalPosition.x) && (y == globalPosition.y) && (z == globalPosition.z);
		} else if (object instanceof VectorI) {
			VectorI vector = (VectorI) object;
			return (x == vector.x) && (y == vector.y) && (z == vector.z);
		} else if (object instanceof TileEntity) {
			TileEntity tileEntity = (TileEntity) object;
			return (dimensionId == tileEntity.getWorldObj().provider.dimensionId) && (x == tileEntity.xCoord) && (y == tileEntity.yCoord) && (z == tileEntity.zCoord);
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return dimensionId << 24 + (x >> 10) << 12 + y << 10 + (z >> 10);
	}
}