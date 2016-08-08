package cr0s.warpdrive.block.hull;

import cr0s.warpdrive.block.ItemBlockAbstractBase;
import net.minecraft.block.Block;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemBlockHull extends ItemBlockAbstractBase {
	
	public ItemBlockHull(Block block) {
		super(block);
		setMaxDamage(0);
		setHasSubtypes(true);
		setUnlocalizedName("warpdrive.hull");
	}
	
	@Override
	public int getMetadata(int damage) {
		return damage;
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		if (itemstack == null || block instanceof BlockHullStairs) {
			return getUnlocalizedName();
		}
		return getUnlocalizedName() + EnumDyeColor.byDyeDamage( itemstack.getItemDamage() ).getUnlocalizedName();
	}
}
