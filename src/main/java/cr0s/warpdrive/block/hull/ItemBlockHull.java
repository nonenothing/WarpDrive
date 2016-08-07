package cr0s.warpdrive.block.hull;

import net.minecraft.block.Block;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemBlockHull extends ItemBlock {
	
	public ItemBlockHull(Block block) {
		super(block);	// sets field_150939_a to block
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
		if (itemstack == null) {
			return getUnlocalizedName();
		}
		return getUnlocalizedName() + EnumDyeColor.byDyeDamage( itemstack.getItemDamage() ).getUnlocalizedName();
	}
}
