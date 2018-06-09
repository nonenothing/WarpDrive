package cr0s.warpdrive.block.detection;

import cr0s.warpdrive.block.ItemBlockAbstractBase;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockSiren extends ItemBlockAbstractBase {
	
	ItemBlockSiren(final Block block) {
		super(block);
		setMaxDamage(0);
		setHasSubtypes(true);
	}
	
	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public ModelResourceLocation getModelResourceLocation(final ItemStack itemStack) {
		final int damage = itemStack.getItemDamage();
		if (damage < 0 || damage > 15) {
			throw new IllegalArgumentException(String.format("Invalid damage %d for %s", damage, itemStack.getItem()));
		}
		final ResourceLocation resourceLocation = getRegistryName();
		@SuppressWarnings("deprecation")
		final String variant = block.getStateFromMeta(damage).toString().split("[\\[\\]]")[1];
		return new ModelResourceLocation(resourceLocation, variant);
	}
}
