package cr0s.warpdrive.block.decoration;

import cr0s.warpdrive.block.ItemBlockAbstractBase;
import cr0s.warpdrive.data.EnumDecorativeType;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemBlockDecorative extends ItemBlockAbstractBase {
	
	public ItemBlockDecorative(final Block block) {
		super(block, true, false);
	}
	
	@Nonnull
	@Override
	public String getUnlocalizedName(final ItemStack itemStack) {
		if (itemStack == null) {
			return getUnlocalizedName();
		}
		return getUnlocalizedName() + EnumDecorativeType.get(itemStack.getItemDamage()).getName();
	}
}
