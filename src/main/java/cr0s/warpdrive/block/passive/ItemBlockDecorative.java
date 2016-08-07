package cr0s.warpdrive.block.passive;

import java.util.List;

import cr0s.warpdrive.data.EnumDecorativeType;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemBlockDecorative extends ItemBlock {
	
	public ItemBlockDecorative(Block block) {
		super(block);
		setHasSubtypes(true);
		setUnlocalizedName("warpdrive.passive.decorative");
	}
	
	@Override
	public int getMetadata(int damage) {
		return damage;
	}
	
	@Override
	public void getSubItems(@Nonnull Item item, @Nonnull CreativeTabs creativeTabs, @Nonnull List<ItemStack> subItems) {
		for (EnumDecorativeType enumDecorativeType : EnumDecorativeType.values()) {
			subItems.add(new ItemStack(item, 1, enumDecorativeType.ordinal()));
		}
	}
	
	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		if (itemstack == null) {
			return getUnlocalizedName();
		}
		return "tile.warpdrive.passive." + EnumDecorativeType.get(itemstack.getItemDamage()).unlocalizedName;
	}
}
