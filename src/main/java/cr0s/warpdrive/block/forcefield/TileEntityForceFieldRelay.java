package cr0s.warpdrive.block.forcefield;

import cr0s.warpdrive.api.IForceFieldUpgrade;
import cr0s.warpdrive.api.IForceFieldUpgradeEffector;
import cr0s.warpdrive.data.EnumForceFieldUpgrade;
import cr0s.warpdrive.data.ForceFieldSetup;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.text.translation.I18n;

public class TileEntityForceFieldRelay extends TileEntityAbstractForceField implements IForceFieldUpgrade {

	// persistent properties
	private EnumForceFieldUpgrade upgrade = EnumForceFieldUpgrade.NONE;
	
	public TileEntityForceFieldRelay() {
		super();
		
		peripheralName = "warpdriveForceFieldRelay";
	}
	
	// onFirstUpdateTick
	// updateEntity
	
	protected EnumForceFieldUpgrade getUpgrade() {
		if (upgrade == null) {
			return EnumForceFieldUpgrade.NONE;
		}
		return upgrade;
	}
	
	protected void setUpgrade(EnumForceFieldUpgrade upgrade) {
		this.upgrade = upgrade;
		markDirty();
	}
	
	private String getUpgradeStatus() {
		EnumForceFieldUpgrade enumForceFieldUpgrade = getUpgrade();
		String strDisplayName = I18n.translateToLocalFormatted("warpdrive.forcefield.upgrade.statusLine." + enumForceFieldUpgrade.unlocalizedName);
		if (enumForceFieldUpgrade == EnumForceFieldUpgrade.NONE) {
			return I18n.translateToLocalFormatted("warpdrive.forcefield.upgrade.statusLine.none",
				strDisplayName);
		} else {
			return I18n.translateToLocalFormatted("warpdrive.forcefield.upgrade.statusLine.valid",
				strDisplayName);
		}
	}
	
	public String getStatus() {
		return super.getStatus()
			       + "\n" + getUpgradeStatus();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		setUpgrade(EnumForceFieldUpgrade.get(tag.getByte("upgrade")));
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag = super.writeToNBT(tag);
		tag.setByte("upgrade", (byte) getUpgrade().ordinal());
		return tag;
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound tagCompound = new NBTTagCompound();
		writeToNBT(tagCompound);
		return new SPacketUpdateTileEntity(pos, 1, tagCompound);
	}
	
	@Override
	public void onDataPacket(NetworkManager networkManager, SPacketUpdateTileEntity packet) {
		NBTTagCompound tagCompound = packet.getNbtCompound();
		readFromNBT(tagCompound);
	}
	
	@Override
	public IForceFieldUpgradeEffector getUpgradeEffector() {
		return isEnabled ? getUpgrade() : null;
	}
	
	@Override
	public float getUpgradeValue() {
		return isEnabled ? getUpgrade().getUpgradeValue() * (1.0F + (tier - 1) * ForceFieldSetup.FORCEFIELD_UPGRADE_BOOST_FACTOR_PER_RELAY_TIER) : 0.0F;
	}
}
