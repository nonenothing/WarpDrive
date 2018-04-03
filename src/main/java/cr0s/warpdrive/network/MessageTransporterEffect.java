package cr0s.warpdrive.network;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.GlobalPosition;
import cr0s.warpdrive.data.MovingEntity;
import cr0s.warpdrive.data.Vector3;
import cr0s.warpdrive.data.VectorI;
import cr0s.warpdrive.render.EntityFXBeam;
import cr0s.warpdrive.render.EntityFXDot;
import io.netty.buffer.ByteBuf;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.Collection;

public class MessageTransporterEffect implements IMessage, IMessageHandler<MessageTransporterEffect, IMessage> {
	
	private boolean isTransporterRoom;
	private GlobalPosition globalPosition;
	private ArrayList<Integer> idEntities;
	private ArrayList<Vector3> v3EntityPositions;
	private double lockStrength;
	private int tickEnergizing;
	private int tickCooldown;
	
	public MessageTransporterEffect() {
		// required on receiving side
	}
	
	public MessageTransporterEffect(final boolean isTransporterRoom, final GlobalPosition globalPosition,
									final Collection<MovingEntity> movingEntities,
									final double lockStrength, final int tickEnergizing, final int tickCooldown) {
		this.isTransporterRoom = isTransporterRoom;
		this.globalPosition = globalPosition;
		if ( movingEntities == null
		  || movingEntities.isEmpty() ) {
			this.idEntities = null;
			this.v3EntityPositions = null;
		} else {
			idEntities = new ArrayList<>(movingEntities.size());
			v3EntityPositions = new ArrayList<>(movingEntities.size());
			for (final MovingEntity movingEntity : movingEntities) {
				final Entity entity = movingEntity.getEntity();
				if (entity != null) {
					idEntities.add(entity.getEntityId());
					v3EntityPositions.add(movingEntity.v3OriginalPosition);
				}
			}
		}
		this.lockStrength = lockStrength;
		this.tickEnergizing = tickEnergizing;
		this.tickCooldown = tickCooldown;
	}
	
	@Override
	public void fromBytes(final ByteBuf buffer) {
		isTransporterRoom = buffer.readBoolean();
		
		final short dimensionId = buffer.readShort();
		final int x = buffer.readInt();
		final int y = buffer.readInt();
		final int z = buffer.readInt();
		globalPosition = new GlobalPosition(dimensionId, x, y, z);
		
		final int countEntities = buffer.readByte();
		idEntities = new ArrayList<>(countEntities);
		v3EntityPositions = new ArrayList<>(countEntities);
		for (int indexEntity = 0; indexEntity < countEntities; indexEntity++) {
			final int idEntity = buffer.readInt();
			idEntities.add(idEntity);
			
			final double xEntity = buffer.readDouble();
			final double yEntity = buffer.readDouble();
			final double zEntity = buffer.readDouble();
			v3EntityPositions.add(new Vector3(xEntity, yEntity, zEntity));
		}
		
		lockStrength = buffer.readFloat();
		tickEnergizing = buffer.readShort();
		tickCooldown = buffer.readShort();
	}
	
	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeBoolean(isTransporterRoom);
		
		buffer.writeShort(globalPosition.dimensionId);
		buffer.writeInt(globalPosition.x);
		buffer.writeInt(globalPosition.y);
		buffer.writeInt(globalPosition.z);
		
		final int countEntities = idEntities == null ? 0 : idEntities.size();
		buffer.writeByte(countEntities);
		for (int indexEntity = 0; indexEntity < countEntities; indexEntity++) {
			buffer.writeInt(idEntities.get(indexEntity));
			final Vector3 v3EntityPosition = v3EntityPositions.get(indexEntity);
			buffer.writeDouble(v3EntityPosition.x);
			buffer.writeDouble(v3EntityPosition.y);
			buffer.writeDouble(v3EntityPosition.z);
		}
		
