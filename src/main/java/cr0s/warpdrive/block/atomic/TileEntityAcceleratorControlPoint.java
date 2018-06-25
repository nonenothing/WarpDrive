package cr0s.warpdrive.block.atomic;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IControlChannel;
import cr0s.warpdrive.block.TileEntityAbstractInterfaced;
import cr0s.warpdrive.config.WarpDriveConfig;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;

public class TileEntityAcceleratorControlPoint extends TileEntityAbstractInterfaced implements IControlChannel {
	
	// persistent properties
	private boolean isEnabled = true;
	private int controlChannel = -1;
	
	// computed properties
	private static final int UPDATE_INTERVAL_TICKS = 20;
	private int updateTicks;
	
	public TileEntityAcceleratorControlPoint() {
		super();
		
		peripheralName = "warpdriveAcceleratorControlPoint";
		addMethods(new String[] {
			"enable",
			"state",
			"controlChannel"
		});
	}
	
	@Override
	protected void onFirstUpdateTick() {
		super.onFirstUpdateTick();
	}
	
	@Override
	public void update() {
		super.update();
		
		if (world.isRemote) {
			return;
		}
		
		updateTicks--;
		if (updateTicks <= 0) {
			updateTicks = UPDATE_INTERVAL_TICKS;
			updateMetadata((controlChannel == -1) || !isEnabled ? 0 : 1); // @TODO MC1.10
		}
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
	}
	
	@Override
	public int getControlChannel() {
		return controlChannel;
	}
	
	@Override
	public void setControlChannel(final int controlChannel) {
		if (this.controlChannel != controlChannel) {
			this.controlChannel = controlChannel;
			if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
				WarpDrive.logger.info(this + " Accelerator control point controlChannel channel set to " + controlChannel);
			}
			// force update through main thread since CC runs on server as 'client'
			markDirty();
		}
	}
	
	private ITextComponent getControlChannelStatus() {
		if (controlChannel == -1) {
			return new TextComponentTranslation("warpdrive.control_channel.status_line.undefined");
		} else if (controlChannel < CONTROL_CHANNEL_MIN || controlChannel > CONTROL_CHANNEL_MAX) {
			return new TextComponentTranslation("warpdrive.control_channel.status_line.invalid", controlChannel);
		} else {
			return new TextComponentTranslation("warpdrive.control_channel.status_line.valid", controlChannel);
		}
	}
	
	@Override
	public ITextComponent getStatus() {
		return super.getStatus()
		       .appendSibling(getControlChannelStatus());
	}
	
	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		isEnabled = !tagCompound.hasKey("isEnabled") || tagCompound.getBoolean("isEnabled");
		controlChannel = tagCompound.getInteger(CONTROL_CHANNEL_TAG);
	}
	
	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
		tagCompound = super.writeToNBT(tagCompound);
		tagCompound.setBoolean("isEnabled", isEnabled);
		tagCompound.setInteger(CONTROL_CHANNEL_TAG, controlChannel);
		return tagCompound;
	}
	
	@Nonnull
	@Override
	public NBTTagCompound getUpdateTag() {
		final NBTTagCompound tagCompound = new NBTTagCompound();
		writeToNBT(tagCompound);
		return tagCompound;
	}
	
	@Override
	public void onDataPacket(final NetworkManager networkManager, final SPacketUpdateTileEntity packet) {
		final NBTTagCompound tagCompound = packet.getNbtCompound();
		readFromNBT(tagCompound);
	}
	
	public boolean getIsEnabled() {
		return isEnabled;
	}
	
	public void setIsEnabled(final boolean isEnabled) {
		this.isEnabled = isEnabled;
		WarpDrive.starMap.onBlockUpdated(world, pos, world.getBlockState(pos));
	}
	
	// OpenComputer callback methods
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] enable(final Context context, final Arguments arguments) {
		return enable(OC_convertArgumentsAndLogCall(context, arguments));
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] controlChannel(final Context context, final Arguments arguments) {
		if (arguments.count() == 1) {
			setControlChannel(arguments.checkInteger(0));
		}
		return new Integer[] { controlChannel };
	}
	
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] state(final Context context, final Arguments arguments) {
		return state();
	}
	
	// Common OC/CC methods
	public Object[] enable(final Object[] arguments) {
		if (arguments.length == 1 && arguments[0] != null) {
			final boolean enable;
			try {
				enable = Commons.toBool(arguments[0]);
			} catch (final Exception exception) {
				if (WarpDriveConfig.LOGGING_LUA) {
					WarpDrive.logger.error(this + " LUA error on enable(): Boolean expected for 1st argument " + arguments[0]);
				}
				return new Object[] { isEnabled };
			}
			setIsEnabled(enable);
		}
		return new Object[] { isEnabled };
	}
	
	private Object[] state() {    // isConnected, isPowered, shape
		final String status = getStatusHeaderInPureText();
		return new Object[] { status, isEnabled, controlChannel };
	}
	
	// ComputerCraft IPeripheral methods implementation
	@Override
	@Optional.Method(modid = "computercraft")
	public Object[] callMethod(@Nonnull final IComputerAccess computer, @Nonnull final ILuaContext context, final int method, @Nonnull final Object[] arguments) {
		final String methodName = CC_getMethodNameAndLogCall(method, arguments);
		
		try {
			switch (methodName) {
			case "enable":
				return enable(arguments);
			
			case "controlChannel":
				if (arguments.length == 1 && arguments[0] != null) {
					setControlChannel(Commons.toInt(arguments[0]));
				}
				return new Integer[] { controlChannel };
				
			case "state":
				return state();
			}
		} catch (final Exception exception) {
			exception.printStackTrace();
			return new String[] { exception.getMessage() };
		}
		
		return super.callMethod(computer, context, method, arguments);
	}
}
