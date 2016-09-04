package cr0s.warpdrive.block.passive;

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
	
	public ItemBlockGas(Block block) {
		super(block);
		setHasSubtypes(true);
		setUnlocalizedName("warpdrive.passive.Gas");
	}
	
	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public ModelResourceLocation getModelResourceLocation(ItemStack itemStack) {
		int damage = itemStack.getItemDamage();
		ResourceLocation resourceLocation = getRegistryName();
		if (damage >= 0 && damage < EnumGasColor.length) {
			resourceLocation = new ResourceLocation(resourceLocation.getResourceDomain(), resourceLocation.getResourcePath() + "-" + EnumGasColor.get(damage).getUnlocalizedName());
		}
		return new ModelResourceLocation(resourceLocation, "inventory");
	}
}
