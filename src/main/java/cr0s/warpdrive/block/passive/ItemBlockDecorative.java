package cr0s.warpdrive.block.passive;

import cr0s.warpdrive.block.ItemBlockAbstractBase;
import cr0s.warpdrive.data.EnumDecorativeType;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class ItemBlockDecorative extends ItemBlockAbstractBase {
	
	public ItemBlockDecorative(Block block) {
		super(block);
		setHasSubtypes(true);
		setUnlocalizedName("warpdrive.passive.Decorative");
	}
	
	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public ModelResourceLocation getModelResourceLocation(ItemStack itemStack) {
		int damage = itemStack.getItemDamage();
		ResourceLocation resourceLocation = getRegistryName();
		if (damage >= 0 && damage < EnumDecorativeType.length) {
			resourceLocation = new ResourceLocation(resourceLocation.getResourceDomain(), resourceLocation.getResourcePath() + "-" + EnumDecorativeType.get(damage).getUnlocalizedName());
		}
		return new ModelResourceLocation(resourceLocation, "inventory");
	}
	
	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		if (itemStack == null) {
			return getUnlocalizedName();
		}
		return "tile.warpdrive.passive.Decorative." + EnumDecorativeType.get(itemStack.getItemDamage()).getUnlocalizedName();
	}
}
