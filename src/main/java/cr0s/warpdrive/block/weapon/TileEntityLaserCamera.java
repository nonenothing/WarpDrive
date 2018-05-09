package cr0s.warpdrive.block.weapon;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IVideoChannel;
import cr0s.warpdrive.block.TileEntityLaser;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.EnumCameraType;
import cr0s.warpdrive.network.PacketHandler;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;

public class TileEntityLaserCamera extends TileEntityLaser implements IVideoChannel {
	
	private int videoChannel = -1;
	
	private static final int REGISTRY_UPDATE_INTERVAL_TICKS = 15 * 20;
	private static final int PACKET_SEND_INTERVAL_TICKS = 60 * 20;
	
	private int packetSendTicks = 10;
	private int registryUpdateTicks = 20;
	
	public TileEntityLaserCamera() {
		super();
		
		peripheralName = "warpdriveLaserCamera";
		addMethods(new String[] {
			"videoChannel"
		});
	}
	
	@Override
	public void update() {
		super.update();
		
		// Update video channel on clients (recovery mechanism, no need to go too fast)
		if (!worldObj.isRemote) {
			packetSendTicks--;
			if (packetSendTicks <= 0) {
				packetSendTicks = PACKET_SEND_INTERVAL_TICKS;
				PacketHandler.sendVideoChannelPacket(worldObj.provider.getDimension(), pos, videoChannel);
			}
		} else {
			registryUpdateTicks--;
			if (registryUpdateTicks <= 0) {
				registryUpdateTicks = REGISTRY_UPDATE_INTERVAL_TICKS;
				if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
					WarpDrive.logger.info(this + " Updating registry (" + videoChannel + ")");
				}
				WarpDrive.cameras.updateInRegistry(worldObj, pos, videoChannel, EnumCameraType.LASER_CAMERA);
			}
		}
	}
	
	@Override
	public int getVideoChannel() {
		return videoChannel;
	}
	
	@Override
	public void setVideoChannel(final int parVideoChannel) {
		if (videoChannel != parVideoChannel && (parVideoChannel <= VIDEO_CHANNEL_MAX) && (parVideoChannel > VIDEO_CHANNEL_MIN)) {
			videoChannel = parVideoChannel;
			markDirty();
			if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
				WarpDrive.logger.info(this + " Video channel updated from " + videoChannel + " to " + parVideoChannel);
			}
			// force update through main thread since CC runs on server as 'client'
			packetSendTicks = 0;
			registryUpdateTicks = 0;
		}
	}
	
	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		setVideoChannel(tagCompound.getInteger("cameraFrequency") + tagCompound.getInteger(VIDEO_CHANNEL_TAG));
	}
	
	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
		tagCompound = super.writeToNBT(tagCompound);
		tagCompound.setInteger(VIDEO_CHANNEL_TAG, videoChannel);
		return tagCompound;
	}
	
	@Nonnull
	@Override
	public NBTTagCompound getUpdateTag() {
		final NBTTagCompound tagCompound = new NBTTagCompound();
		// (beam frequency is server side only)
		tagCompound.setInteger(VIDEO_CHANNEL_TAG, videoChannel);
		return tagCompound;
	}
	
	@Override
	public void onDataPacket(final NetworkManager networkManager, final SPacketUpdateTileEntity packet) {
		final NBTTagCompound tagCompound = packet.getNbtCompound();
		// (beam frequency is server side only)
		setVideoChannel(tagCompound.getInteger(VIDEO_CHANNEL_TAG));
	}
	
	@Override
	public void invalidate() {
		WarpDrive.cameras.removeFromRegistry(worldObj, pos);
		super.invalidate();
	}
	
	@Override
	public void onChunkUnload() {
		WarpDrive.cameras.removeFromRegistry(worldObj, pos);
		super.onChunkUnload();
	}
	
	// OpenComputer callback methods
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] videoChannel(Context context, Arguments arguments) {
		if (arguments.count() == 1) {
			setVideoChannel(arguments.checkInteger(0));
		}
		return new Integer[] { videoChannel };
	}
	
	// ComputerCraft IPeripheral methods implementation
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
		final String methodName = getMethodName(method);
		
		if (methodName.equals("videoChannel")) {
			if (arguments.length == 1 && arguments[0] != null) {
				setVideoChannel(Commons.toInt(arguments[0]));
			}
			return new Integer[] { videoChannel };
		}
		
		return super.callMethod(computer, context, method, arguments);
	}
	
	@Override
	public String toString() {
		return String.format("%s Beam \'%d\' Camera \'%d\' @ %s (%d %d %d)",
		                     getClass().getSimpleName(),
		                     beamFrequency,
		                     videoChannel,
		                     worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(),
		                     worldObj == null ? "~NULL~" : worldObj.provider.getSaveFolder(),
		                     pos.getX(), pos.getY(), pos.getZ());
	}
}