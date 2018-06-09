package cr0s.warpdrive.block.forcefield;

import cr0s.warpdrive.block.ItemBlockAbstractBase;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockForceFieldProjector extends ItemBlockAbstractBase {
	
	public ItemBlockForceFieldProjector(final Block block) {
		super(block);
		setMaxDamage(0);
		setHasSubtypes(true);
	}
	
	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public ModelResourceLocation getModelResourceLocation(final ItemStack itemStack) {
		final int damage = itemStack.getItemDamage();
		final ResourceLocation resourceLocation = getRegistryName();
		String stringVariant = "inventory";
/*		if (itemStack.hasTagCompound() && itemStack.getTagCompound() != null) {
			final NBTTagCompound tagCompound = itemStack.getTagCompound();
			stringVariant = "facing=" + (tagCompound.getBoolean("isDoubleSided") ? "east" : "north")
					+ ",is_double_sided=false"
					+ "state=connected_powered,shape=" + EnumForceFieldShape.get(tagCompound.getByte("shape")).getName();
		} else {
			if (damage >= 0 && damage < 2) {
				stringVariant = "facing=" + (damage == 1 ? "east" : "north")
						+ ",is_double_sided=false"
						+ ",state=connected_powered,shape=" + EnumForceFieldShape.NONE.getName();
			}
		}/**/
		return new ModelResourceLocation(resourceLocation, stringVariant);
	}
	
	@Nonnull
	@Override
	public String getUnlocalizedName(final ItemStack itemStack) {
		if (itemStack == null) {
			return getUnlocalizedName();
		}
		return getUnlocalizedName() + (itemStack.getItemDamage() == 1 ? ".double" : ".single");
	}
}
