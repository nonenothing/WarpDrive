package cr0s.warpdrive.api;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

public interface IItemBase {
    
    // wrapper for Forge ItemExpireEvent
    void onEntityExpireEvent(EntityItem entityItem, ItemStack itemStack);
}
