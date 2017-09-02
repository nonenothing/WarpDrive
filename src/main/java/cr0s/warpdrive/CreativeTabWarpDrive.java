package cr0s.warpdrive;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class CreativeTabWarpDrive extends CreativeTabs {
	
	public CreativeTabWarpDrive(final String label) {
		super(label);
	}
	
	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public Item getTabIconItem() {
		return WarpDrive.itemComponent;
    }
}
