package cr0s.warpdrive.block.breathing;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.BlockProperties;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.common.Optional;

public class TileEntityAirGenerator extends TileEntityAbstractEnergy {
	
	private int cooldownTicks = 0;
	private boolean isEnabled = true;
	private static final int START_CONCENTRATION_VALUE = 15;
	
	public TileEntityAirGenerator() {
		super();
		
		peripheralName = "warpdriveAirGenerator";
		addMethods(new String[] {
			"enable"
		});
	}
	
	@Override
	public void update() {
		super.update();
		
		if (worldObj.isRemote) {
			return;
		}
		
		if (isInvalid()) {
			return;
		}
		
		// Air generator works only in space & hyperspace
		if (WarpDrive.starMap.hasAtmosphere(worldObj, pos.getX(), pos.getZ())) {
			IBlockState blockState = worldObj.getBlockState(pos);
			if (blockState.getValue(BlockProperties.ACTIVE)) {
				worldObj.setBlockState(pos, blockState.withProperty(BlockProperties.ACTIVE, false)); // set disabled texture
			}
			return;
		}
		
		cooldownTicks++;
		if (cooldownTicks > WarpDriveConfig.AIRGEN_AIR_GENERATION_TICKS) {
			IBlockState blockState = worldObj.getBlockState(pos);
			if (isEnabled && energy_consume(WarpDriveConfig.AIRGEN_ENERGY_PER_NEWAIRBLOCK, true)) {
				if (!blockState.getValue(BlockProperties.ACTIVE)) {
					worldObj.setBlockState(pos, blockState.withProperty(BlockProperties.ACTIVE, true)); // set enabled texture
				}
			} else {
				if (blockState.getValue(BlockProperties.ACTIVE)) {
					worldObj.setBlockState(pos, blockState.withProperty(BlockProperties.ACTIVE, false)); // set disabled texture
				}
			}
			releaseAir(pos.north());
			releaseAir(pos.south());
			releaseAir(pos.east());
			releaseAir(pos.west());
			releaseAir(pos.up());
			releaseAir(pos.down());
			
			
			cooldownTicks = 0;
		}
	}
	
	private void releaseAir(BlockPos blockPos) {
		IBlockState blockState = worldObj.getBlockState(blockPos);
		if (blockState.getBlock().isAir(blockState, worldObj, blockPos)) {// can be air
			int energy_cost = (!blockState.getBlock().isAssociatedBlock(WarpDrive.blockAir)) ? WarpDriveConfig.AIRGEN_ENERGY_PER_NEWAIRBLOCK : WarpDriveConfig.AIRGEN_ENERGY_PER_EXISTINGAIRBLOCK;
			if (isEnabled && energy_consume(energy_cost, true)) {// enough energy
				if (worldObj.setBlockState(blockPos, WarpDrive.blockAir.getStateFromMeta(START_CONCENTRATION_VALUE), 2)) {
					// (needs to renew air or was not maxed out)
					energy_consume(WarpDriveConfig.AIRGEN_ENERGY_PER_NEWAIRBLOCK, false);
				} else {
					energy_consume(WarpDriveConfig.AIRGEN_ENERGY_PER_EXISTINGAIRBLOCK, false);
				}
			} else {// low energy => remove air block
				if (blockState.getBlock().isAssociatedBlock(WarpDrive.blockAir)) {
					int metadata = blockState.getBlock().getMetaFromState(blockState);
					if (metadata > 4) {
						worldObj.setBlockState(blockPos, WarpDrive.blockAir.getStateFromMeta(metadata - 4), 2);
					} else if (metadata > 1) {
						worldObj.setBlockState(blockPos, WarpDrive.blockAir.getStateFromMeta(1), 2);
					// } else {
						// worldObj.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 2);
					}
				}
			}
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		isEnabled = tag.getBoolean("isEnabled");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag.setBoolean("isEnabled", isEnabled);
		return super.writeToNBT(tag);
	}
	
	@Override
	public int energy_getMaxStorage() {
		return WarpDriveConfig.AIRGEN_MAX_ENERGY_STORED;
	}
	
	@Override
	public boolean energy_canInput(EnumFacing from) {
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("%s @ \'%s\' (%d %d %d)",
			getClass().getSimpleName(),
			worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(),
			pos.getX(), pos.getY(), pos.getZ());
	}
	
	public Object[] enable(Object[] arguments) {
		if (arguments.length == 1) {
			isEnabled = Commons.toBool(arguments[0]);
		}
		return new Object[]{isEnabled};
	}
	
	// OpenComputer callback methods
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] enable(Context context, Arguments arguments) {
		return enable(argumentsOCtoCC(arguments));
	}
	
	// ComputerCraft IPeripheral methods implementation
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
		String methodName = getMethodName(method);
		
		switch (methodName) {
		case "enable":
			return enable(arguments);
		}
		
		return super.callMethod(computer, context, method, arguments);
	}
}
