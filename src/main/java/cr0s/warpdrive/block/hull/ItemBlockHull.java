package cr0s.warpdrive.block.hull;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
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
}
