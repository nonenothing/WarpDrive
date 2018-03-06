package cr0s.warpdrive.network;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
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

public class MessageTransporterEffect implements IMessage, IMessageHandler<MessageTransporterEffect, IMessage> {
	
	private final int ENTITY_ID_NONE = 0;
	
	private VectorI vSource;
	private VectorI vDestination;
	private double lockStrength;
	private int idEntity;
	private Vector3 v3EntityPosition;
	private int tickEnergizing;
	private int tickCooldown;
	
	public MessageTransporterEffect() {
		// required on receiving side
	}
	
	public MessageTransporterEffect(final VectorI vSource, final VectorI vDestination, final double lockStrength,
	                                final Entity entity, final Vector3 v3EntityPosition,
	                                final int tickEnergizing, final int tickCooldown) {
		this.vSource = vSource;
		this.vDestination = vDestination;
		this.lockStrength = lockStrength;
		this.idEntity = entity == null ? ENTITY_ID_NONE : entity.getEntityId();
		this.v3EntityPosition = v3EntityPosition;
		this.tickEnergizing = tickEnergizing;
		this.tickCooldown = tickCooldown;
	}
	
	@Override
	public void fromBytes(final ByteBuf buffer) {
		int x = buffer.readInt();
		int y = buffer.readInt();
		int z = buffer.readInt();
		vSource = new VectorI(x, y, z);
		
		x = buffer.readInt();
		y = buffer.readInt();
		z = buffer.readInt();
		vDestination = new VectorI(x, y, z);
		
		lockStrength = buffer.readFloat();
		
		idEntity = buffer.readInt();
		
		final double xEntity = buffer.readDouble();
		final double yEntity = buffer.readDouble();
		final double zEntity = buffer.readDouble();
		v3EntityPosition = new Vector3(xEntity, yEntity, zEntity);
		
		tickEnergizing = buffer.readShort();
		tickCooldown = buffer.readShort();
	}
	
	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(vSource.x);
		buffer.writeInt(vSource.y);
		buffer.writeInt(vSource.z);
		buffer.writeInt(vDestination.x);
		buffer.writeInt(vDestination.y);
		buffer.writeInt(vDestination.z);
		buffer.writeFloat((float) lockStrength);
		buffer.writeInt(idEntity);
		buffer.writeDouble(v3EntityPosition == null ? 0.0D : v3EntityPosition.x);
		buffer.writeDouble(v3EntityPosition == null ? -1000.0D : v3EntityPosition.y);
		buffer.writeDouble(v3EntityPosition == null ? 0.0D : v3EntityPosition.z);
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
		if (vSource.distance2To(player) <= maxRenderDistance_squared) {
			handleAtSource(world);
		}
		
		// handle target
		if (vDestination.distance2To(player) <= maxRenderDistance_squared) {
			handleAtDestination(world);
		}
	}
	
	private void handleAtSource(final World world) {
		// add flying particles in area of effect
		spawnParticlesInArea(world, vSource, false,
		                     0.4F, 0.7F, 0.9F,
		                     0.10F, 0.15F, 0.10F);
		
		// get actual entity
		final Entity entity = idEntity == ENTITY_ID_NONE ? null : world.getEntityByID(idEntity);
		
		// energizing
		// @TODO cylinder fade in + shower
		if (entity != null) {
			final Vector3 v3Position = new Vector3(entity);
			final Vector3 v3Target = v3Position.clone().translate(ForgeDirection.UP, entity.height);
			EntityFX effect = new EntityFXBeam(world, v3Position, v3Target,
			                          0.6F + 0.1F * world.rand.nextFloat(),
			                          0.6F + 0.15F * world.rand.nextFloat(),
			                          0.8F + 0.10F * world.rand.nextFloat(),
			                          20, 0);
			FMLClientHandler.instance().getClient().effectRenderer.addEffect(effect);
		}
		
		// cooldown
		// @TODO cylinder fade out
	}
	
	private void handleAtDestination(final World world) {
		// add flying particles in area of effect
		spawnParticlesInArea(world, vDestination, true,
		                     0.4F, 0.9F, 0.7F,
		                     0.10F, 0.10F, 0.15F);
		
		// energizing
		// @TODO cylinder fade in + shower
		
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
			WarpDrive.logger.info("Received transporter effect from %s to %s with %.3f lockStrength towards entity with id %d at %s, energizing in %d ticks, cooldown for %d ticks",
			                      messageSpawnParticle.vSource, messageSpawnParticle.vDestination, messageSpawnParticle.lockStrength,
			                      messageSpawnParticle.idEntity, messageSpawnParticle.v3EntityPosition,
			                      messageSpawnParticle.tickEnergizing, messageSpawnParticle.tickCooldown);
		}
		
		messageSpawnParticle.handle(Minecraft.getMinecraft().theWorld);
		
		return null;	// no response
	}
}
