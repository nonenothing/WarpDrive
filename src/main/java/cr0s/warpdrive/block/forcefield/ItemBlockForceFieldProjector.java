package cr0s.warpdrive.block.forcefield;

import cr0s.warpdrive.block.ItemBlockAbstractBase;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class ItemBlockForceFieldProjector extends ItemBlockAbstractBase {
	
	public ItemBlockForceFieldProjector(Block block) {
		super(block);
		setMaxDamage(0);
		setHasSubtypes(true);
		setUnlocalizedName("warpdrive.forcefield.projector");
	}
	
	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		if (itemstack == null) {
			return getUnlocalizedName();
		}
		return getUnlocalizedName() + (itemstack.getItemDamage() == 1 ? ".double" : ".single");
	}
}
