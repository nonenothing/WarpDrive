package cr0s.warpdrive.network;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.CelestialObject;
import cr0s.warpdrive.data.CloakedArea;
import cr0s.warpdrive.data.Vector3;
import cr0s.warpdrive.data.VectorI;

import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class PacketHandler {
	
	private static final SimpleNetworkWrapper simpleNetworkManager = NetworkRegistry.INSTANCE.newSimpleChannel(WarpDrive.MODID);
	private static Method EntityTrackerEntry_getPacketForThisEntity;
	
	public static void init() {
		// Forge packets
		simpleNetworkManager.registerMessage(MessageBeamEffect.class          , MessageBeamEffect.class          , 0, Side.CLIENT);
		simpleNetworkManager.registerMessage(MessageClientSync.class          , MessageClientSync.class          , 2, Side.CLIENT);
		simpleNetworkManager.registerMessage(MessageCloak.class               , MessageCloak.class               , 3, Side.CLIENT);
		simpleNetworkManager.registerMessage(MessageSpawnParticle.class       , MessageSpawnParticle.class       , 4, Side.CLIENT);
		simpleNetworkManager.registerMessage(MessageVideoChannel.class        , MessageVideoChannel.class        , 5, Side.CLIENT);
		simpleNetworkManager.registerMessage(MessageTransporterEffect.class   , MessageTransporterEffect.class   , 6, Side.CLIENT);
		
		simpleNetworkManager.registerMessage(MessageTargeting.class           , MessageTargeting.class           , 100, Side.SERVER);
		simpleNetworkManager.registerMessage(MessageClientValidation.class    , MessageClientValidation.class    , 101, Side.SERVER);
		
		// Entity packets for 'uncloaking' entities
		try {
			EntityTrackerEntry_getPacketForThisEntity = Class.forName("net.minecraft.entity.EntityTrackerEntry").getDeclaredMethod("func_151260_c"); 
			EntityTrackerEntry_getPacketForThisEntity.setAccessible(true);
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}
	
	// Beam effect sent to client side
	public static void sendBeamPacket(final World world, final Vector3 v3Source, final Vector3 v3Target,
	                                  final float red, final float green, final float blue,
	                                  final int age, final int energy, final int radius) {
		assert(!world.isRemote);
		
		final MessageBeamEffect messageBeamEffect = new MessageBeamEffect(v3Source, v3Target, red, green, blue, age, energy);
		
		// small beam are sent relative to beam center
		if (v3Source.distanceTo_square(v3Target) < 3600 /* 60 * 60 */) {
			simpleNetworkManager.sendToAllAround(messageBeamEffect, new TargetPoint(
					world.provider.dimensionId, (v3Source.x + v3Target.x) / 2, (v3Source.y + v3Target.y) / 2, (v3Source.z + v3Target.z) / 2, radius));
		} else {// large beam are sent from both ends
			final List<EntityPlayerMP> playerEntityList = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
			final int dimensionId = world.provider.dimensionId;
			final int radius_square = radius * radius;
			for (int index = 0; index < playerEntityList.size(); index++) {
				final EntityPlayerMP entityPlayerMP = playerEntityList.get(index);
				
				if (entityPlayerMP.dimension == dimensionId) {
					if ( v3Source.distanceTo_square(entityPlayerMP) < radius_square
					  || v3Target.distanceTo_square(entityPlayerMP) < radius_square ) {
						simpleNetworkManager.sendTo(messageBeamEffect, entityPlayerMP);
					}
				}
			}
		}
	}
	
	public static void sendBeamPacketToPlayersInArea(final World world, final Vector3 source, final Vector3 target,
	                                                 final float red, final float green, final float blue,
	                                                 final int age, final int energy, final AxisAlignedBB aabb) {
		assert(!world.isRemote);
		
		MessageBeamEffect messageBeamEffect = new MessageBeamEffect(source, target, red, green, blue, age, energy);
		// Send packet to all players within cloaked area
		final List<Entity> list = world.getEntitiesWithinAABB(EntityPlayerMP.class, aabb);
		for (final Entity entity : list) {
			if (entity != null && entity instanceof EntityPlayerMP) {
				PacketHandler.simpleNetworkManager.sendTo(messageBeamEffect, (EntityPlayerMP) entity);
			}
		}
	}
	
	// Forced particle effect sent to client side
	public static void sendSpawnParticlePacket(final World world, final String type, final byte quantity,
	                                           final Vector3 origin, final Vector3 direction,
	                                           final float baseRed, final float baseGreen, final float baseBlue,
	                                           final float fadeRed, final float fadeGreen, final float fadeBlue,
	                                           final int radius) {
		assert(!world.isRemote);
		
		MessageSpawnParticle messageSpawnParticle = new MessageSpawnParticle(
			type, quantity, origin, direction, baseRed, baseGreen, baseBlue, fadeRed, fadeGreen, fadeBlue);
		
		// small beam are sent relative to beam center
		simpleNetworkManager.sendToAllAround(messageSpawnParticle, new TargetPoint(
				world.provider.dimensionId, origin.x, origin.y, origin.z, radius));
		
		if (WarpDriveConfig.LOGGING_EFFECTS) {
			WarpDrive.logger.info(String.format("Sent particle effect '%s' x %d from %s toward %s as RGB %.2f %.2f %.2f fading to %.2f %.2f %.2f",
				type, quantity, origin, direction, baseRed, baseGreen, baseBlue, fadeRed, fadeGreen, fadeBlue));
		}
	}
	
	// Transporter effect sent to client side
	public static void sendTransporterEffectPacket(final World world, final VectorI vSource, final VectorI vDestination, final double lockStrength,
	                                               final Entity entity, final Vector3 v3EntityPosition,
	                                               final int tickEnergizing, final int tickCooldown, final int radius) {
		assert(!world.isRemote);
		
		final MessageTransporterEffect messageTransporterEffect = new MessageTransporterEffect(vSource, vDestination, lockStrength,
		                                                                                       entity, v3EntityPosition,
		                                                                                       tickEnergizing, tickCooldown);
		
		// check both ends to send packet
		final List<EntityPlayerMP> playerEntityList = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		final int dimensionId = world.provider.dimensionId;
		final int radius_square = radius * radius;
		for (int index = 0; index < playerEntityList.size(); index++) {
			final EntityPlayerMP entityPlayerMP = playerEntityList.get(index);
			
			if (entityPlayerMP.dimension == dimensionId) {
				if ( vSource.distance2To(entityPlayerMP) < radius_square
				  || vDestination.distance2To(entityPlayerMP) < radius_square ) {
					simpleNetworkManager.sendTo(messageTransporterEffect, entityPlayerMP);
				}
			}
		}
	}
	
	// Monitor/Laser/Camera updating its video channel to client side
	public static void sendVideoChannelPacket(final int dimensionId, final int xCoord, final int yCoord, final int zCoord, final int videoChannel) {
		MessageVideoChannel messageVideoChannel = new MessageVideoChannel(xCoord, yCoord, zCoord, videoChannel);
		simpleNetworkManager.sendToAllAround(messageVideoChannel, new TargetPoint(dimensionId, xCoord, yCoord, zCoord, 100));
		if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
			WarpDrive.logger.info("Sent video channel packet (" + xCoord + " " + yCoord + " " + zCoord + ") video channel " + videoChannel);
		}
	}
	
	// LaserCamera shooting at target (client -> server)
	public static void sendLaserTargetingPacket(final int x, final int y, final int z, final float yaw, final float pitch) {
		MessageTargeting messageTargeting = new MessageTargeting(x, y, z, yaw, pitch);
		simpleNetworkManager.sendToServer(messageTargeting);
		if (WarpDriveConfig.LOGGING_TARGETING) {
			WarpDrive.logger.info("Sent targeting packet (" + x + " " + y + " " + z + ") yaw " + yaw + " pitch " + pitch);
		}
	}
	
	// Sending cloaking area definition (server -> client)
	public static void sendCloakPacket(final EntityPlayerMP entityPlayerMP, final CloakedArea area, final boolean decloak) {
		final MessageCloak messageCloak = new MessageCloak(area, decloak);
		simpleNetworkManager.sendTo(messageCloak, entityPlayerMP);
		if (WarpDriveConfig.LOGGING_CLOAKING) {
			WarpDrive.logger.info("Sent cloak packet (area " + area + " decloak " + decloak + ")");
		}
	}
	
	public static void sendClientSync(final EntityPlayerMP entityPlayerMP, final CelestialObject celestialObject) {
		if (WarpDriveConfig.LOGGING_CLIENT_SYNCHRONIZATION) {
			WarpDrive.logger.info(String.format("PacketHandler.sendClientSync %s", entityPlayerMP));
		}
		final MessageClientSync messageClientSync = new MessageClientSync(entityPlayerMP, celestialObject);
		simpleNetworkManager.sendTo(messageClientSync, entityPlayerMP);
	}
	
	public static Packet getPacketForThisEntity(final Entity entity) {
		EntityTrackerEntry entry = new EntityTrackerEntry(entity, 0, 0, false);
		try {
			return (Packet) EntityTrackerEntry_getPacketForThisEntity.invoke(entry);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}
}