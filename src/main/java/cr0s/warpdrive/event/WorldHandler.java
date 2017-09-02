package cr0s.warpdrive.event;

import cr0s.warpdrive.BreathingManager;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.data.CelestialObjectManager;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.CelestialObject;
import cr0s.warpdrive.network.PacketHandler;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;

/**
*
* @author LemADEC
*/
public class WorldHandler {
	
	//TODO: register as event receiver
	public void onChunkLoaded(ChunkWatchEvent event) {
		ChunkPos chunk = event.getChunk();
		
		// Check chunk for locating in cloaked areas
		WarpDrive.logger.info("onChunkLoaded " + chunk.chunkXPos + " " + chunk.chunkZPos);
		WarpDrive.cloaks.onChunkLoaded(event.getPlayer(), chunk.chunkXPos, chunk.chunkZPos);
		
		/*
		List<Chunk> list = new ArrayList<Chunk>();
		list.add(c);
		
		// Send obscured chunk
		System.out.println("[Cloak] Sending to player " + p.username + " obscured chunk at (" + chunk.chunkXPos + "; " + chunk.chunkZPos + ")");
		((EntityPlayerMP)p).connection.sendPacketToPlayer(new Packet56MapChunks(list));
		*/
	}
	
	// Server side
	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event){
		if (event.getWorld().isRemote) {
			return;
		}			
		// WarpDrive.logger.info("onEntityJoinWorld " + event.entity);
		if (event.getEntity() instanceof EntityLivingBase) {
			final EntityLivingBase entityLivingBase = (EntityLivingBase) event.getEntity();
			final int x = MathHelper.floor_double(event.getEntity().posX);
			final int y = MathHelper.floor_double(event.getEntity().posY);
			final int z = MathHelper.floor_double(event.getEntity().posZ);
			final CelestialObject celestialObject = CelestialObjectManager.get(event.getWorld(), x, z);
			
			if (event.getEntity() instanceof EntityPlayerMP) {
				WarpDrive.cloaks.onPlayerJoinWorld((EntityPlayerMP) event.getEntity(), event.getWorld());
				PacketHandler.sendClientSync((EntityPlayerMP) event.getEntity(), celestialObject);
				
			} else {
				if (celestialObject == null) {
					// unregistered dimension => exit
					return;
				}
				if (event.getEntity().ticksExisted > 5) {
					// just changing dimension
					return;
				}
				if (!celestialObject.hasAtmosphere()) {
					final boolean canJoin = BreathingManager.onLivingJoinEvent(entityLivingBase, x, y, z);
					if (!canJoin) {
						event.setCanceled(true);
					}
				}
				if (!celestialObject.isInsideBorder(event.getEntity().posX, event.getEntity().posZ)) {
					event.setCanceled(true);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
		WarpDrive.logger.info(String.format("onPlayerChangedDimension %s %d -> %d",
		                                    event.player.getName(), event.fromDim, event.toDim ));
		WarpDrive.cloaks.onPlayerJoinWorld((EntityPlayerMP) event.player, ((EntityPlayerMP) event.player).worldObj);
	}
	
	// Client side
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onClientConnectedToServer(ClientConnectedToServerEvent event) {
		// WarpDrive.logger.info("onClientConnectedToServer connectionType " + event.connectionType + " isLocal " + event.isLocal);
		WarpDrive.cloaks.onClientChangingDimension();
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onWorldUnload(WorldEvent.Unload event) {
		// WarpDrive.logger.info("onWorldUnload world " + event.getWorld());
		WarpDrive.cloaks.onClientChangingDimension();
	}
	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent event) {
		if (event.side != Side.SERVER || event.phase != Phase.END) {
			return;
		}
		
		AbstractSequencer.updateTick();
	}
	
	@SubscribeEvent
	public void onBlockUpdated(BlockEvent blockEvent) {
		if (WarpDriveConfig.LOGGING_BREAK_PLACE && WarpDrive.isDev) {
			WarpDrive.logger.info("onBlockUpdate args " + blockEvent.getState()
			                      + " actual " + blockEvent.getWorld().getBlockState(blockEvent.getPos()));
		}
		WarpDrive.starMap.onBlockUpdated(blockEvent.getWorld(), blockEvent.getPos(), blockEvent.getState());
		ChunkHandler.onBlockUpdated(blockEvent.getWorld(), blockEvent.getPos().getX(), blockEvent.getPos().getY(), blockEvent.getPos().getZ());
	}
}
