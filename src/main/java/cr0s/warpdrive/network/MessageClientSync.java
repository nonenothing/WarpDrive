package cr0s.warpdrive.network;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.data.CelestialObjectManager;
import cr0s.warpdrive.config.WarpDriveConfig;
import io.netty.buffer.ByteBuf;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MessageClientSync implements IMessage, IMessageHandler<MessageClientSync, IMessage> {
	
	private NBTTagCompound nbtTagCompound;
	
	public MessageClientSync() {
		// required on receiving side
	}
	
	public MessageClientSync(final NBTTagCompound nbtTagCompound) {
		this.nbtTagCompound = nbtTagCompound;
	}
	
	@Override
	public void fromBytes(ByteBuf buffer) {
		nbtTagCompound = ByteBufUtils.readTag(buffer);
	}
	
	@Override
	public void toBytes(ByteBuf buffer) {
		ByteBufUtils.writeTag(buffer, nbtTagCompound);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IMessage onMessage(MessageClientSync messageClientSync, MessageContext context) {
		// skip in case player just logged in
		if (Minecraft.getMinecraft().theWorld == null) {
			WarpDrive.logger.error("WorldObj is null, ignoring video channel packet");
			return null;
		}
		
		if (WarpDriveConfig.LOGGING_CLIENT_SYNCHRONIZATION) {
			WarpDrive.logger.info(String.format("Received client synchronization packet: %s",
			                                    messageClientSync.nbtTagCompound));
		}
		
		CelestialObjectManager.onClientSync(messageClientSync.nbtTagCompound);
		
		return null;	// no response
	}
}
