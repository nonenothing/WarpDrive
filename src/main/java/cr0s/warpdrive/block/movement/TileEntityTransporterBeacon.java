package cr0s.warpdrive.block.movement;

import cpw.mods.fml.common.Optional;
import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.computer.ITransporterBeacon;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.StarMapRegistryItem;
import cr0s.warpdrive.data.StarMapRegistryItem.EnumStarMapEntryType;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.UUID;

public class TileEntityTransporterBeacon extends TileEntityAbstractEnergy implements ITransporterBeacon {
	
	private static final int TICK_DEPLOYING = 20;
	private static final int TICK_LOW_POWER = 20 * 5;
	
	// persistent properties
	private boolean isEnabled = true;
	private String nameTransporterCore;
	private UUID uuidTransporterCore;
	private int tickDeploying = 0;
	
	// computer properties
	private boolean isActive = false;
	
	public TileEntityTransporterBeacon() {
		super();
		
		IC2_sinkTier = 2;
		isEnergyLostWhenBroken = false;
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
		
		if (!isEnabled) {
			isActive = false;
			return;
		}
		
		// deploy
		final boolean isDeployed = tickDeploying > TICK_DEPLOYING;
		if (!isDeployed) {
			tickDeploying++;
		}
		
		// get current status
		final boolean isConnected = uuidTransporterCore != null && (uuidTransporterCore.getLeastSignificantBits() != 0 || uuidTransporterCore.getMostSignificantBits() != 0);
		final boolean isPowered = energy_consume(WarpDriveConfig.TRANSPORTER_BEACON_ENERGY_PER_TICK, true);
		final boolean isLowPower = energy_getEnergyStored() < WarpDriveConfig.TRANSPORTER_BEACON_ENERGY_PER_TICK * TICK_LOW_POWER;
		
		// reach transporter
		boolean isActiveNew = false;
		if (isPowered) {
			if (isConnected) {// only consume is transporter is reachable
				isActiveNew = pingTransporter();
				if (isActiveNew) {
					energy_consume(WarpDriveConfig.TRANSPORTER_BEACON_ENERGY_PER_TICK, false);
				}
				
			} else {// always consume
				energy_consume(WarpDriveConfig.TRANSPORTER_BEACON_ENERGY_PER_TICK, false);
			}
		}
		isActive = isActiveNew;
		
		// report updated status
		final int metadataNew = 0; // @TODO block rendering !isDeployed || isLowPower ? 1 : !isConnected ? 0 : 2;
		updateMetadata(metadataNew);
	}
	
	private boolean pingTransporter() {
		final StarMapRegistryItem starMapRegistryItem = WarpDrive.starMap.getByUUID(EnumStarMapEntryType.TRANSPORTER, uuidTransporterCore);
		if (starMapRegistryItem == null) {
			return false;
		}
		
		final WorldServer worldTransporter = Commons.getOrCreateWorldServer(starMapRegistryItem.dimensionId);
		if (worldTransporter == null) {
			WarpDrive.logger.error(String.format("%s Unable to load dimension %d for transporter with UUID %s",
			                                     this, starMapRegistryItem.dimensionId, uuidTransporterCore));
			return false;
		}
		
		final TileEntity tileEntity = worldTransporter.getTileEntity(starMapRegistryItem.x, starMapRegistryItem.y, starMapRegistryItem.z);
		if (!(tileEntity instanceof TileEntityTransporterCore)) {
			WarpDrive.logger.warn(String.format("%s Transporter has gone missing for %s, found %s",
			                                    this, starMapRegistryItem, tileEntity));
			return false;
		}
		
		final TileEntityTransporterCore tileEntityTransporterCore = (TileEntityTransporterCore) tileEntity;
		return tileEntityTransporterCore.updateBeacon(this, uuidTransporterCore);
	}
	
	// Common OC/CC methods
	@Override
	public Boolean[] enable(final Object[] arguments) {
		if (arguments.length == 1) {
			isEnabled = Commons.toBool(arguments[0]);
			markDirty();
		}
		return new Boolean[] { isEnabled };
	}
	
