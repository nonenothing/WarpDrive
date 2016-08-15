package cr0s.warpdrive.block.energy;

import cr0s.warpdrive.data.EnumComponentType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.ForgeDirection;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.config.WarpDriveConfig;

public class TileEntityEnergyBank extends TileEntityAbstractEnergy {
	
	static final byte MODE_DISABLED = 0;
	static final byte MODE_INPUT = 1;
	static final byte MODE_OUTPUT = 2;
	private static final byte[] MODE_DEFAULT_SIDES = { MODE_INPUT, MODE_INPUT, MODE_OUTPUT, MODE_OUTPUT, MODE_OUTPUT, MODE_OUTPUT };
	
	// persistent properties
	private byte tier = -1;
	private byte[] modeSide = MODE_DEFAULT_SIDES.clone();
	
	public TileEntityEnergyBank() {
		this((byte) 1);
	}
	
	public TileEntityEnergyBank(final byte tier) {
		super();
		this.tier = tier;
		peripheralName = "warpdriveEnergyBank";
		
		setUpgradeMaxCount(EnumComponentType.SUPERCONDUCTOR, WarpDriveConfig.ENERGY_BANK_EFFICIENCY_PER_UPGRADE.length - 1);
	}
	
	@Override
	protected void onFirstUpdateTick() {
		if (tier == 0) {
			IC2_sinkTier = Integer.MAX_VALUE;
			IC2_sourceTier = Integer.MAX_VALUE;
		} else {
			IC2_sinkTier = WarpDriveConfig.ENERGY_BANK_IC2_TIER[tier - 1];
			IC2_sourceTier = WarpDriveConfig.ENERGY_BANK_IC2_TIER[tier - 1];
		}
		super.onFirstUpdateTick();
	}
	
	private double getEfficiency() {
		int upgradeCount = clamp(0, getUpgradeMaxCount(EnumComponentType.SUPERCONDUCTOR), getUpgradeCount(EnumComponentType.SUPERCONDUCTOR));
		return WarpDriveConfig.ENERGY_BANK_EFFICIENCY_PER_UPGRADE[upgradeCount];
	}
	
	@Override
	public int energy_getEnergyStored() {
		if (tier == 0) {
			return WarpDriveConfig.ENERGY_BANK_MAX_ENERGY_STORED[2] / 2;
		} else {
			return super.energy_getEnergyStored();
		}
	}
	
	@Override
	public int energy_getPotentialOutput() {
		if (tier == 0) {
			return Integer.MAX_VALUE;
		} else {
			return (int) Math.round(Math.min(energy_getEnergyStored() * getEfficiency(), WarpDriveConfig.ENERGY_BANK_TRANSFER_PER_TICK[tier - 1]));
		}
	}
	
	@Override
	public int energy_getMaxStorage() {
		if (tier == 0) {
			return WarpDriveConfig.ENERGY_BANK_MAX_ENERGY_STORED[2];
		} else {
			return WarpDriveConfig.ENERGY_BANK_MAX_ENERGY_STORED[tier - 1];
		}
	}
	
	@Override
	public boolean energy_consume(final int amount_internal, final boolean simulate) {
		int amountWithLoss = (int) Math.round(amount_internal / getEfficiency());
		if (energy_getEnergyStored() >= amountWithLoss) {
			if (!simulate) {
				super.energy_consume(amountWithLoss);
			}
			return true;
		}
		return false;
	}
	@Override
	public void energy_consume(final int amount_internal) {
		int amountWithLoss = (int) Math.round(amount_internal > 0 ? amount_internal / getEfficiency() : amount_internal * getEfficiency());
		super.energy_consume(amountWithLoss);
	}
	
	@Override
	public boolean energy_canInput(ForgeDirection from) {
		return modeSide[from.ordinal()] == MODE_INPUT;
	}
	
	@Override
	public boolean energy_canOutput(ForgeDirection to) {
		return modeSide[to.ordinal()] == MODE_OUTPUT;
	}
	
	byte getTier() {
		return tier;
	}
	
	byte getMode(final EnumFacing facing) {
		return modeSide[facing.ordinal()];
	}
	
	void setMode(final EnumFacing facing, final byte mode) {
		modeSide[facing.ordinal()] = (byte)(mode % 3);
		markDirty();
		energy_resetConnections(facing);
	}
	
	@Override
	public String getStatus() {
		return super.getStatus()
		       + "\n" + getUpgradeStatus();
	}
	
	// Forge overrides
	@Override
	public void writeToNBT(NBTTagCompound nbtTagCompound) {
		super.writeToNBT(nbtTagCompound);
		nbtTagCompound.setByte("tier", tier);
		nbtTagCompound.setByteArray("modeSide", modeSide);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbtTagCompound) {
		super.readFromNBT(nbtTagCompound);
		tier = nbtTagCompound.getByte("tier");
		modeSide = nbtTagCompound.getByteArray("modeSide");
		if (modeSide == null || modeSide.length != 6) {
			modeSide = MODE_DEFAULT_SIDES.clone();
		}
	}
	
	@Override
	public NBTTagCompound writeItemDropNBT(NBTTagCompound nbtTagCompound) {
		nbtTagCompound = super.writeItemDropNBT(nbtTagCompound);
		return nbtTagCompound;
	}
	
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tagCompound = new NBTTagCompound();
		writeToNBT(tagCompound);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tagCompound);
	}
	
	@Override
	public void onDataPacket(NetworkManager networkManager, S35PacketUpdateTileEntity packet) {
		NBTTagCompound tagCompound = packet.func_148857_g();
		readFromNBT(tagCompound);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	@Override
	public String toString() {
		return String.format("%s @ \'%s\' (%d %d %d) %8d",
		getClass().getSimpleName(),
		worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(),
		xCoord, yCoord, zCoord,
		energy_getEnergyStored());
	}
}