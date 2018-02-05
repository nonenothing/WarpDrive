package cr0s.warpdrive.item;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IAirContainerItem;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.data.EnumComponentType;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemComponent extends Item implements IAirContainerItem {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;
	
	private static ItemStack[] itemStackCache;
	
	public ItemComponent() {
		super();
		setHasSubtypes(true);
		setUnlocalizedName("warpdrive.crafting.component");
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		
		itemStackCache = new ItemStack[EnumComponentType.length];
	}
	
	public static ItemStack getItemStack(EnumComponentType enumComponentType) {
		if (enumComponentType != null) {
			int damage = enumComponentType.ordinal();
			if (itemStackCache[damage] == null) {
				itemStackCache[damage] = new ItemStack(WarpDrive.itemComponent, 1, damage);
			}
			return itemStackCache[damage];
		}
		return null;
	}
	
	public static ItemStack getItemStackNoCache(EnumComponentType enumComponentType, int amount) {
		return new ItemStack(WarpDrive.itemComponent, amount, enumComponentType.ordinal());
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(final IIconRegister iconRegister) {
		icons = new IIcon[EnumComponentType.length];
		for (final EnumComponentType enumComponentType : EnumComponentType.values()) {
			icons[enumComponentType.ordinal()] = iconRegister.registerIcon("warpdrive:component/" + enumComponentType.unlocalizedName);
		}
	}
	
	@Override
	public String getUnlocalizedName(final ItemStack itemStack) {
		int damage = itemStack.getItemDamage();
		if (damage >= 0 && damage < EnumComponentType.length) {
			return "item.warpdrive.crafting." + EnumComponentType.get(damage).unlocalizedName;
		}
		return getUnlocalizedName();
	}
	
	@Override
	public IIcon getIconFromDamage(final int damage) {
		if (damage >= 0 && damage < EnumComponentType.length) {
			return icons[damage];
		}
		return icons[0];
	}
	
	@Override
	public void getSubItems(final Item item, final CreativeTabs creativeTab, final List list) {
		for (final EnumComponentType enumComponentType : EnumComponentType.values()) {
			list.add(new ItemStack(item, 1, enumComponentType.ordinal()));
		}
	}
	
	// IAirContainerItem overrides for empty air canister
	@Override
	public boolean canContainAir(final ItemStack itemStack) {
		return (itemStack.getItem() instanceof ItemComponent && itemStack.getItemDamage() == EnumComponentType.AIR_CANISTER.ordinal());
	}
	
	@Override
	public int getMaxAirStorage(final ItemStack itemStack) {
		if (canContainAir(itemStack)) {
			return WarpDrive.itemAirTanks[0].getMaxAirStorage(itemStack);
		} else {
			return 0;
		}
	}
	
	@Override
	public int getCurrentAirStorage(final ItemStack itemStack) {
		return 0;
	}
	
	@Override
	public ItemStack consumeAir(final ItemStack itemStack) {
		WarpDrive.logger.error(this + " consumeAir() with itemStack " + itemStack);
		throw new RuntimeException("Invalid call to consumeAir() on non or empty container");
	}
	
	@Override
	public int getAirTicksPerConsumption(final ItemStack itemStack) {
		if (canContainAir(itemStack)) {
			return WarpDrive.itemAirTanks[0].getAirTicksPerConsumption(itemStack);
		} else {
			return 0;
		}
	}
	
	@Override
	public ItemStack getFullAirContainer(final ItemStack itemStack) {
		if (canContainAir(itemStack)) {
			return WarpDrive.itemAirTanks[0].getFullAirContainer(itemStack);
		}
		return null;
	}
	
	@Override
	public ItemStack getEmptyAirContainer(final ItemStack itemStack) {
		if (canContainAir(itemStack)) {
			return WarpDrive.itemAirTanks[0].getEmptyAirContainer(itemStack);
		}
		return null;
	}
	
	
	
	@Override
	public boolean doesSneakBypassUse(final World world, final int x, final int y, final int z, final EntityPlayer player) {
		final Block block = world.getBlock(x, y, z);
		
		return block instanceof BlockAbstractContainer
		    || super.doesSneakBypassUse(world, x, y, z, player);
	}
	
	@Override
	public void addInformation(final ItemStack itemStack, final EntityPlayer entityPlayer, final List list, final boolean advancedItemTooltips) {
		super.addInformation(itemStack, entityPlayer, list, advancedItemTooltips);
		
		String tooltip = "";
		switch (EnumComponentType.get(itemStack.getItemDamage())) {
		case AIR_CANISTER:
			tooltip += StatCollector.translateToLocalFormatted("item.warpdrive.crafting.AirCanisterEmpty.tooltip");
			break;
		default:
			break;
		}
		
		Commons.addTooltip(list, tooltip);
	}
}