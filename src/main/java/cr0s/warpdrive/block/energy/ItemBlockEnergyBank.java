package cr0s.warpdrive.block.energy;

import cr0s.warpdrive.api.IBlockBase;
import cr0s.warpdrive.block.ItemBlockAbstractBase;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockEnergyBank extends ItemBlockAbstractBase {
	
	ItemBlockEnergyBank(final Block block) {
		super(block);
		setMaxDamage(0);
		setHasSubtypes(true);
	}
	
	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings("deprecation")
	public ModelResourceLocation getModelResourceLocation(final ItemStack itemStack) {
		final int damage = itemStack.getItemDamage();
		if (damage < 0 || damage > 15) {
			throw new IllegalArgumentException(String.format("Invalid damage %d for %s", damage, itemStack.getItem()));
		}
		final ResourceLocation resourceLocation = getRegistryName();
		final String variant = block.getStateFromMeta(damage).toString().split("[\\[\\]]")[1];
		return new ModelResourceLocation(resourceLocation, variant);
	}
	
	@Nonnull
	@Override
	public String getUnlocalizedName(final ItemStack itemStack) {
		if (itemStack == null) {
			return getUnlocalizedName();
		}
		return getUnlocalizedName() + itemStack.getItemDamage();
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
