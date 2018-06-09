package cr0s.warpdrive;

import cr0s.warpdrive.data.EnumComponentType;
import cr0s.warpdrive.item.ItemComponent;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

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
	public ItemStack getTabIconItem() {
		return ItemComponent.getItemStack(EnumComponentType.EMERALD_CRYSTAL);
    }
}
