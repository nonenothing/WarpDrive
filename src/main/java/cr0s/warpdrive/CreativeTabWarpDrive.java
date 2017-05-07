package cr0s.warpdrive;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CreativeTabWarpDrive extends CreativeTabs {
	
	public CreativeTabWarpDrive(final String label) {
		super(label);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Item getTabIconItem() {
		return WarpDrive.itemComponent;
    }
}
