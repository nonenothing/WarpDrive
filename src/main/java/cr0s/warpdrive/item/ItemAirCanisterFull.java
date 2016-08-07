package cr0s.warpdrive.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IAirCanister;
import cr0s.warpdrive.data.EnumComponentType;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemAirCanisterFull extends Item implements IAirCanister {
	
	public ItemAirCanisterFull() {
		super();
		setMaxDamage(20);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setMaxStackSize(1);
		setUnlocalizedName("warpdrive.armor.AirCanisterFull");
		setRegistryName(getUnlocalizedName());
		GameRegistry.register(this);
	}
	
	@Override
	public boolean canContainAir(ItemStack itemStack) {
		if (itemStack != null && itemStack.getItem() instanceof ItemAirCanisterFull) {
			return itemStack.getItemDamage() > 0;
		}
		return false;
	}
	
	@Override
	public boolean containsAir(ItemStack itemStack) {
		return true;
	}
	
	@Override
	public ItemStack emptyDrop(ItemStack itemStack) {
		return ItemComponent.getItemStackNoCache(EnumComponentType.AIR_CANISTER, 1);
	}
	
	@Override
	public ItemStack fullDrop(ItemStack itemStack) {
		return new ItemStack(WarpDrive.itemAirCanisterFull, 1);
	}
}
