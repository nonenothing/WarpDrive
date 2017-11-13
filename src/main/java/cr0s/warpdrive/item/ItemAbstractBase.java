package cr0s.warpdrive.item;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IItemBase;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemAbstractBase extends Item implements IItemBase {
	
	public ItemAbstractBase() {
		super();
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
	}
	
	@Override
	public void onEntityExpireEvent(final EntityItem entityItem, final ItemStack itemStack) {
	}
}
