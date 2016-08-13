package cr0s.warpdrive.block.forcefield;

import cpw.mods.fml.common.Optional;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IBeamFrequency;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.ForceFieldRegistry;
import cr0s.warpdrive.data.Vector3;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.StatCollector;

public class TileEntityAbstractForceField extends TileEntityAbstractEnergy implements IBeamFrequency {
	// persistent properties
	protected byte tier = -1;
	protected int beamFrequency = -1;
	public boolean isEnabled = true;
	
	// computed properties
	protected Vector3 vRGB;
	protected boolean isConnected = true;
	
	public TileEntityAbstractForceField() {
		super();

		addMethods(new String[]{
			"enable",
			"beamFrequency"
		});
	}
	
	@Override
	protected void onFirstUpdateTick() {
		Block block = getBlockType();
		if (block instanceof BlockAbstractForceField) {
			tier = ((BlockAbstractForceField) block).tier;
		} else {
			WarpDrive.logger.error("Missing block for " + this + " at " + worldObj + " " + xCoord + " " + yCoord + " " + zCoord);
		}
		if (beamFrequency >= 0 && beamFrequency <= IBeamFrequency.BEAM_FREQUENCY_MAX) {
			ForceFieldRegistry.updateInRegistry(this);
		}
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if (worldObj.isRemote) {
			return;
		}
		
		// Frequency is not set
		isConnected = beamFrequency > 0 && beamFrequency <= IBeamFrequency.BEAM_FREQUENCY_MAX;
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		ForceFieldRegistry.removeFromRegistry(this);
	}
	
	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		// reload chunks as needed
		// ForceFieldRegistry.removeFromRegistry(this);
	}
	
	@Override
	public int getBeamFrequency() {
		return beamFrequency;
	}
	
	@Override
	public void setBeamFrequency(final int parBeamFrequency) {
		if (beamFrequency != parBeamFrequency && (parBeamFrequency <= BEAM_FREQUENCY_MAX) && (parBeamFrequency > 0)) {
			if (WarpDriveConfig.LOGGING_VIDEO_CHANNEL) {
				WarpDrive.logger.info(this + " Beam frequency set from " + beamFrequency + " to " + parBeamFrequency);
			}
			if (worldObj != null) {
				ForceFieldRegistry.removeFromRegistry(this);
			}
			beamFrequency = parBeamFrequency;
			vRGB = IBeamFrequency.getBeamColor(beamFrequency);
		}
		markDirty();
		if (worldObj != null) {
			ForceFieldRegistry.updateInRegistry(this);
		}
	}
	
	String getBeamFrequencyStatus() {
		if (beamFrequency == -1) {
			return StatCollector.translateToLocalFormatted("warpdrive.beamFrequency.statusLine.undefined");
		} else if (beamFrequency < 0) {
			return StatCollector.translateToLocalFormatted("warpdrive.beamFrequency.statusLine.invalid", beamFrequency);
		} else {
			return StatCollector.translateToLocalFormatted("warpdrive.beamFrequency.statusLine.valid", beamFrequency);
		}
	}
	
	@Override
	public String getStatus() {
		return super.getStatus()
			+ "\n" + getBeamFrequencyStatus();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		tier = tag.getByte("tier");
		setBeamFrequency(tag.getInteger("beamFrequency"));
		isEnabled = tag.getBoolean("isEnabled");
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setByte("tier", tier);
		tag.setInteger("beamFrequency", beamFrequency);
		tag.setBoolean("isEnabled", isEnabled);
	}
	
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tagCompound = new NBTTagCompound();
		writeToNBT(tagCompound);
		tagCompound.setBoolean("isConnected", isConnected);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tagCompound);
	}
	
	@Override
	public void onDataPacket(NetworkManager networkManager, S35PacketUpdateTileEntity packet) {
		NBTTagCompound tagCompound = packet.func_148857_g();
		readFromNBT(tagCompound);
		isConnected = tagCompound.getBoolean("isConnected");
	}
	
	// OpenComputer callback methods
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] enable(Context context, Arguments arguments) throws Exception {
		return enable(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] beamFrequency(Context context, Arguments arguments) {
		if (arguments.count() == 1) {
			setBeamFrequency(arguments.checkInteger(0));
		}
		return new Integer[] { beamFrequency };
	}
	
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
	
	// ComputerCraft IPeripheral methods implementation
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
		String methodName = getMethodName(method);
		
		try {
			switch (methodName) {
				case "enable":
					return enable(arguments);
				
				case "beamFrequency":
					if (arguments.length == 1) {
						setBeamFrequency(toInt(arguments[0]));
					}
					return new Integer[]{ beamFrequency };
			}
		} catch (Exception exception) {
			return new String[] { exception.getMessage() };
		}
		
		return super.callMethod(computer, context, method, arguments);
	}
	
	@Override
	public String toString() {
		return String.format("%s Beam \'%d\' @ \'%s\' (%d %d %d)", getClass().getSimpleName(),
			beamFrequency, worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(), xCoord, yCoord, zCoord);
	}
}
