package cr0s.warpdrive.event;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.ChunkData;
import cr0s.warpdrive.data.StateAir;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;

public class ChunkHandler {
	
	// persistent properties
	private static final Map<Integer, Map<Long, ChunkData>> registry = new ConcurrentHashMap<>(32);
	
	// computed properties
	public static long delayLogging = 0;
	
	/* event catchers */
	@SubscribeEvent
	public void onLoadWorld(WorldEvent.Load event) {
		if (event.world.isRemote || event.world.provider.dimensionId != 0) {
			return;
		}
		if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
			WarpDrive.logger.info(String.format("World %s load", 
			                                    event.world.provider.getDimensionName()));
		}
		// @TODO load star map
	}
	
	// (server side only)
	@SubscribeEvent
	public void onLoadChunkData(ChunkDataEvent.Load event) {
		if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
			WarpDrive.logger.info(String.format("World %s chunk %s loading data",
			                                    event.world.provider.getDimensionName(),
			                                    event.getChunk().getChunkCoordIntPair()));
		}
		
		ChunkData chunkData = getChunkData(event.world.provider.dimensionId, event.getChunk().xPosition, event.getChunk().zPosition);
		chunkData.load(event.getData());
	}
	
	// (called after data loading, only useful client side)
	@SubscribeEvent
	public void onLoadChunk(ChunkEvent.Load event) {
		if (event.world.isRemote) {
			if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
				WarpDrive.logger.info(String.format("World %s chunk %s loaded",
				                                    event.world.provider.getDimensionName(),
				                                    event.getChunk().getChunkCoordIntPair()));
			}
			
			ChunkData chunkData = getChunkData(event.world.provider.dimensionId, event.getChunk().xPosition, event.getChunk().zPosition);
			chunkData.load(new NBTTagCompound());
		}
	}
	
	// (server side only)
	@SubscribeEvent
	public void onWatchChunk(ChunkWatchEvent.Watch event) {
		if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
			WarpDrive.logger.info(String.format("World %s chunk %s watch by %s",
			                                    event.player.worldObj.provider.getDimensionName(),
			                                    event.chunk,
			                                    event.player));
		}
	}
	
	// (server side only)
	// not called when chunk wasn't changed since last save?
	@SubscribeEvent
	public void onSaveChunkData(ChunkDataEvent.Save event) {
		if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
			WarpDrive.logger.info(String.format("World %s chunk %s save data",
			                                    event.world.provider.getDimensionName(),
			                                    event.getChunk().getChunkCoordIntPair()));
		}
		ChunkData chunkData = getChunkData(event.world.provider.dimensionId, event.getChunk().xPosition, event.getChunk().zPosition);
		chunkData.save(event.getData());
	}
	
	// (server side only)
	@SubscribeEvent
	public void onSaveWorld(WorldEvent.Save event) {
		if (event.world.isRemote || event.world.provider.dimensionId != 0) {
			return;
		}
		if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
			WarpDrive.logger.info(String.format("World %s saved",
			                                    event.world.provider.getDimensionName()));
		}
		// @TODO save star map
	}
	
	@SubscribeEvent
	public void onUnloadWorld(WorldEvent.Unload event) {
		if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
			WarpDrive.logger.info(String.format("World %s unload",
			                                    event.world.provider.getDimensionName()));
		}
		
		// get dimension data
		Map<Long, ChunkData> mapRegistryItems = registry.get(event.world.provider.dimensionId);
		if (mapRegistryItems != null) {
			// unload chunks during shutdown
			for (ChunkData chunkData : mapRegistryItems.values()) {
				if (chunkData.isLoaded()) {
					chunkData.unload();
				}
			}
		}
		
		if (event.world.isRemote || event.world.provider.dimensionId != 0) {
			return;
		}
		// @TODO unload star map
	}
	
	
	// (not called when closing SSP game)
	@SubscribeEvent
	public void onUnloadChunk(ChunkEvent.Unload event) {
		if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
			WarpDrive.logger.info(String.format("World %s chunk %s unload",
			                                    event.world.provider.getDimensionName(),
			                                    event.getChunk().getChunkCoordIntPair()));
		}
		
		getChunkData(event.world.provider.dimensionId, event.getChunk().xPosition, event.getChunk().zPosition).unload();
	}
	
	// (not called when closing SSP game)
	@SubscribeEvent
	public void onUnwatchChunk(ChunkWatchEvent.UnWatch event) {
		if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
			WarpDrive.logger.info(String.format("World %s chunk %s unwatch by %s",
			                                    event.player.worldObj.provider.getDimensionName(),
			                                    event.chunk,
			                                    event.player));
		}
	}
	
	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event) {
		if (event.side != Side.SERVER || event.phase != Phase.END) {
			return;
		}
		updateTick(event.world);
	}
	
	public static void onBlockUpdated(final World world, final int x, final int y, final int z) {
		if (!world.isRemote) {
			getChunkData(world, x, y, z).onBlockUpdated(x, y, z);
		}
	}
	
	/* internal access */
	public static ChunkData getChunkData(final World world, final int x, final int y, final int z) {
		return getChunkData(world.provider.dimensionId, x, y, z);
	}
	
	private static ChunkData getChunkData(final int dimensionId, final int x, final int y, final int z) {
		assert (y >= 0 && y <= 255);
		return getChunkData(dimensionId, x >> 4, z >> 4);
	}
	
	private static ChunkData getChunkData(final int dimensionId, final int xChunk, final int zChunk) {
		// get dimension data
		Map<Long, ChunkData> mapRegistryItems = registry.get(dimensionId);
		// (lambda expressions are forcing synchronisation, so we don't use them here)
		if (mapRegistryItems == null) {
			mapRegistryItems = new ConcurrentHashMap<>(4096);
			registry.put(dimensionId, mapRegistryItems);
		}
		// get chunk data
		final long index = ChunkCoordIntPair.chunkXZ2Int(xChunk, zChunk);
		ChunkData chunkData = mapRegistryItems.get(index);
		// (lambda expressions are forcing synchronisation, so we don't use them here)
		if (chunkData == null) {
			chunkData = new ChunkData(xChunk, zChunk);
			mapRegistryItems.put(index, chunkData);
		}
		return chunkData;
	}
	
	private static boolean isLoaded(final Map<Long, ChunkData> mapRegistryItems, final int xChunk, final int zChunk) {
		// get chunk data
		final long index = ChunkCoordIntPair.chunkXZ2Int(xChunk, zChunk);
		ChunkData chunkData = mapRegistryItems.get(index);
		return chunkData != null && chunkData.isLoaded();
	}
	
	/* commons */
	public static boolean isLoaded(final World world, final int x, final int y, final int z) {
		return getChunkData(world, x, y, z).isLoaded();
	}
	
	/* air handling */
	public static StateAir getStateAir(final World world, final int x, final int y, final int z) {
		return getChunkData(world, x, y, z).getStateAir(world, x, y, z);
	}
	
	public static void updateTick(final World world) {
		// get dimension data
		Map<Long, ChunkData> mapRegistryItems = registry.get(world.provider.dimensionId);
		if (mapRegistryItems == null) {
			return;
		}
		int countLoaded = 0;
		for (Entry<Long, ChunkData> entryChunkData : mapRegistryItems.entrySet()) {
			if (updateTickLoopStep(world, mapRegistryItems, entryChunkData.getValue())) {
				continue;
			}
			countLoaded++;
		}
		if (WarpDriveConfig.LOGGING_CHUNK_HANDLER) {
			if (world.provider.dimensionId == 0) {
				delayLogging = (delayLogging + 1) % 600;
			}
			if (delayLogging == 1) {
				WarpDrive.logger.info(String.format("Dimension %d as %d / %d chunks loaded",
				                                    world.provider.dimensionId,
				                                    countLoaded,
				                                    mapRegistryItems.size()));
			}
		}
	}
	
	public static boolean updateTickLoopStep(final World world, final Map<Long, ChunkData> mapRegistryItems, final ChunkData chunkData) {
		// skip unloaded chunks
		if (!chunkData.isLoaded()) {
			return true;
		}
		chunkData.updateTick(world);
		ChunkCoordIntPair chunkCoordIntPair = chunkData.getChunkCoords();
		// skip chunks with unloaded neighbours
		if ( isLoaded(mapRegistryItems, chunkCoordIntPair.chunkXPos + 1, chunkCoordIntPair.chunkZPos)
		  && isLoaded(mapRegistryItems, chunkCoordIntPair.chunkXPos - 1, chunkCoordIntPair.chunkZPos)
		  && isLoaded(mapRegistryItems, chunkCoordIntPair.chunkXPos, chunkCoordIntPair.chunkZPos + 1)
		  && isLoaded(mapRegistryItems, chunkCoordIntPair.chunkXPos, chunkCoordIntPair.chunkZPos - 1) ) {
			chunkData.updateTick(world);
		}
		return false;
	}
}