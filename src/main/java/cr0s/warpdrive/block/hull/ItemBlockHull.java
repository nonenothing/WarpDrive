package cr0s.warpdrive.block.hull;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemBlockHull extends ItemBlock {
	
	public ItemBlockHull(Block block) {
		super(block);	// sets field_150939_a to block
		setMaxDamage(0);
		setHasSubtypes(true);
		setUnlocalizedName("warpdrive.hull");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int p_77617_1_) {
		return field_150939_a.getIcon(2, BlockColored.func_150031_c(p_77617_1_));
	}
	
	@Override
	public int getMetadata(int damage) {
		return damage;
	}
	
	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		if (itemstack == null || field_150939_a instanceof BlockHullStairs) {
			return getUnlocalizedName();
		}
		return getUnlocalizedName() + ItemDye.field_150923_a[BlockColored.func_150031_c(itemstack.getItemDamage())];
	}
	
	private byte getTier() {
		if (field_150939_a instanceof BlockHullGlass) {
			return ((BlockHullGlass)field_150939_a).tier;
		} else if (field_150939_a instanceof BlockHullPlain) {
			return ((BlockHullPlain)field_150939_a).tier;
		} else if (field_150939_a instanceof BlockHullStairs) {
			return ((BlockHullStairs)field_150939_a).tier;
		}
		return 1;
	}
	
	@Override
	public EnumRarity getRarity(final ItemStack itemStack) {
		switch (getTier()) {
			case 0:	return EnumRarity.epic;
			case 1:	return EnumRarity.common;
			case 2:	return EnumRarity.uncommon;
			case 3:	return EnumRarity.rare;
			default: return EnumRarity.common;
		}
	}
}
