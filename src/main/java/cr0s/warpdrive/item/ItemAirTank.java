package cr0s.warpdrive.item;

import cr0s.warpdrive.api.IAirContainerItem;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.EnumAirTankTier;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;

public class ItemAirTank extends ItemAbstractBase implements IAirContainerItem {
	
	protected EnumAirTankTier enumAirTankTier;
	
	public ItemAirTank(final String registryName, final EnumAirTankTier enumAirTankTier) {
		super(registryName);
		
		this.enumAirTankTier = enumAirTankTier;
		setMaxDamage(WarpDriveConfig.BREATHING_AIR_TANK_CAPACITY_BY_TIER[enumAirTankTier.getIndex()]);
		setMaxStackSize(1);
		setUnlocalizedName("warpdrive.breathing.air_tank." + enumAirTankTier.getName());
	}
	
	@Override
	public void getSubItems(@Nonnull final CreativeTabs creativeTab, @Nonnull final NonNullList<ItemStack> list) {
		if (!isInCreativeTab(creativeTab)) {
			return;
		}
		list.add(new ItemStack(this, 1, 0));
		list.add(new ItemStack(this, 1, getMaxDamage()));
	}
	
	@Nonnull
	@Override
	public EnumRarity getRarity(@Nonnull final ItemStack itemStack) {
		return enumAirTankTier.getRarity();
	}
	
	@Override
	public boolean canContainAir(final ItemStack itemStack) {
		if ( itemStack == null
		  || itemStack.getItem() != this ) {
			return false;
		}
		return itemStack.getItemDamage() > 0;
	}
	
	@Override
	public int getMaxAirStorage(final ItemStack itemStack) {
		if ( itemStack == null
		  || itemStack.getItem() != this ) {
			return 0;
		}
		return itemStack.getMaxDamage();
	}
	
	@Override
	public int getCurrentAirStorage(final ItemStack itemStack) {
		if ( itemStack == null
		  || itemStack.getItem() != this ) {
			return 0;
		}
		return getMaxDamage() - itemStack.getItemDamage();
	}
	
	@Override
	public ItemStack consumeAir(final ItemStack itemStack) {
		if ( itemStack == null
		  || itemStack.getItem() != this ) {
			return itemStack;
		}
		itemStack.setItemDamage(Math.min(getMaxDamage(), itemStack.getItemDamage() + 1)); // bypass unbreaking enchantment
		return itemStack;
	}
	
	@Override
	public int getAirTicksPerConsumption(final ItemStack itemStack) {
		if ( itemStack == null
		  || itemStack.getItem() != this ) {
			return 0;
		}
		return WarpDriveConfig.BREATHING_AIR_TANK_BREATH_DURATION_TICKS;
	}
	
	@Override
	public ItemStack getEmptyAirContainer(final ItemStack itemStack) {
		if ( itemStack == null
		  || itemStack.getItem() != this ) {
			return itemStack;
		}
		return new ItemStack(itemStack.getItem(), 1, itemStack.getMaxDamage());
	}
	
	@Override
	public ItemStack getFullAirContainer(final ItemStack itemStack) {
		if ( itemStack == null
		  || itemStack.getItem() != this ) {
			return itemStack;
		}
		return new ItemStack(itemStack.getItem(), 1);
	}
}
