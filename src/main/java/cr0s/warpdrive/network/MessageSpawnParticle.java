package cr0s.warpdrive.network;

import java.nio.charset.StandardCharsets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.init.Items;
import net.minecraft.world.World;
import io.netty.buffer.ByteBuf;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.Vector3;


public class MessageSpawnParticle implements IMessage, IMessageHandler<MessageSpawnParticle, IMessage> {
	
	private String type;
	private Vector3 origin;
	private Vector3 direction;
	private float baseRed;
	private float baseGreen;
	private float baseBlue;
	private float fadeRed;
	private float fadeGreen;
	private float fadeBlue;
	
	public MessageSpawnParticle() {
		// required on receiving side
	}
	
	public MessageSpawnParticle(final String type, final Vector3 origin, final Vector3 direction,
	                            final float baseRed, final float baseGreen, final float baseBlue,
	                            final float fadeRed, final float fadeGreen, final float fadeBlue) {
		this.type = type;
		this.origin = origin;
		this.direction = direction;
		this.baseRed = baseRed;
		this.baseGreen = baseGreen;
		this.baseBlue = baseBlue;
		this.fadeRed = fadeRed;
		this.fadeGreen = fadeGreen;
		this.fadeBlue = fadeBlue;
	}
	
	@Override
	public void fromBytes(ByteBuf buffer) {
		int typeSize = buffer.readByte();
		type = buffer.toString(buffer.readerIndex(), typeSize, StandardCharsets.US_ASCII);
		buffer.skipBytes(typeSize);
		
		double x = buffer.readDouble();
		double y = buffer.readDouble();
		double z = buffer.readDouble();
		origin = new Vector3(x, y, z);
		
		x = buffer.readDouble();
		y = buffer.readDouble();
		z = buffer.readDouble();
		direction = new Vector3(x, y, z);
		
		baseRed = buffer.readFloat();
		baseGreen = buffer.readFloat();
		baseBlue = buffer.readFloat();
		fadeRed = buffer.readFloat();
		fadeGreen = buffer.readFloat();
		fadeBlue = buffer.readFloat();
	}
	
	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeByte(type.length());
		buffer.writeBytes(type.getBytes(StandardCharsets.US_ASCII), 0, type.length());
		buffer.writeDouble(origin.x);
		buffer.writeDouble(origin.y);
		buffer.writeDouble(origin.z);
		buffer.writeDouble(direction.x);
		buffer.writeDouble(direction.y);
		buffer.writeDouble(direction.z);
		buffer.writeFloat(baseRed);
		buffer.writeFloat(baseGreen);
		buffer.writeFloat(baseBlue);
		buffer.writeFloat(fadeRed);
		buffer.writeFloat(fadeGreen);
		buffer.writeFloat(fadeBlue);
	}
	
	private int integerFromRGB(final float red, final float green, final float blue) {
		return (Math.round(red * 255.0F) << 16)
			+  (Math.round(green * 255.0F) << 8)
			+   Math.round(blue * 255.0F);
	}
	
	@SideOnly(Side.CLIENT)
	private void handle(World worldObj) {
		// Directly spawn particle as per RenderGlobal.doSpawnParticle, bypassing range check
		// adjust color as needed
		EntityFX effect;
		double noiseLevel = direction.getMagnitude() * 0.35D;
		for (int i = 0; i < 5; i++) {
			Vector3 directionRandomized = new Vector3(
					direction.x + noiseLevel * (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()),
					direction.y + noiseLevel * (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()),
					direction.z + noiseLevel * (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()));
			switch (type) {
			case "explode":
			default:
				effect = new EntityExplodeFX(worldObj, origin.x, origin.y, origin.z, directionRandomized.x, directionRandomized.y, directionRandomized.z);
				break;
			
			case "fireworksSpark":
				EntityFireworkSparkFX entityFireworkSparkFX = new EntityFireworkSparkFX(worldObj, origin.x, origin.y, origin.z, directionRandomized.x, directionRandomized.y, directionRandomized.z,
					                                                                       FMLClientHandler.instance().getClient().effectRenderer);
				entityFireworkSparkFX.setFadeColour(integerFromRGB(fadeRed, fadeGreen, fadeBlue));
				effect = entityFireworkSparkFX;
				break;
			
			case "flame":
				effect = new EntityFlameFX(worldObj, origin.x, origin.y, origin.z, directionRandomized.x, directionRandomized.y, directionRandomized.z);
				break;
			
			case "snowballpoof":
				effect = new EntityBreakingFX(worldObj, origin.x, origin.y, origin.z, Items.snowball);
				break;
			
			case "snowshovel":
				effect = new EntitySnowShovelFX(worldObj, origin.x, origin.y, origin.z, directionRandomized.x, directionRandomized.y, directionRandomized.z);
				break;
			} 
			if (baseRed >= 0.0F && baseGreen >= 0.0F && baseBlue >= 0.0F) {
				effect.setRBGColorF(baseRed, baseGreen, baseBlue);
			}
			FMLClientHandler.instance().getClient().effectRenderer.addEffect(effect);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MessageSpawnParticle messageSpawnParticle, MessageContext context) {
		// skip in case player just logged in
		if (Minecraft.getMinecraft().theWorld == null) {
			WarpDrive.logger.error("WorldObj is null, ignoring beam packet");
			return null;
		}
		
		if (WarpDriveConfig.LOGGING_EFFECTS) {
			WarpDrive.logger.info("Received particle effect '" + messageSpawnParticle.type + "' from " + messageSpawnParticle.origin + " toward " + messageSpawnParticle.direction
				+ " as RGB " + messageSpawnParticle.baseRed + " " + messageSpawnParticle.baseGreen + " " + messageSpawnParticle.baseBlue
				+ " fading to " + messageSpawnParticle.fadeRed + " " + messageSpawnParticle.fadeGreen + " " + messageSpawnParticle.fadeBlue);
		}
		
		messageSpawnParticle.handle(Minecraft.getMinecraft().theWorld);
		
		return null;	// no response
	}
}
