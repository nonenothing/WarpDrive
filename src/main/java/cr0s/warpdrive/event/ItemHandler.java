package cr0s.warpdrive.event;

import cr0s.warpdrive.api.IItemBase;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;

public class ItemHandler {
	
	@SubscribeEvent
	public static void onItemExpireEvent(final ItemExpireEvent event) {
		if (event.entityItem == null) {
			return;
		}
		final ItemStack itemStack = event.entityItem.getEntityItem();
		if (itemStack == null) {
			return;
		}
		final Item item = itemStack.getItem();
		if (!(item instanceof IItemBase)) {
			return;
		}
		((IItemBase) item).onEntityExpireEvent(event.entityItem, itemStack);
	}
}
