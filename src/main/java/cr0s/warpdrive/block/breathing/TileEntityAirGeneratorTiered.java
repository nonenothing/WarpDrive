package cr0s.warpdrive.block.breathing;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.CelestialObjectManager;
import cr0s.warpdrive.data.StateAir;
import cr0s.warpdrive.event.ChunkHandler;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.Optional;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityAirGeneratorTiered extends TileEntityAbstractEnergy {
	
	// persistent properties
	protected byte tier = -1;
	private boolean isEnabled = true;
	
	// computed properties
	private int maxEnergyStored = 0;
	private int cooldownTicks = 0;
	
	public TileEntityAirGeneratorTiered() {
		super();
		
		peripheralName = "warpdriveAirGenerator";
		addMethods(new String[] {
				"enable"
		});
	}
	
	@Override
	protected void onFirstUpdateTick() {
		super.onFirstUpdateTick();
		final Block block = getBlockType();
		if (block instanceof BlockAirGeneratorTiered) {
			tier = ((BlockAirGeneratorTiered) block).tier;
			maxEnergyStored = WarpDriveConfig.BREATHING_MAX_ENERGY_STORED[tier - 1];
		} else {
			WarpDrive.logger.error("Missing block for " + this + " at " + worldObj + " " + xCoord + " " + yCoord + " " + zCoord);
		}
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if (worldObj.isRemote) {
			return;
		}
		
		if (isInvalid()) {
			return;
		}
		
		// Air generator works only in space & hyperspace
		final int metadata = getBlockMetadata();
		if (CelestialObjectManager.hasAtmosphere(worldObj, xCoord, zCoord)) {
			if ((metadata & 8) != 0) {
				worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, metadata & 7, 2); // set disabled texture
			}
			return;
		}
		
		cooldownTicks++;
		if (cooldownTicks > WarpDriveConfig.BREATHING_AIR_GENERATION_TICKS) {
			final ForgeDirection direction = ForgeDirection.getOrientation(metadata & 7);
			final boolean isActive = releaseAir(direction);
			if (isActive) {
				if ((metadata & 8) == 0) {
					worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, metadata | 8, 2); // set enabled texture
				}
			} else {
				if ((metadata & 8) != 0) {
					worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, metadata & 7, 2); // set disabled texture
				}
			}
			
			cooldownTicks = 0;
		}
	}
	
	private boolean releaseAir(final ForgeDirection direction) {
		final int x = xCoord + direction.offsetX;
		final int y = yCoord + direction.offsetY;
		final int z = zCoord + direction.offsetZ;
		
		// reject cables or signs in front of the fan (it's inconsistent and not really supported)
		if (!worldObj.isAirBlock(x, y, z)) {
			return false;
		}
		
		// get the state object
		// assume it works when chunk isn't loaded
		final StateAir stateAir = ChunkHandler.getStateAir(worldObj, x, y, z);
		if (stateAir == null) {
			return true;
		}
		stateAir.updateBlockCache(worldObj);
		
		// only accept air block (i.e. rejecting the dictionary blacklist)
		if (!stateAir.isAir()) {
			return false;
		}
		
		if (isEnabled) {
			final int energy_cost = !stateAir.isAirSource() ? WarpDriveConfig.BREATHING_ENERGY_PER_NEW_AIR_BLOCK[tier - 1] : WarpDriveConfig.BREATHING_ENERGY_PER_EXISTING_AIR_BLOCK[tier - 1];
			if (energy_consume(energy_cost, true)) {// enough energy
				final short range = (short) (WarpDriveConfig.BREATHING_AIR_GENERATION_RANGE_BLOCKS[tier - 1] - 1);
				stateAir.setAirSource(worldObj, direction, range);
				energy_consume(energy_cost, false);
				return true;
			}
		}
		
		// disabled or low energy => remove air block
		if (stateAir.concentration > 4) {
			stateAir.setConcentration(worldObj, (byte) (stateAir.concentration / 2));
		} else if (stateAir.concentration > 0) {
			stateAir.removeAirSource(worldObj);
		}
		return false;
	}
	
	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		isEnabled = !tagCompound.hasKey("isEnabled") || tagCompound.getBoolean("isEnabled");
	}
	
	@Override
	public void writeToNBT(final NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		tagCompound.setBoolean("isEnabled", isEnabled);
	}
	
	@Override
	public int energy_getMaxStorage() {
		return maxEnergyStored;
	}
	
	@Override
	public boolean energy_canInput(final ForgeDirection from) {
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("%s @ %s (%d %d %d)", 
		                     getClass().getSimpleName(), 
		                     worldObj == null ? "~NULL~" : worldObj.provider.getDimensionName(), 
		                     xCoord, yCoord, zCoord);
	}
	
	public Object[] enable(final Object[] arguments) {
		if (arguments.length == 1 && arguments[0] != null) {
			isEnabled = Commons.toBool(arguments[0]);
		}
		return new Object[] { isEnabled };
	}
	
	// OpenComputer callback methods
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] enable(final Context context, final Arguments arguments) {
			return enable(argumentsOCtoCC(arguments));
	}
	
	// ComputerCraft IPeripheral methods implementation
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(final IComputerAccess computer, final ILuaContext context, final int method, final Object[] arguments) {
		final String methodName = getMethodName(method);
		
		switch (methodName) {
		case "enable": 
			return enable(arguments);		
		}
		
		return super.callMethod(computer, context, method, arguments);
	}
}
