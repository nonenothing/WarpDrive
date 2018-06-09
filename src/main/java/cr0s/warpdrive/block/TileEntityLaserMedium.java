package cr0s.warpdrive.block;

import cr0s.warpdrive.config.WarpDriveConfig;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class TileEntityLaserMedium extends TileEntityAbstractEnergy {
	
	private int ticks = 0;
	
	public TileEntityLaserMedium() {
		peripheralName = "warpdriveLaserMedium";
		OC_enable = false;
	}
	
	@Override
	public void update() {
		super.update();
		
		if (world.isRemote) {
			return;
		}
		
		ticks++;
		if (ticks > 20) {
			ticks = 0;
			
			final int level = Math.max(0, Math.min(7, Math.round((energy_getEnergyStored() * 8) / energy_getMaxStorage())));
			final IBlockState blockState = world.getBlockState(pos);
			if (blockState.getValue(BlockLaserMedium.LEVEL) != level) {
				updateBlockState(blockState, BlockLaserMedium.LEVEL, level);
			}
		}
	}
	
	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
	}
	
	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(final NBTTagCompound tagCompound) {
		return super.writeToNBT(tagCompound);
	}
	
	// IEnergySink methods implementation
	@Override
	public int energy_getMaxStorage() {
		return WarpDriveConfig.LASER_MEDIUM_MAX_ENERGY_STORED;
	}
	
	@Override
	public boolean energy_canInput(final EnumFacing from) {
		return true;
	}
}
