package cr0s.warpdrive.item;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.forcefield.BlockForceFieldProjector;
import cr0s.warpdrive.block.forcefield.BlockForceFieldRelay;
import cr0s.warpdrive.data.EnumForceFieldUpgrade;

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

public class ItemForceFieldUpgrade extends ItemAbstractBase {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;
	
	private static ItemStack[] itemStackCache;
	
	public ItemForceFieldUpgrade() {
		super();
		setHasSubtypes(true);
		setUnlocalizedName("warpdrive.forcefield.upgrade");
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		
		itemStackCache = new ItemStack[EnumForceFieldUpgrade.length];
	}
	
	public static ItemStack getItemStack(final EnumForceFieldUpgrade forceFieldUpgrade) {
		if (forceFieldUpgrade != null) {
			final int damage = forceFieldUpgrade.ordinal();
			if (itemStackCache[damage] == null) {
				itemStackCache[damage] = new ItemStack(WarpDrive.itemForceFieldUpgrade, 1, damage);
			}
			return itemStackCache[damage];
		}
		return null;
	}
	
	public static ItemStack getItemStackNoCache(final EnumForceFieldUpgrade forceFieldUpgrade, final int amount) {
		return new ItemStack(WarpDrive.itemForceFieldUpgrade, amount, forceFieldUpgrade.ordinal());
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(final IIconRegister iconRegister) {
		icons = new IIcon[EnumForceFieldUpgrade.length];
		for (final EnumForceFieldUpgrade enumForceFieldUpgrade : EnumForceFieldUpgrade.values()) {
			icons[enumForceFieldUpgrade.ordinal()] = iconRegister.registerIcon("warpdrive:forcefield/upgrade_" + enumForceFieldUpgrade.unlocalizedName);
		}
	}
	
	@Override
	public String getUnlocalizedName(final ItemStack itemStack) {
		final int damage = itemStack.getItemDamage();
		if (damage >= 0 && damage < EnumForceFieldUpgrade.length) {
			return getUnlocalizedName() + "." + EnumForceFieldUpgrade.get(damage).unlocalizedName;
		}
		return getUnlocalizedName();
	}
	
	@Override
	public IIcon getIconFromDamage(final int damage) {
		if (damage >= 0 && damage < EnumForceFieldUpgrade.length) {
			return icons[damage];
		}
		return icons[0];
	}
	
	@Override
	public void getSubItems(final Item item, final CreativeTabs creativeTab, final List list) {
		for (final EnumForceFieldUpgrade enumForceFieldUpgrade : EnumForceFieldUpgrade.values()) {
			if (enumForceFieldUpgrade != EnumForceFieldUpgrade.NONE) {
				list.add(new ItemStack(item, 1, enumForceFieldUpgrade.ordinal()));
			}
		}
	}
	
	@Override
	public boolean doesSneakBypassUse(final World world, final int x, final int y, final int z, final EntityPlayer player) {
		final Block block = world.getBlock(x, y, z);
		return block instanceof BlockForceFieldRelay || block instanceof BlockForceFieldProjector || super.doesSneakBypassUse(world, x, y, z, player);
	}
	
	@Override
	public void addInformation(final ItemStack itemStack, final EntityPlayer entityPlayer, final List list, final boolean advancedItemTooltips) {
		super.addInformation(itemStack, entityPlayer, list, advancedItemTooltips);
		
		Commons.addTooltip(list, "\n");
		
		final EnumForceFieldUpgrade forceFieldUpgrade = EnumForceFieldUpgrade.get(itemStack.getItemDamage());
		if (forceFieldUpgrade.maxCountOnProjector > 0) {
			Commons.addTooltip(list, StatCollector.translateToLocalFormatted("item.warpdrive.forcefield.upgrade.tooltip.usage.projector"));
		}
		if (forceFieldUpgrade.maxCountOnRelay > 0) {
			Commons.addTooltip(list, StatCollector.translateToLocalFormatted("item.warpdrive.forcefield.upgrade.tooltip.usage.relay"));
		}
		Commons.addTooltip(list, StatCollector.translateToLocalFormatted("item.warpdrive.forcefield.upgrade.tooltip.usage.dismount"));
	}
}
