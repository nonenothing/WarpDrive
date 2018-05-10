package cr0s.warpdrive.block.forcefield;

import cr0s.warpdrive.block.ItemBlockAbstractBase;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class ItemBlockForceFieldProjector extends ItemBlockAbstractBase {
	
	public ItemBlockForceFieldProjector(final Block block) {
		super(block);
		setMaxDamage(0);
		setHasSubtypes(true);
		setUnlocalizedName("warpdrive.forcefield.projector");
	}
	
	@Override
	public String getUnlocalizedName(final ItemStack itemstack) {
		if (itemstack == null) {
			return getUnlocalizedName();
		}
		return getUnlocalizedName() + (itemstack.getItemDamage() == 1 ? ".double" : ".single");
	}
}
