package cr0s.warpdrive.item;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IAirContainerItem;
import cr0s.warpdrive.data.EnumComponentType;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemAirCanisterFull extends Item implements IAirContainerItem {
	
	private IIcon icon;
	
	public ItemAirCanisterFull() {
		super();
		setMaxDamage(20);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setMaxStackSize(1);
		setUnlocalizedName("warpdrive.armor.AirCanisterFull");
	}
	
	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("warpdrive:AirCanisterFull");
	}
	
	@Override
	public IIcon getIconFromDamage(int damage) {
		return icon;
	}
	
	@Override
	public boolean canContainAir(ItemStack itemStack) {
		if ( itemStack == null
		  || itemStack.getItem() != this ) {
			return false;
		}
		return itemStack.getItemDamage() > 0;
	}
	
	@Override
	public int getMaxAirStorage(ItemStack itemStack) {
		if ( itemStack == null
		  || itemStack.getItem() != this ) {
			return 0;
		}
		return itemStack.getMaxDamage();
	}
	
	@Override
	public int getCurrentAirStorage(ItemStack itemStack) {
		if ( itemStack == null
		  || itemStack.getItem() != this ) {
			return 0;
		}
		return getMaxDamage() - itemStack.getItemDamage();
	}
	
	@Override
	public ItemStack consumeAir(ItemStack itemStack) {
		if ( itemStack == null
		  || itemStack.getItem() != this ) {
			return itemStack;
		}
		itemStack.setItemDamage(itemStack.getItemDamage() + 1); // bypass unbreaking enchantment
		if (itemStack.getItemDamage() >= itemStack.getMaxDamage()) {
			return getEmptyAirContainer(itemStack);
		}
		return itemStack;
	}
	
	@Override
	public int airTicksPerConsumption(ItemStack itemStack) {
		return 300;
	}
	
	@Override
	public ItemStack getEmptyAirContainer(ItemStack itemStack) {
		return ItemComponent.getItemStackNoCache(EnumComponentType.AIR_CANISTER, 1);
	}
	
	@Override
	public ItemStack getFullAirContainer(ItemStack itemStack) {
		return new ItemStack(WarpDrive.itemAirCanisterFull, 1);
	}
}
