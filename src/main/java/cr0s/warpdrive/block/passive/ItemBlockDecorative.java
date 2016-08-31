package cr0s.warpdrive.block.passive;

import java.util.List;

import cr0s.warpdrive.block.ItemBlockAbstractBase;
import cr0s.warpdrive.data.EnumDecorativeType;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
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
	
	@Override
	public int getMetadata(int damage) {
		return damage;
	}
	
	@Override
	public void getSubItems(@Nonnull Item item, @Nonnull CreativeTabs creativeTabs, @Nonnull List<ItemStack> subItems) {
		for (EnumDecorativeType enumDecorativeType : EnumDecorativeType.values()) {
			subItems.add(new ItemStack(item, 1, enumDecorativeType.ordinal()));
		}
	}
	
	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public ModelResourceLocation getModelResourceLocation(ItemStack itemStack) {
		int damage = itemStack.getItemDamage();
		ResourceLocation resourceLocation = getRegistryName();
		if (damage >= 0 && damage < EnumDecorativeType.length) {
			resourceLocation = new ResourceLocation(resourceLocation.getResourceDomain(), resourceLocation.getResourcePath() + "_" + EnumDecorativeType.get(damage).getUnlocalizedName());
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
