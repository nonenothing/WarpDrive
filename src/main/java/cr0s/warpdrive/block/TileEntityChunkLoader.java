package cr0s.warpdrive.block;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.EnumComponentType;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;

import cpw.mods.fml.common.Optional;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityChunkLoader extends TileEntityAbstractChunkLoading {
	
	// persistent properties
	private boolean isEnabled = false;
	private int radiusXneg = 0;
	private int radiusXpos = 0;
	private int radiusZneg = 0;
	private int radiusZpos = 0;
	
	// fuel status is needed before first tick
	private boolean isPowered = false;
	
	// computed properties
	// (none)
	
	public TileEntityChunkLoader() {
		super();
		
		IC2_sinkTier = 2;
		IC2_sourceTier = 2;
		
		peripheralName = "warpdriveChunkLoader";
		addMethods(new String[] {
				"enable",
				"bounds",
				"radius",				
				"upgrades",
				"getEnergyRequired"
		});
		
		setUpgradeMaxCount(EnumComponentType.SUPERCONDUCTOR, 5);
		setUpgradeMaxCount(EnumComponentType.EMERALD_CRYSTAL, WarpDriveConfig.CHUNK_LOADER_MAX_RADIUS);
	}
	
	@Override
	public int energy_getMaxStorage() {
		return WarpDriveConfig.CHUNK_LOADER_MAX_ENERGY_STORED;
	}
	
	@Override
	public boolean energy_canInput(final ForgeDirection from) {
		return true;
	}
	
	@Override
	public boolean dismountUpgrade(final Object upgrade) {
		final boolean isSuccess = super.dismountUpgrade(upgrade);
		if (isSuccess) {
			final int maxRange = getMaxRange();
			setBounds(maxRange, maxRange, maxRange, maxRange);
		}
		return isSuccess;
	}
	
	@Override
	public boolean mountUpgrade(final Object upgrade) {
		final boolean isSuccess = super.mountUpgrade(upgrade);
		if (isSuccess) {
			final int maxRange = getMaxRange();
			setBounds(maxRange, maxRange, maxRange, maxRange);
		}
		return isSuccess;
	}
	
	private int getMaxRange() {
		return getValidUpgradeCount(EnumComponentType.EMERALD_CRYSTAL);
	}
	
	private double getEnergyFactor() {
		final int upgradeCount = getValidUpgradeCount(EnumComponentType.SUPERCONDUCTOR);
		return 1.0D - 0.1D * upgradeCount;
	}
	
	public long chunkloading_getEnergyRequired() {
		return (long) Math.ceil(getEnergyFactor() * chunkloading_getArea() * WarpDriveConfig.CHUNK_LOADER_ENERGY_PER_CHUNK);
	}
	
	@Override
	public boolean shouldChunkLoad() {
		return isEnabled && isPowered;
	}
	
	@Override
	protected void onFirstUpdateTick() {
		super.onFirstUpdateTick();
		
		if (worldObj.isRemote) {
			return;
		}
		
		refreshChunkRange();
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if (worldObj.isRemote) {
			return;
		}
		
		isPowered = energy_consume(chunkloading_getEnergyRequired(), !isEnabled);
		
		updateMetadata(isEnabled ? isPowered ? 2 : 1 : 0);
	}
	
	private void setBounds(final int negX, final int posX, final int negZ, final int posZ) {
		// compute new values
		final int maxRange = getMaxRange();
		final int radiusXneg_new = - Commons.clamp(0, 1000, Math.abs(negX));
		final int radiusXpos_new =   Commons.clamp(0, 1000, Math.abs(posX));
		final int radiusZneg_new = - Commons.clamp(0, 1000, Math.abs(negZ));
		final int radiusZpos_new =   Commons.clamp(0, 1000, Math.abs(posZ));
		
		// validate size constrains
		final int maxArea = (1 + 2 * maxRange) * (1 + 2 * maxRange);
		final int newArea = (-radiusXneg_new + 1 + radiusXpos_new)
		                  * (-radiusZneg_new + 1 + radiusZpos_new);
		if (newArea <= maxArea) {
			radiusXneg = radiusXneg_new;
			radiusXpos = radiusXpos_new;
			radiusZneg = radiusZneg_new;
			radiusZpos = radiusZpos_new;
			refreshChunkRange();
		}
	}
	
	private void refreshChunkRange() {
		if (worldObj == null) {
			return;
		}
		final ChunkCoordIntPair chunkSelf = worldObj.getChunkFromBlockCoords(xCoord, zCoord).getChunkCoordIntPair();
		
		chunkMin = new ChunkCoordIntPair(chunkSelf.chunkXPos + radiusXneg, chunkSelf.chunkZPos + radiusZneg);
		chunkMax = new ChunkCoordIntPair(chunkSelf.chunkXPos + radiusXpos, chunkSelf.chunkZPos + radiusZpos);
		refreshChunkLoading();
	}
	
	// Forge overrides
	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		isEnabled = !tagCompound.hasKey("isEnabled") || tagCompound.getBoolean("isEnabled");
		setBounds(tagCompound.getInteger("radiusXneg"), tagCompound.getInteger("radiusXpos"), tagCompound.getInteger("radiusZneg"), tagCompound.getInteger("radiusZpos"));
		isPowered = tagCompound.getBoolean("isPowered");
	}
	
	@Override
	public void writeToNBT(final NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		tagCompound.setBoolean("isEnabled", isEnabled);
		tagCompound.setInteger("radiusXneg", radiusXneg);
		tagCompound.setInteger("radiusZneg", radiusZneg);
		tagCompound.setInteger("radiusXpos", radiusXpos);
		tagCompound.setInteger("radiusZpos", radiusZpos);
		tagCompound.setBoolean("isPowered", isPowered);
	}
	
	// Common OC/CC methods
	public Object[] enable(final Object[] arguments) {
		if (arguments.length == 1 && arguments[0] != null) {
			isEnabled = Commons.toBool(arguments[0]);
		}
		return new Object[] { isEnabled };
	}
	
	public Object[] bounds(final Object[] arguments) {
		if (arguments.length == 4) {
			setBounds(Commons.toInt(arguments[0]), Commons.toInt(arguments[1]), Commons.toInt(arguments[2]), Commons.toInt(arguments[3]));
		}
		return new Object[] { radiusXneg, radiusXpos, radiusZneg, radiusZpos };
	}
	
	public Object[] radius(final Object[] arguments) {
		if (arguments.length == 1 && arguments[0] != null) {
			final int radius = Commons.toInt(arguments[0]);
			setBounds(radius, radius, radius, radius);
		}
		return new Object[] { radiusXneg, radiusXpos, radiusZneg, radiusZpos };
	}
	
	public Object[] upgrades() {
		return new Object[] { getUpgradesAsString() };
	}
	
	public Object[] getEnergyRequired() {
		return new Object[] { chunkloading_getEnergyRequired() };
	}
	
	// OpenComputer callback methods
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] enable(final Context context, final Arguments arguments) {
		return enable(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] bounds(final Context context, final Arguments arguments) {
		return bounds(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] radius(final Context context, final Arguments arguments) {
		return radius(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] upgrades(final Context context, final Arguments arguments) {
		return upgrades();
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getEnergyRequired(final Context context, final Arguments arguments) {
		return getEnergyRequired();
	}
	
	// ComputerCraft IPeripheral methods
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(final IComputerAccess computer, final ILuaContext context, final int method, final Object[] arguments) {
		final String methodName = getMethodName(method);
		
		switch (methodName) {
		case "radius":
			return radius(arguments);
			
		case "bounds":
			return bounds(arguments);
			
		case "enable":
			return enable(arguments);
			
		case "upgrades":
			return upgrades();
			
		case "getEnergyRequired":
			return getEnergyRequired();
		}
		
		return super.callMethod(computer, context, method, arguments);
	}
	
	@Override
	public String toString() {
		return String.format(
			"%s @ %s (%d %d %d)",
			getClass().getSimpleName(),
			worldObj == null ? "~NULL~" : worldObj.provider.getDimensionName(),
			xCoord, yCoord, zCoord);
	}
}
