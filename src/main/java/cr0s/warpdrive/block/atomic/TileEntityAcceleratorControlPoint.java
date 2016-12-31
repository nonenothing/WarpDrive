package cr0s.warpdrive.block.atomic;

import cpw.mods.fml.common.Optional;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IVideoChannel;
import cr0s.warpdrive.block.TileEntityAbstractInterfaced;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.*;
import cr0s.warpdrive.network.PacketHandler;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.StatCollector;

public class TileEntityAcceleratorControlPoint extends TileEntityAbstractInterfaced implements IVideoChannel {
	
	// persistent properties
	public boolean isEnabled = true;
	private int videoChannel = -1;
	
	// computed properties
	private final static int PACKET_SEND_INTERVAL_TICKS = 60 * 20;
	private int packetSendTicks = 10;
	
	public TileEntityAcceleratorControlPoint() {
		super();
				
		peripheralName = "warpdriveAcceleratorControlPoint";
		addMethods(new String[] {
			"enable",
			"state",
			"videoChannel"
		});
	}
	
	@Override
	protected void onFirstUpdateTick() {
		super.onFirstUpdateTick();
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if (worldObj.isRemote) {
			return;
		}
		
		packetSendTicks--;
		if (packetSendTicks <= 0) {
			packetSendTicks = PACKET_SEND_INTERVAL_TICKS;
			PacketHandler.sendVideoChannelPacket(worldObj.provider.dimensionId, xCoord, yCoord, zCoord, videoChannel);
		}
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		// @TODO
	}
	
	@Override
	public int getVideoChannel() {
		return videoChannel;
	}
	
	@Override
	public void setVideoChannel(int parVideoChannel) {
		if (videoChannel != parVideoChannel) {
			videoChannel = parVideoChannel;
			if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
				WarpDrive.logger.info(this + " Monitor video channel set to " + videoChannel);
			}
			// force update through main thread since CC runs on server as 'client'
			packetSendTicks = 0;
			markDirty();
		}
	}
	
	private String getVideoChannelStatus() {
		if (videoChannel == -1) {
			return StatCollector.translateToLocalFormatted("warpdrive.videoChannel.statusLine.undefined");
		} else if (videoChannel < 0) {
			return StatCollector.translateToLocalFormatted("warpdrive.videoChannel.statusLine.invalid", videoChannel);
		} else {
			CameraRegistryItem camera = WarpDrive.cameras.getCameraByVideoChannel(worldObj, videoChannel);
			if (camera == null) {
				return StatCollector.translateToLocalFormatted("warpdrive.videoChannel.statusLine.invalidOrNotLoaded", videoChannel);
			} else if (camera.isTileEntity(this)) {
				return StatCollector.translateToLocalFormatted("warpdrive.videoChannel.statusLine.valid", videoChannel);
			} else {
				return StatCollector.translateToLocalFormatted("warpdrive.videoChannel.statusLine.validCamera",
				videoChannel,
				camera.position.chunkPosX,
				camera.position.chunkPosY,
				camera.position.chunkPosZ);
			}
		}
	}
	
	@Override
	public String getStatus() {
		return super.getStatus()
		       + getVideoChannelStatus();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		isEnabled = tag.getBoolean("isEnabled");
		videoChannel = tag.getInteger("frequency") + tag.getInteger("videoChannel");
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setBoolean("isEnabled", isEnabled);
		tag.setInteger("videoChannel", videoChannel);
	}
	
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tagCompound = new NBTTagCompound();
		writeToNBT(tagCompound);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tagCompound);
	}
	
	@Override
	public void onDataPacket(NetworkManager networkManager, S35PacketUpdateTileEntity packet) {
		NBTTagCompound tagCompound = packet.func_148857_g();
		readFromNBT(tagCompound);
	}
	
	// OpenComputer callback methods
	@Callback
	@cpw.mods.fml.common.Optional.Method(modid = "OpenComputers")
	public Object[] enable(Context context, Arguments arguments) throws Exception {
		return enable(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] videoChannel(Context context, Arguments arguments) {
		if (arguments.count() == 1) {
			setVideoChannel(arguments.checkInteger(0));
		}
		return new Integer[] { videoChannel };
	}
	
	@Callback
	@cpw.mods.fml.common.Optional.Method(modid = "OpenComputers")
	public Object[] state(Context context, Arguments arguments) {
		return state();
	}
	
	// Common OC/CC methods
	public Object[] enable(Object[] arguments) throws Exception {
		if (arguments.length == 1) {
			boolean enable;
			try {
				enable = toBool(arguments[0]);
			} catch (Exception exception) {
				throw new Exception("Function expects a boolean value");
			}
			isEnabled = enable;
		}
		return new Object[] { isEnabled };
	}
	
	private Object[] state() {    // isConnected, isPowered, shape
		String status = getStatus();
		return new Object[] { status, isEnabled, videoChannel };
	}
	
	// ComputerCraft IPeripheral methods implementation
	@Override
	@cpw.mods.fml.common.Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
		String methodName = getMethodName(method);
		
		try {
			switch (methodName) {
			case "enable":
				return enable(arguments);
			
			case "videoChannel":
				if (arguments.length == 1) {
					setVideoChannel(toInt(arguments[0]));
				}
				return new Integer[] { videoChannel };
				
			case "state":
				return state();
			}
		} catch (Exception exception) {
			return new String[] { exception.getMessage() };
		}
		
		return super.callMethod(computer, context, method, arguments);
	}
}
