package cr0s.warpdrive.api;

import net.minecraft.item.ItemStack;

@Deprecated // we should use fluid storage instead here
public interface IAirCanister {
	// Return true if that itemStack is Air compatible (i.e. may or already contains air)
	boolean canContainAir(ItemStack itemStack);
	
	boolean containsAir(ItemStack itemStack);
	ItemStack emptyDrop(ItemStack itemStack);
	ItemStack fullDrop(ItemStack itemStack);
}
