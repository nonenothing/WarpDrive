package cr0s.warpdrive.block.energy;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.EnumComponentType;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

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
			IC2_sinkTier = IC2_sinkTier_max;
			IC2_sourceTier = IC2_sourceTier_max;
		} else {
			IC2_sinkTier = WarpDriveConfig.ENERGY_BANK_IC2_TIER[tier - 1];
			IC2_sourceTier = WarpDriveConfig.ENERGY_BANK_IC2_TIER[tier - 1];
		}
		super.onFirstUpdateTick();
	}
	
	private double getEfficiency() {
		int upgradeCount = Commons.clamp(0, getUpgradeMaxCount(EnumComponentType.SUPERCONDUCTOR), getUpgradeCount(EnumComponentType.SUPERCONDUCTOR));
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
			return Integer.MAX_VALUE / 2;
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
	public boolean energy_consume(final long amount_internal, final boolean simulate) {
		if (tier == 0) {
			return true;
		}
		final int amountWithLoss = (int) Math.round(amount_internal / getEfficiency());
		if (energy_getEnergyStored() >= amountWithLoss) {
			if (!simulate) {
				super.energy_consume(amountWithLoss);
			}
			return true;
		}
		return false;
	}
	@Override
	public void energy_consume(final long amount_internal) {
		if (tier == 0) {
			return;
		}
		final int amountWithLoss = (int) Math.round(amount_internal > 0 ? amount_internal / getEfficiency() : amount_internal * getEfficiency());
		super.energy_consume(amountWithLoss);
	}
	
	@Override
	public boolean energy_canInput(EnumFacing from) {
		return modeSide[from.ordinal()] == MODE_INPUT;
	}
	
	@Override
	public boolean energy_canOutput(EnumFacing to) {
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
	public ITextComponent getStatus() {
		return super.getStatus()
		       .appendSibling(new TextComponentString("\n")).appendSibling(getUpgradeStatus());
	}
	
	// Forge overrides
	public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
		super.writeToNBT(nbtTagCompound);
		nbtTagCompound.setByte("tier", tier);
		nbtTagCompound.setByteArray("modeSide", modeSide);
		return nbtTagCompound;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbtTagCompound) {
		super.readFromNBT(nbtTagCompound);
		tier = nbtTagCompound.getByte("tier");
		modeSide = nbtTagCompound.getByteArray("modeSide");
		if (modeSide.length != 6) {
			modeSide = MODE_DEFAULT_SIDES.clone();
		}
	}
	
	@Override
	public NBTTagCompound writeItemDropNBT(NBTTagCompound nbtTagCompound) {
		nbtTagCompound = super.writeItemDropNBT(nbtTagCompound);
		return nbtTagCompound;
	}

	@Nonnull
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound tagCompound = new NBTTagCompound();
		writeToNBT(tagCompound);
		return tagCompound;
	}
	
	@Override
	public void onDataPacket(NetworkManager networkManager, SPacketUpdateTileEntity packet) {
		NBTTagCompound tagCompound = packet.getNbtCompound();
		readFromNBT(tagCompound);
		worldObj.markBlockRangeForRenderUpdate(pos, pos);
	}
	
	@Override
	public String toString() {
		return String.format("%s @ \'%s\' (%d %d %d) %8d",
			getClass().getSimpleName(),
			worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(),
			pos.getX(), pos.getY(), pos.getZ(),
			energy_getEnergyStored());
	}
}