package cr0s.warpdrive.block.hull;

import cr0s.warpdrive.api.IBlockBase;
import cr0s.warpdrive.block.ItemBlockAbstractBase;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockHull extends ItemBlockAbstractBase {
	
	ItemBlockHull(Block block) {
		super(block);
		setMaxDamage(0);
		setHasSubtypes(true);
		setUnlocalizedName("warpdrive.hull");
	}
	
	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public ModelResourceLocation getModelResourceLocation(ItemStack itemStack) {
		int damage = itemStack.getItemDamage();
		if (damage < 0 || damage > 15) {
			throw new IllegalArgumentException(String.format("Invalid damage %d for %s", damage, itemStack.getItem()));
		}
		ResourceLocation resourceLocation = getRegistryName();
		String variant = String.format("color=%s", EnumDyeColor.byDyeDamage( itemStack.getItemDamage() ).getName());
		return new ModelResourceLocation(resourceLocation, variant);
	}
	
	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		if (itemStack == null || block instanceof BlockHullStairs) {
			return getUnlocalizedName();
		}
		return getUnlocalizedName() + EnumDyeColor.byDyeDamage( itemStack.getItemDamage() ).getUnlocalizedName();
	}
	
	private byte getTier(final ItemStack itemStack) {
		if (block instanceof IBlockBase) {
			return ((IBlockBase) block).getTier(itemStack);
		}
		return 1;
	}
	
	@Nonnull
	@Override
	public EnumRarity getRarity(@Nonnull final ItemStack itemStack) {
		switch (getTier(itemStack)) {
			case 0:	return EnumRarity.EPIC;
			case 1:	return EnumRarity.COMMON;
			case 2:	return EnumRarity.UNCOMMON;
			case 3:	return EnumRarity.RARE;
			default: return EnumRarity.COMMON;
		}
	}
}
