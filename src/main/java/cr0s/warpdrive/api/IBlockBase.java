package cr0s.warpdrive.api;

import cr0s.warpdrive.data.EnumTier;

import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IBlockBase {
	
	@Nonnull
	EnumTier getTier(final ItemStack itemStack);
	
	EnumRarity getRarity(final ItemStack itemStack);
	
    @Nullable
    ItemBlock createItemBlock();
    
    void modelInitialisation();
}
