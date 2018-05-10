package cr0s.warpdrive.block.passive;

import cr0s.warpdrive.data.EnumDecorativeType;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockDecorative extends ItemBlock {
	
	public ItemBlockDecorative(final Block block) {
		super(block);
		setHasSubtypes(true);
		setUnlocalizedName("warpdrive.decoration.decorative");
	}
	
	@Override
	public int getMetadata(final int damage) {
		return damage;
	}
	
	@Override
	public void getSubItems(final Item item, final CreativeTabs creativeTabs, final List list) {
		for (final EnumDecorativeType decorativeType : EnumDecorativeType.values()) {
			list.add(new ItemStack(item, 1, decorativeType.ordinal()));
		}
	}
	
	@Override
	public String getUnlocalizedName(final ItemStack itemstack) {
		if (itemstack == null) {
			return getUnlocalizedName();
		}
		return "tile.warpdrive.decoration.decorative." + EnumDecorativeType.get(itemstack.getItemDamage()).unlocalizedName;
	}
}
