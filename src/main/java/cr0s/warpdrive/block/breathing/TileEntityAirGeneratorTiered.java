package cr0s.warpdrive.block.breathing;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.BlockProperties;
import cr0s.warpdrive.data.CelestialObjectManager;
import cr0s.warpdrive.data.StateAir;
import cr0s.warpdrive.event.ChunkHandler;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.common.Optional;

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
			WarpDrive.logger.error("Missing block for " + this + " at " + world + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ());
		}
	}
	
	@Override
	public void update() {
		super.update();
		
		if (world.isRemote) {
			return;
		}
		
		if (isInvalid()) {
			return;
		}
		
		// Air generator works only in space & hyperspace
		final IBlockState blockState = world.getBlockState(pos);
		if (CelestialObjectManager.hasAtmosphere(world, pos.getX(), pos.getZ())) {
			if (blockState.getValue(BlockProperties.ACTIVE)) {
				world.setBlockState(pos, blockState.withProperty(BlockProperties.ACTIVE, false)); // set disabled texture
			}
			return;
		}
		
		cooldownTicks++;
		if (cooldownTicks > WarpDriveConfig.BREATHING_AIR_GENERATION_TICKS) {
			final boolean isActive = releaseAir(blockState.getValue(BlockProperties.FACING));
			if (isActive) {
				if (!blockState.getValue(BlockProperties.ACTIVE)) {
					world.setBlockState(pos, blockState.withProperty(BlockProperties.ACTIVE, true)); // set enabled texture
				}
			} else {
				if (blockState.getValue(BlockProperties.ACTIVE)) {
					world.setBlockState(pos, blockState.withProperty(BlockProperties.ACTIVE, false)); // set disabled texture
				}
			}
			releaseAir(blockState.getValue(BlockProperties.FACING));
			
			cooldownTicks = 0;
		}
	}
	
	private boolean releaseAir(final EnumFacing direction) {
		final BlockPos posDirection = pos.offset(direction);
		
		// reject cables or signs in front of the fan (it's inconsistent and not really supported)
		if (!world.isAirBlock(posDirection)) {
			return false;
		}
		
		// get the state object
		// assume it works when chunk isn't loaded
		final StateAir stateAir = ChunkHandler.getStateAir(world, posDirection.getX(), posDirection.getY(), posDirection.getZ());
		if (stateAir == null) {
			return true;
		}
		stateAir.updateBlockCache(world);
		
		// only accept air block (i.e. rejecting the dictionary blacklist)
		if (!stateAir.isAir()) {
			return false;
		}
		
		if (isEnabled) {
			final int energy_cost = !stateAir.isAirSource() ? WarpDriveConfig.BREATHING_ENERGY_PER_NEW_AIR_BLOCK[tier - 1] : WarpDriveConfig.BREATHING_ENERGY_PER_EXISTING_AIR_BLOCK[tier - 1];
			if (energy_consume(energy_cost, true)) {// enough energy
				final short range = (short) (WarpDriveConfig.BREATHING_AIR_GENERATION_RANGE_BLOCKS[tier - 1] - 1);
				stateAir.setAirSource(world, direction, range);
				energy_consume(energy_cost, false);
				return true;
			}
		}
		
		// disabled or low energy => remove air block
		if (stateAir.concentration > 4) {
			stateAir.setConcentration(world, (byte) (stateAir.concentration / 2));
		} else if (stateAir.concentration > 0) {
			stateAir.removeAirSource(world);
		}
		return false;
	}
	
	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		isEnabled = !tagCompound.hasKey("isEnabled") || tagCompound.getBoolean("isEnabled");
	}
	
	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
		tagCompound = super.writeToNBT(tagCompound);
		tagCompound.setBoolean("isEnabled", isEnabled);
		return tagCompound;
	}
	
	@Override
	public int energy_getMaxStorage() {
		return maxEnergyStored;
	}
	
	@Override
	public boolean energy_canInput(final EnumFacing from) {
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("%s @ %s (%d %d %d)",
		                     getClass().getSimpleName(),
		                     world == null ? "~NULL~" : world.getWorldInfo().getWorldName(),
		                     pos.getX(), pos.getY(), pos.getZ());
	}
	
	public Object[] enable(final Object[] arguments) {
		if (arguments.length == 1 && arguments[0] != null) {
			isEnabled = Commons.toBool(arguments[0]);
		}
		return new Object[] { isEnabled };
	}
	
	// OpenComputer callback methods
	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] enable(final Context context, final Arguments arguments) {
			return enable(argumentsOCtoCC(arguments));
	}
	
	// ComputerCraft IPeripheral methods implementation
	@Override
	@Optional.Method(modid = "computercraft")
	public Object[] callMethod(final IComputerAccess computer, final ILuaContext context, final int method, final Object[] arguments) {
		final String methodName = getMethodName(method);
		
		switch (methodName) {
		case "enable": 
			return enable(arguments);		
		}
		
		return super.callMethod(computer, context, method, arguments);
	}
}
