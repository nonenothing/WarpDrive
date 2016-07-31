package cr0s.warpdrive.block.weapon;

import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;
import cr0s.warpdrive.block.TileEntityAbstractInterfaced;
import net.minecraft.util.text.translation.I18n;

public class TileEntityWeaponController extends TileEntityAbstractInterfaced {
	
	public TileEntityWeaponController() {
		super();
		
		peripheralName = "warpdriveWeaponController";
		addMethods(new String[] {
		});
		CC_scripts = Arrays.asList("startup");
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
	}
	
	public String getStatus() {
		return I18n.translateToLocalFormatted("warpdrive.guide.prefix",
				getBlockType().getLocalizedName());
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		return super.writeToNBT(tag);
	}
	
	@Override
	public String toString() {
		return String.format("%s @ \'%s\' (%d %d %d)", getClass().getSimpleName(),
				worldObj == null ? "~NULL~" : worldObj.getWorldInfo().getWorldName(), pos.getX(), pos.getY(), pos.getZ());
	}
}