		buffer.writeFloat((float) lockStrength);
		buffer.writeShort(tickEnergizing);
		buffer.writeShort(tickCooldown);
	}
	
	@SideOnly(Side.CLIENT)
	private void handle(final World world) {
		// adjust render distance
		final int maxRenderDistance = Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16;
		final int maxRenderDistance_squared = maxRenderDistance * maxRenderDistance;
		
		final EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		
		// handle source
		if (globalPosition.distance2To(player) <= maxRenderDistance_squared) {
			handleAtSource(world);
		}
	}
	
	private void handleAtSource(final World world) {
		// add flying particles in area of effect when in the wild
		if (!isTransporterRoom) {
			spawnParticlesInArea(world, globalPosition.getVectorI(), false,
					0.4F, 0.7F, 0.9F,
					0.10F, 0.15F, 0.10F);
		}
		
		// get actual entity
		for (int indexEntity = 0; indexEntity < idEntities.size(); indexEntity++) {
			final Entity entity = world.getEntityByID(idEntities.get(indexEntity));
			
			// energizing
			// @TODO cylinder fade in + shower
			if (entity != null) {
				final Vector3 v3Position = new Vector3(entity);
				final Vector3 v3Target = v3Position.clone().translate(ForgeDirection.UP, entity.height);
				final EntityFX effect = new EntityFXBeam(world, v3Position, v3Target,
				                          0.6F + 0.1F * world.rand.nextFloat(),
				                          0.6F + 0.15F * world.rand.nextFloat(),
				                          0.8F + 0.10F * world.rand.nextFloat(),
				                          20, 0);
				FMLClientHandler.instance().getClient().effectRenderer.addEffect(effect);
			}
		}
		
		// cooldown
		// @TODO cylinder fade out
	}
	
	private void spawnParticlesInArea(final World world, final VectorI vCenter, final boolean isFalling,
	                                  final float redBase, final float greenBase, final float blueBase,
	                                  final float redFactor, final float greenFactor, final float blueFactor) {
		
		// compute area of effect
		final AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(
				vCenter.x - WarpDriveConfig.TRANSPORTER_ENTITY_GRAB_RADIUS_BLOCKS,
				vCenter.y - 1.0D,
				vCenter.z - WarpDriveConfig.TRANSPORTER_ENTITY_GRAB_RADIUS_BLOCKS,
				vCenter.x + WarpDriveConfig.TRANSPORTER_ENTITY_GRAB_RADIUS_BLOCKS + 1.0D,
				vCenter.y + 2.0D,
				vCenter.z + WarpDriveConfig.TRANSPORTER_ENTITY_GRAB_RADIUS_BLOCKS + 1.0D);
		
		final Vector3 v3Motion = new Vector3(0.0D, 0.0D, 0.0D);
		final Vector3 v3Acceleration = new Vector3(0.0D, isFalling ? -0.001D : 0.001D, 0.0D);
		
		// adjust quantity to lockStrength
		final int quantityInArea = (int) Commons.clamp(1, 5, Math.round(Commons.interpolate(0.2D, 1.0D, 0.8D, 5.0D, lockStrength)));
		for (int count = 0; count < quantityInArea; count++) {
			// start preferably from top or bottom side
			double y;
			if (isFalling) {
				y = aabb.maxY - Math.pow(world.rand.nextDouble(), 3.0D) * (aabb.maxY - aabb.minY);
			} else {
				y = aabb.minY + Math.pow(world.rand.nextDouble(), 3.0D) * (aabb.maxY - aabb.minY);
			}
			final Vector3 v3Position = new Vector3(
					aabb.minX + world.rand.nextDouble() * (aabb.maxX - aabb.minX),
					y,
					aabb.minZ + world.rand.nextDouble() * (aabb.maxZ - aabb.minZ));
			
			// adjust to block presence
			if ( ( isFalling && MathHelper.floor_double(y) == MathHelper.floor_double(aabb.maxY))
			  || (!isFalling && MathHelper.floor_double(y) == MathHelper.floor_double(aabb.minY)) ) {
				final VectorI vPosition = new VectorI(MathHelper.floor_double(v3Position.x),
				                                      MathHelper.floor_double(v3Position.y),
				                                      MathHelper.floor_double(v3Position.z));
				final Block block = vPosition.getBlock(world);
				if ( !block.isAir(world, vPosition.x, vPosition.y, vPosition.z)
				  && block.isOpaqueCube() ) {
					y += isFalling ? -1.0D : 1.0D;
					v3Position.y = y;
				}
			}
			
			// add particle
			final EntityFX effect = new EntityFXDot(world, v3Position,
			                                        v3Motion, v3Acceleration, 0.98D,
			                                        30);
			effect.setRBGColorF(redBase   + redFactor   * world.rand.nextFloat(),
			                    greenBase + greenFactor * world.rand.nextFloat(),
			                    blueBase  + blueFactor  * world.rand.nextFloat() );
			effect.setAlphaF(1.0F);
			FMLClientHandler.instance().getClient().effectRenderer.addEffect(effect);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MessageTransporterEffect messageSpawnParticle, MessageContext context) {
		// skip in case player just logged in
		if (Minecraft.getMinecraft().theWorld == null) {
			WarpDrive.logger.error("WorldObj is null, ignoring particle packet");
			return null;
		}
		
		if (WarpDriveConfig.LOGGING_EFFECTS) {
			WarpDrive.logger.info("Received transporter effect isTransporterRoom %s at %s towards %d entities, with %.3f lockStrength, energizing in %d ticks, cooldown for %d ticks",
			                      messageSpawnParticle.isTransporterRoom, messageSpawnParticle.globalPosition,
			                      messageSpawnParticle.idEntities.size(),
			                      messageSpawnParticle.lockStrength, messageSpawnParticle.tickEnergizing, messageSpawnParticle.tickCooldown);
		}
		
		messageSpawnParticle.handle(Minecraft.getMinecraft().theWorld);
		
		return null;	// no response
	}
}
