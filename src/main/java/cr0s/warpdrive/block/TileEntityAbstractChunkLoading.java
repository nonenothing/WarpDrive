package cr0s.warpdrive.block;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.event.ChunkLoadingHandler;

import java.util.ArrayList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;

import net.minecraftforge.common.ForgeChunkManager.Ticket;

public abstract class TileEntityAbstractChunkLoading extends TileEntityAbstractEnergy {
	
	// persistent properties
	protected ChunkPos chunkMin = null;
	protected ChunkPos chunkMax = null;
	
	// computed properties
	private Ticket ticket = null;
	private boolean isRefreshNeeded = true;
	protected boolean areChunksLoaded = false;
	
	@Override
	protected void onFirstUpdateTick() {
		super.onFirstUpdateTick();
		
		if (worldObj.isRemote) {
			return;
		}
		
		if ( chunkMin == null
		  || chunkMax == null ) {
			WarpDrive.logger.warn(this + " No chunk coordinates defined, assuming current chunk");
			chunkMin = worldObj.getChunkFromBlockCoords(pos).getChunkCoordIntPair();
			chunkMax = worldObj.getChunkFromBlockCoords(pos).getChunkCoordIntPair();
		}
	}
	
	@Override
	public void update() {
		super.update();
		
		if (worldObj.isRemote) {
			return;
		}
		
		if ( isRefreshNeeded
		  || shouldChunkLoad() != areChunksLoaded ) {
			refreshLoading(isRefreshNeeded);
			isRefreshNeeded = false;
		}
	}
	
	public abstract boolean shouldChunkLoad();
	
	public void refreshChunkLoading() {
		isRefreshNeeded = true;
	}
	
	public synchronized void refreshLoading(final boolean force) {
		final boolean shouldChunkLoad = shouldChunkLoad();
		if (shouldChunkLoad) {
			if (ticket == null) {
				chunkloading_giveTicket(ChunkLoadingHandler.forgeTicket_requestNormal(worldObj, this));
			} else if (force) {
				ChunkLoadingHandler.forgeTicket_clearChunks(ticket);
			}
			
			if (!areChunksLoaded || force) {
				final int ticketSize = ticket.getMaxChunkListDepth();
				final ArrayList<ChunkPos> chunksToLoad = getChunksToLoad();
				if (chunksToLoad.size() > ticketSize) {
					WarpDrive.logger.error(String.format("Too many chunk requested for loading @ %s (%d %d %d)",
					                                     worldObj.provider.getSaveFolder(),
					                                     pos.getX(), pos.getY(), pos.getZ()));
					return;
				}
				
				for (final ChunkPos chunk : chunksToLoad) {
					ChunkLoadingHandler.forgeTicket_addChunks(ticket, chunk);
				}
				areChunksLoaded = true;
			}
			
		} else if (ticket != null) {
			ChunkLoadingHandler.forgeTicket_release(ticket);
			ticket = null;
			areChunksLoaded = false;
		}
	}
	
	public void chunkloading_giveTicket(final Ticket ticket) {
		if (this.ticket != null) {
			ChunkLoadingHandler.forgeTicket_release(this.ticket);
			this.ticket = null;
		}
		this.ticket = ticket;
	}
	
	public int chunkloading_getArea() {
		return (chunkMax.chunkXPos - chunkMin.chunkXPos + 1)
		     * (chunkMax.chunkZPos - chunkMin.chunkZPos + 1);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
		tagCompound = super.writeToNBT(tagCompound);
		
		if (chunkMin == null) {
			chunkMin = worldObj.getChunkFromBlockCoords(pos).getChunkCoordIntPair();
		}
		
		if (chunkMax == null) {
			chunkMax = worldObj.getChunkFromBlockCoords(pos).getChunkCoordIntPair();
		}
		
		tagCompound.setInteger("minChunkX", chunkMin.chunkXPos);
		tagCompound.setInteger("minChunkZ", chunkMin.chunkZPos);
		tagCompound.setInteger("maxChunkX", chunkMax.chunkXPos);
		tagCompound.setInteger("maxChunkZ", chunkMax.chunkZPos);
		return tagCompound;
	}
	
	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		
		if (tagCompound.hasKey("minChunkX")) {
			final int xMin = tagCompound.getInteger("minChunkX");
			final int zMin = tagCompound.getInteger("minChunkZ");
			chunkMin = new ChunkPos(xMin, zMin);
			
			final int xMax = tagCompound.getInteger("maxChunkX");
			final int zMax = tagCompound.getInteger("maxChunkZ");
			chunkMax = new ChunkPos(xMax, zMax);
		}
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		if (ticket != null) {
			ChunkLoadingHandler.forgeTicket_release(ticket);
			ticket = null;
		}
	}
	
	public ArrayList<ChunkPos> getChunksToLoad() {
		if (!shouldChunkLoad()) {
			return null;
		}
		
		assert(chunkMin.chunkXPos <= chunkMax.chunkXPos);
		assert(chunkMin.chunkZPos <= chunkMax.chunkZPos);
		
		final int count = chunkloading_getArea();
		if (WarpDriveConfig.LOGGING_CHUNK_LOADING) {
			WarpDrive.logger.info(String.format("Collecting %d chunks to be loaded @ %s from %s to %s",
			                                    count,
			                                    worldObj.provider.getSaveFolder(),
			                                    chunkMin, chunkMax));
		}
		final ArrayList<ChunkPos> chunkCoords = new ArrayList<>(count);
		
		for (int x = chunkMin.chunkXPos; x <= chunkMax.chunkXPos; x++) {
			for (int z = chunkMin.chunkZPos; z <= chunkMax.chunkZPos; z++) {
				chunkCoords.add(new ChunkPos(x, z));
			}
		}
		
		return chunkCoords;
	}
}