	@Override
	public Boolean[] isActive(final Object[] arguments) {
		return new Boolean[] { isActive };
	}
	
	@Override
	public boolean isActive() {
		return isActive;
	}
	
	@Override
	public void energizeDone() {
		isEnabled = false;
	}
	
	// OpenComputers callback methods
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] enable(Context context, Arguments arguments) {
		return enable(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] isActive(Context context, Arguments arguments) {
		return isActive(argumentsOCtoCC(arguments));
	}
	
	// ComputerCraft IPeripheral methods
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
		final String methodName = getMethodName(method);
		
		switch (methodName) {
		case "enable":
			return enable(arguments);
		
		case "isActive":
			return isActive(arguments);
		}
		
		return super.callMethod(computer, context, method, arguments);
	}
	
	// TileEntityAbstractBase overrides
	private String getSignatureStatus() {
		if (uuidTransporterCore == null) {
			return "No transporter room linked";
		}
		return StatCollector.translateToLocalFormatted("Linked to transporter room %s",
		                                               nameTransporterCore)
				+ "\n" + "§8" + uuidTransporterCore;
	}
	
	@Override
	public String getStatus() {
		final String strSignatureStatus = getSignatureStatus();
		return super.getStatus()
		       + (strSignatureStatus.isEmpty() ? "" : "\n" + strSignatureStatus);
	}
	
	// TileEntityAbstractEnergy overrides
	@Override
	public int energy_getMaxStorage() {
		return WarpDriveConfig.TRANSPORTER_BEACON_MAX_ENERGY_STORED;
	}
	
	@Override
	public boolean energy_canInput(ForgeDirection from) {
		// only from bottom
		if (from != ForgeDirection.DOWN) {
			return false;
		}
		
		// only when offline
		final int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		return (metadata == 0);
	}
	
	@Override
	public boolean energy_canOutput(ForgeDirection to) {
		return false;
	}
	
	// Forge overrides
	@Override
	public void writeToNBT(final NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		
		if (uuidTransporterCore != null) {
			tagCompound.setString("name", nameTransporterCore);
			tagCompound.setLong("uuidMost", uuidTransporterCore.getMostSignificantBits());
			tagCompound.setLong("uuidLeast", uuidTransporterCore.getLeastSignificantBits());
		}
		
		tagCompound.setInteger("tickDeploying", tickDeploying);
	}
	
	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		
		nameTransporterCore = tagCompound.getString("name");
		uuidTransporterCore = new UUID(tagCompound.getLong("uuidMost"), tagCompound.getLong("uuidLeast"));
		if (uuidTransporterCore.getMostSignificantBits() == 0 && uuidTransporterCore.getLeastSignificantBits() == 0) {
			uuidTransporterCore = null;
			nameTransporterCore = "";
		}
		
		tickDeploying = tagCompound.getInteger("tickDeploying");
	}
	
	@Override
	public NBTTagCompound writeItemDropNBT(NBTTagCompound tagCompound) {
		tagCompound = super.writeItemDropNBT(tagCompound);
		tagCompound.removeTag("tickDeploying");
		return tagCompound;
	}
	
	@Override
	public Packet getDescriptionPacket() {
		final NBTTagCompound tagCompound = new NBTTagCompound();
		writeToNBT(tagCompound);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tagCompound);
	}
	
	@Override
	public void onDataPacket(final NetworkManager networkManager, final S35PacketUpdateTileEntity packet) {
		final NBTTagCompound tagCompound = packet.func_148857_g();
		readFromNBT(tagCompound);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	@Override
	public String toString() {
		return String.format("%s @ %s (%d %d %d) %8d EU",
		                     getClass().getSimpleName(),
		                     worldObj == null ? "~NULL~" : worldObj.provider.getDimensionName(),
		                     xCoord, yCoord, zCoord,
		                     energy_getEnergyStored());
	}
}