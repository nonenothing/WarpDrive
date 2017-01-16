package cr0s.warpdrive.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import cr0s.warpdrive.api.IStarMapRegistryTileEntity;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import cr0s.warpdrive.LocalProfiler;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.movement.TileEntityShipCore;
import cr0s.warpdrive.block.movement.TileEntityShipCore.EnumShipCoreMode;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.StarMapRegistryItem.EnumStarMapEntryType;

/**
 * Registry of all known ships, jumpgates, etc. in the world
 * 
 * @author LemADEC
 */
public class StarMapRegistry {
	private final HashMap<Integer, CopyOnWriteArraySet<StarMapRegistryItem>> registry;
	private int countAdd = 0;
	private int countRemove = 0;
	private int countRead = 0;
	
	public StarMapRegistry() {
		registry = new HashMap<>();
	}
	
	public void updateInRegistry(IStarMapRegistryTileEntity tileEntity) {
		assert(tileEntity instanceof TileEntity);
		
		countRead++;
		if (WarpDriveConfig.LOGGING_STARMAP) {
			if (countRead % 1000 == 0) {
				WarpDrive.logger.info("Starmap registry stats: read " + countRead + " add " + countAdd + " remove " + countRemove + " => " + ((float) countRead) / (countRemove + countRead + countAdd) + "% read");
			}
		}
		CopyOnWriteArraySet<StarMapRegistryItem> setRegistryItems = registry.get(((TileEntity) tileEntity).getWorldObj().provider.dimensionId);
		if (setRegistryItems == null) {
			setRegistryItems = new CopyOnWriteArraySet<>();
		}
		for (StarMapRegistryItem registryItem : setRegistryItems) {
			if (registryItem.sameIdOrCoordinates(tileEntity)) {
				// already registered
				registryItem.update(tileEntity);    // @TODO probably not thread safe
				return;
			}
		}
		
		// not found => add
		countAdd++;
		setRegistryItems.add(new StarMapRegistryItem(tileEntity));
		registry.put(((TileEntity) tileEntity).getWorldObj().provider.dimensionId, setRegistryItems);
		if (WarpDriveConfig.LOGGING_STARMAP) {
			printRegistry("added");
		}
	}
	
	public void removeFromRegistry(IStarMapRegistryTileEntity tileEntity) {
		assert(tileEntity instanceof TileEntity);
		
		countRead++;
		Set<StarMapRegistryItem> setRegistryItems = registry.get(((TileEntity) tileEntity).getWorldObj().provider.dimensionId);
		if (setRegistryItems == null) {
			// noting to remove
			return;
		}
		
		for (StarMapRegistryItem registryItem : setRegistryItems) {
			if (registryItem.isSameTileEntity(tileEntity)) {
				// found it, remove and exit
				countRemove++;
				setRegistryItems.remove(registryItem);
				return;
			}
		}
		// not found => ignore it
	}
	
	public void onBlockUpdated(World world, final int x, final int y, final int z) {
		CopyOnWriteArraySet<StarMapRegistryItem> setStarMapRegistryItems = registry.get(world.provider.dimensionId);
		for (StarMapRegistryItem registryItem : setStarMapRegistryItems) {
			if (registryItem.contains(x, y, z)) {
				TileEntity tileEntity = world.getTileEntity(registryItem.x, registryItem.y, registryItem.z);
				if (tileEntity instanceof IStarMapRegistryTileEntity) {
					((IStarMapRegistryTileEntity) tileEntity).onBlockUpdatedInArea(new VectorI(x, y, z));
				}
			}
		}		
	}
	
	public ArrayList<StarMapRegistryItem> radarScan(TileEntity tileEntity, final int radius) {
		ArrayList<StarMapRegistryItem> res = new ArrayList<>(registry.size());
		cleanup();
		
		// printRegistry();
		int radius2 = radius * radius;
		for (Map.Entry<Integer, CopyOnWriteArraySet<StarMapRegistryItem>> entryDimension : registry.entrySet()) {
			for (StarMapRegistryItem entry : entryDimension.getValue()) {
				double dX = entry.x - tileEntity.xCoord;
				double dY = entry.y - tileEntity.yCoord;
				double dZ = entry.z - tileEntity.zCoord;
				double distance2 = dX * dX + dY * dY + dZ * dZ;
				
				if (distance2 <= radius2
				    && (entry.isolationRate == 0.0D || tileEntity.getWorldObj().rand.nextDouble() >= entry.isolationRate)
				    && (entry.getSpaceCoordinates() != null)) {
					res.add(entry);
				}
			}
		}
		
		return res;
	}
	
	public void printRegistry(final String trigger) {
		WarpDrive.logger.info("Starmap registry (" + registry.size() + " entries after " + trigger + "):");
		
		for (Map.Entry<Integer, CopyOnWriteArraySet<StarMapRegistryItem>> entryDimension : registry.entrySet()) {
			String message = "";
			for (StarMapRegistryItem registryItem : entryDimension.getValue()) {
				message += "\n- " + registryItem.type + " '" + registryItem.name + "' @ "
						+ registryItem.dimensionId + ": " + registryItem.x + " " + registryItem.y + " " + registryItem.z
						+ " with " + registryItem.isolationRate + " isolation rate";
			}
			WarpDrive.logger.info("- " + entryDimension.getValue().size() + " entries in dimension " + entryDimension.getKey() + ": " + message);
		}
	}
	
