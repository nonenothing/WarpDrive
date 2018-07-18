package cr0s.warpdrive.block.decoration;

import cr0s.warpdrive.block.ItemBlockAbstractBase;
import cr0s.warpdrive.data.EnumGasColor;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class ItemBlockGas extends ItemBlockAbstractBase {
	
	public ItemBlockGas(final Block block) {
		super(block);
		
		setHasSubtypes(true);
	}
	
	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public ModelResourceLocation getModelResourceLocation(final ItemStack itemStack) {
		final int damage = itemStack.getItemDamage();
		if (damage < 0 || damage > EnumGasColor.length) {
			throw new IllegalArgumentException(String.format("Invalid damage %d for %s",
			                                                 damage, itemStack.getItem()));
		}
		final ResourceLocation resourceLocation = getRegistryName();
		assert resourceLocation != null;
		final String variant = String.format("color=%s",
		                                     EnumGasColor.get( itemStack.getItemDamage() ).getUnlocalizedName());
		return new ModelResourceLocation(resourceLocation, variant);
	}
}
