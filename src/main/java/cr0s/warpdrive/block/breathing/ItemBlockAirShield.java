package cr0s.warpdrive.block.breathing;

import cr0s.warpdrive.block.ItemBlockAbstractBase;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemBlockAirShield extends ItemBlockAbstractBase {
	
	public ItemBlockAirShield(final Block block) {
		super(block);
		
		setHasSubtypes(true);
	}
	
	@Override
	public int getMetadata(final int damage) {
		return damage;
	}
	
	@Override
	public void getSubItems(@Nonnull final CreativeTabs creativeTab, @Nonnull final NonNullList<ItemStack> list) {
		if (!isInCreativeTab(creativeTab)) {
			return;
		}
		for (int metadata = 0; metadata < 16; metadata++) {
			list.add(new ItemStack(this, 1, metadata));
		}
	}
	
	@Nonnull
	@Override
	public String getUnlocalizedName(final ItemStack itemstack) {
		return getUnlocalizedName();
	}
}
