package cr0s.warpdrive.api;

import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public interface IBlockBase {
	
	byte getTier(final ItemStack itemStack);
	
	EnumRarity getRarity(final ItemStack itemStack, final EnumRarity rarity);
	
    @Nullable
    ItemBlock createItemBlock();
    
    void modelInitialisation();
}
