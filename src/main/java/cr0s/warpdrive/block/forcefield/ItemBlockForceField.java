package cr0s.warpdrive.block.forcefield;

import cr0s.warpdrive.block.ItemBlockAbstractBase;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class ItemBlockForceField extends ItemBlockAbstractBase {
	
	public ItemBlockForceField(final Block block) {
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
			throw new IllegalArgumentException(String.format("Invalid damage %d for %s",
			                                                 damage, itemStack.getItem()));
		}
		final ResourceLocation resourceLocation = getRegistryName();
		assert resourceLocation != null;
		final String variant = String.format("frequency=%d",
		                                     itemStack.getItemDamage());
		return new ModelResourceLocation(resourceLocation, variant);
	}
}
