package cr0s.warpdrive.item;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IAirContainerItem;
import cr0s.warpdrive.block.energy.BlockEnergyBank;
import cr0s.warpdrive.data.EnumComponentType;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemComponent extends ItemAbstractBase implements IAirContainerItem {
	
	private static ItemStack[] itemStackCache;
	
	public ItemComponent(final String registryName) {
		super(registryName);
		setHasSubtypes(true);
		setUnlocalizedName("warpdrive.component");
		
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
	
	@Nonnull
	@Override
	public String getUnlocalizedName(final ItemStack itemStack) {
		int damage = itemStack.getItemDamage();
		if (damage >= 0 && damage < EnumComponentType.length) {
			return "item.warpdrive.component." + EnumComponentType.get(damage).getUnlocalizedName();
		}
		return getUnlocalizedName();
	}
	
	@Override
	public void getSubItems(@Nonnull final Item item, @Nonnull final CreativeTabs creativeTabs, @Nonnull final List<ItemStack> subItems) {
		for(EnumComponentType enumComponentType : EnumComponentType.values()) {
			subItems.add(new ItemStack(item, 1, enumComponentType.ordinal()));
		}
	}
	
	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public ModelResourceLocation getModelResourceLocation(final ItemStack itemStack) {
		int damage = itemStack.getItemDamage();
		ResourceLocation resourceLocation = getRegistryName();
		if (damage >= 0 && damage < EnumComponentType.length) {
			resourceLocation = new ResourceLocation(resourceLocation.getResourceDomain(), resourceLocation.getResourcePath() + "-" + EnumComponentType.get(damage).getUnlocalizedName());
		}
		return new ModelResourceLocation(resourceLocation, "inventory");
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
	public boolean doesSneakBypassUse(final ItemStack itemStack, final IBlockAccess world, final BlockPos blockPos, final EntityPlayer player) {
		final Block block = world.getBlockState(blockPos).getBlock();
		
		return block instanceof BlockEnergyBank
		    || super.doesSneakBypassUse(itemStack, world, blockPos, player);
	}
	
	@Override
	public void addInformation(final ItemStack itemStack, final EntityPlayer entityPlayer, final List<String> list, final boolean advancedItemTooltips) {
		super.addInformation(itemStack, entityPlayer, list, advancedItemTooltips);
		
		String tooltip = "";
		switch (EnumComponentType.get(itemStack.getItemDamage())) {
		case AIR_CANISTER:
			tooltip += new TextComponentTranslation("item.warpdrive.component.airCanisterEmpty.tooltip").getFormattedText();
			break;
		default:
			break;
		}
		
		Commons.addTooltip(list, tooltip);
	}
}