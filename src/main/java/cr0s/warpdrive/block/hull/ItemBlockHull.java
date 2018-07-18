package cr0s.warpdrive.block.hull;

import cr0s.warpdrive.api.IBlockBase;
import cr0s.warpdrive.block.ItemBlockAbstractBase;
import cr0s.warpdrive.data.EnumTier;

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
	
	ItemBlockHull(final Block block) {
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
			throw new IllegalArgumentException(String.format("Invalid damage %d for %s",
			                                                 damage, itemStack.getItem()));
		}
		final ResourceLocation resourceLocation = getRegistryName();
		assert resourceLocation != null;
		final String variant;
		if (block instanceof BlockHullStairs) {
			variant = "facing=east,half=bottom,shape=straight";
		} else {
			variant = block.getStateFromMeta(damage).toString().split("[\\[\\]]")[1];
		}
		return new ModelResourceLocation(resourceLocation, variant);
	}
	
	@Nonnull
	@Override
	public String getUnlocalizedName(final ItemStack itemStack) {
		if (itemStack == null || block instanceof BlockHullStairs) {
			return getUnlocalizedName();
		}
		return getUnlocalizedName() + EnumDyeColor.byMetadata( itemStack.getItemDamage() ).getUnlocalizedName();
	}
	
	private EnumTier getTier(final ItemStack itemStack) {
		if (block instanceof IBlockBase) {
			return ((IBlockBase) block).getTier(itemStack);
		}
		return EnumTier.BASIC;
	}
	
	@Nonnull
	@Override
	public EnumRarity getRarity(@Nonnull final ItemStack itemStack) {
		return getTier(itemStack).getRarity();
	}
}
