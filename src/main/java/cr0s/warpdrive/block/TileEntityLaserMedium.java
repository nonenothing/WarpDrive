package cr0s.warpdrive.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import cr0s.warpdrive.config.WarpDriveConfig;

public class TileEntityLaserMedium extends TileEntityAbstractEnergy {
	private int ticks = 0;
	
	public TileEntityLaserMedium() {
		peripheralName = "warpdriveLaserMedium";
		OC_enable = false;
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if (worldObj.isRemote) {
			return;
		}
		
		ticks++;
		if (ticks > 20) {
			ticks = 0;
			
			int metadata = Math.max(0, Math.min(7, Math.round((getEnergyStored() * 8) / getMaxEnergyStored())));
			if (getBlockMetadata() != metadata) {
				updateMetadata(metadata);
			}
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		return super.writeToNBT(tag);
	}
	
	// IEnergySink methods implementation
	@Override
	public int getMaxEnergyStored() {
		return WarpDriveConfig.LASER_MEDIUM_MAX_ENERGY_STORED;
	}
	
	@Override
	public boolean canInputEnergy(EnumFacing from) {
		return true;
	}
}
