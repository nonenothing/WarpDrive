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
	
	//	icons[ 0] = iconRegister.registerIcon("warpdrive:breathing/air_canister");
	//	icons[ 1] = iconRegister.registerIcon("warpdrive:breathing/air_canister");
	//	icons[ 2] = iconRegister.registerIcon("warpdrive:breathing/air_canister");
	//	icons[ 3] = iconRegister.registerIcon("warpdrive:breathing/air_canister");
	//	icons[ 4] = iconRegister.registerIcon("warpdrive:breathing/air_canister");
	//	icons[ 5] = iconRegister.registerIcon("warpdrive:breathing/air_canister");
	//	icons[ 6] = iconRegister.registerIcon("warpdrive:breathing/air_tank1-0");
	//	icons[ 7] = iconRegister.registerIcon("warpdrive:breathing/air_tank1-20");
	//	icons[ 8] = iconRegister.registerIcon("warpdrive:breathing/air_tank1-40");
	//	icons[ 9] = iconRegister.registerIcon("warpdrive:breathing/air_tank1-60");
	//	icons[10] = iconRegister.registerIcon("warpdrive:breathing/air_tank1-80");
	//	icons[11] = iconRegister.registerIcon("warpdrive:breathing/air_tank1-100");
	//	icons[12] = iconRegister.registerIcon("warpdrive:breathing/air_tank2-0");
	//	icons[13] = iconRegister.registerIcon("warpdrive:breathing/air_tank2-20");
	//	icons[14] = iconRegister.registerIcon("warpdrive:breathing/air_tank2-40");
	//	icons[15] = iconRegister.registerIcon("warpdrive:breathing/air_tank2-60");
	//	icons[16] = iconRegister.registerIcon("warpdrive:breathing/air_tank2-80");
	//	icons[17] = iconRegister.registerIcon("warpdrive:breathing/air_tank2-100");
	//	icons[18] = iconRegister.registerIcon("warpdrive:breathing/air_tank3-0");
	//	icons[19] = iconRegister.registerIcon("warpdrive:breathing/air_tank3-20");
	//	icons[20] = iconRegister.registerIcon("warpdrive:breathing/air_tank3-40");
	//	icons[21] = iconRegister.registerIcon("warpdrive:breathing/air_tank3-60");
	//	icons[22] = iconRegister.registerIcon("warpdrive:breathing/air_tank3-80");
	//	icons[23] = iconRegister.registerIcon("warpdrive:breathing/air_tank3-100");
	//	final double ratio = 1.0D - damage / (double) getMaxDamage();
	//	final int offset = (ratio <= 0.0) ? 0 : (ratio < 0.2) ? 1 : (ratio < 0.4) ? 2 : (ratio < 0.6) ? 3 : (ratio < 0.8) ? 4 : 5;
	//	return icons[Math.min(icons.length, offset + tier * 6)];
	
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