	public boolean isWarpCoreIntersectsWithOthers(TileEntityShipCore core) {
		StringBuilder reason = new StringBuilder();
		AxisAlignedBB aabb1, aabb2;
		cleanup();
		
		core.validateShipSpatialParameters(reason);
		aabb1 = AxisAlignedBB.getBoundingBox(core.minX, core.minY, core.minZ, core.maxX, core.maxY, core.maxZ);
		
		CopyOnWriteArraySet<StarMapRegistryItem> setRegistryItems = registry.get(core.getWorldObj().provider.dimensionId);
		if (setRegistryItems == null) {
			return false;
		}
		for (StarMapRegistryItem registryItem : setRegistryItems) {
			assert(registryItem.dimensionId == core.getWorldObj().provider.dimensionId);
			
			// only check cores
			if (registryItem.type != EnumStarMapEntryType.SHIP) {
				continue;
			}
			
			// Skip self
			if (registryItem.x == core.xCoord && registryItem.y == core.yCoord && registryItem.z == core.zCoord) {
				continue;
			}
			
			// Skip missing ship cores
			TileEntity tileEntity = core.getWorldObj().getTileEntity(registryItem.x, registryItem.y, registryItem.z);
			if (!(tileEntity instanceof TileEntityShipCore)) {
				continue;
			}
			TileEntityShipCore shipCore = (TileEntityShipCore) core.getWorldObj().getTileEntity(registryItem.x, registryItem.y, registryItem.z);
			
			// Skip offline warp cores
			if (shipCore.controller == null || shipCore.controller.getMode() == EnumShipCoreMode.IDLE || !shipCore.validateShipSpatialParameters(reason)) {
				continue;
			}
			
			// Compare areas for intersection
			aabb2 = AxisAlignedBB.getBoundingBox(registryItem.minX, registryItem.minY, registryItem.minZ, registryItem.maxX, registryItem.maxY, registryItem.maxZ);
			if (aabb1.intersectsWith(aabb2)) {
				return true;
			}
		}
		
		return false;
	}
	
	// do not call during tileEntity construction (readFromNBT and validate)
	private void cleanup() {
		LocalProfiler.start("Starmap registry cleanup");
		
		boolean isValid;
		for (Map.Entry<Integer, CopyOnWriteArraySet<StarMapRegistryItem>> entryDimension : registry.entrySet()) {
			WorldServer world = DimensionManager.getWorld(entryDimension.getKey());
			// skip unloaded worlds
			if (world == null) {
				continue;
			}
			for (StarMapRegistryItem registryItem : entryDimension.getValue()) {
				isValid = false;
				if (registryItem != null) {
					
					boolean isLoaded;
					if (world.getChunkProvider() instanceof ChunkProviderServer) {
						ChunkProviderServer chunkProviderServer = (ChunkProviderServer) world.getChunkProvider();
						try {
							isLoaded = chunkProviderServer.loadedChunkHashMap.containsItem(ChunkCoordIntPair.chunkXZ2Int(registryItem.x >> 4, registryItem.z >> 4));
						} catch (NoSuchFieldError exception) {
							isLoaded = chunkProviderServer.chunkExists(registryItem.x >> 4, registryItem.z >> 4);
						}
					} else {
						isLoaded = world.getChunkProvider().chunkExists(registryItem.x >> 4, registryItem.z >> 4);
					}
					// skip unloaded chunks
					if (!isLoaded) {
						continue;
					}
					
					// get block and tile entity
					Block block = world.getBlock(registryItem.x, registryItem.y, registryItem.z);
					
					TileEntity tileEntity = world.getTileEntity(registryItem.x, registryItem.y, registryItem.z);
					isValid = true;
					switch (registryItem.type) {
						case UNDEFINED:
							break;
						case SHIP:
							isValid = block == WarpDrive.blockShipCore && tileEntity != null && !tileEntity.isInvalid();
							break;
						case JUMPGATE:
							break;
						case PLANET:
							break;
						case STAR:
							break;
						case STRUCTURE:
							break;
						case WARP_ECHO:
							break;
						case ACCELERATOR:
							isValid = block == WarpDrive.blockAcceleratorController && tileEntity != null && !tileEntity.isInvalid();
							break;
						default:
							break;
					}
				}
				
				if (!isValid) {
					if (WarpDriveConfig.LOGGING_STARMAP) {
						if (registryItem == null) {
							WarpDrive.logger.info("Cleaning up starmap object ~null~");
						} else {
							WarpDrive.logger.info("Cleaning up starmap object " + registryItem.type + " at "
							                      + registryItem.dimensionId + " " + registryItem.x + " " + registryItem.y + " " + registryItem.z);
						}
					}
					countRemove++;
					entryDimension.getValue().remove(registryItem);
				}
			}
		}
		
		LocalProfiler.stop();
	}
}
