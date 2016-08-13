package cr0s.warpdrive.block.energy;

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
	private byte[] modeSide = MODE_DEFAULT_SIDES.clone();
	
	public TileEntityEnergyBank() {
		super();
		IC2_sinkTier = 0;
		IC2_sourceTier = 0;
		peripheralName = "warpdriveEnergyBank";
	}
	
	@Override
	public int getPotentialEnergyOutput() {
		return getEnergyStored();
	}
	
	@Override
	protected void energyOutputDone(int energyOutput) {
		consumeEnergy(energyOutput, false);
	}
	
	@Override
	public int getMaxEnergyStored() {
		return WarpDriveConfig.ENERGY_BANK_MAX_ENERGY_STORED;
	}
	
	@Override
	public boolean canInputEnergy(ForgeDirection from) {
		return modeSide[from.ordinal()] == MODE_INPUT;
	}
	
	@Override
	public boolean canOutputEnergy(ForgeDirection to) {
		return modeSide[to.ordinal()] == MODE_OUTPUT;
	}
	
	byte getMode(final EnumFacing facing) {
		return modeSide[facing.ordinal()];
	}
	
	void setMode(final EnumFacing facing, final byte mode) {
		modeSide[facing.ordinal()] = (byte)(mode % 3);
		markDirty();
	}
	
	// Forge overrides
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setByteArray("modeSide", modeSide);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		modeSide = nbt.getByteArray("modeSide");
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
}