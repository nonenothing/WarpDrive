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

public class ItemForceFieldUpgrade extends Item {
	
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
			int damage = forceFieldUpgrade.ordinal();
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
	public void registerIcons(IIconRegister iconRegister) {
		icons = new IIcon[EnumForceFieldUpgrade.length];
		for (final EnumForceFieldUpgrade enumForceFieldUpgrade : EnumForceFieldUpgrade.values()) {
			icons[enumForceFieldUpgrade.ordinal()] = iconRegister.registerIcon("warpdrive:forcefield/upgrade_" + enumForceFieldUpgrade.unlocalizedName);
		}
	}
	
	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		int damage = itemStack.getItemDamage();
		if (damage >= 0 && damage < EnumForceFieldUpgrade.length) {
			return getUnlocalizedName() + "." + EnumForceFieldUpgrade.get(damage).unlocalizedName;
		}
		return getUnlocalizedName();
	}
	
	@Override
	public IIcon getIconFromDamage(int damage) {
		if (damage >= 0 && damage < EnumForceFieldUpgrade.length) {
			return icons[damage];
		}
		return icons[0];
	}
	
	@Override
	public void getSubItems(Item item, CreativeTabs creativeTab, List list) {
		for (final EnumForceFieldUpgrade enumForceFieldUpgrade : EnumForceFieldUpgrade.values()) {
			if (enumForceFieldUpgrade != EnumForceFieldUpgrade.NONE) {
				list.add(new ItemStack(item, 1, enumForceFieldUpgrade.ordinal()));
			}
		}
	}
	
	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
		Block block = world.getBlock(x, y, z);
		return block instanceof BlockForceFieldRelay || block instanceof BlockForceFieldProjector || super.doesSneakBypassUse(world, x, y, z, player);
	}
	
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean advancedItemTooltips) {
		super.addInformation(itemStack, entityPlayer, list, advancedItemTooltips);
		
		final String tooltipName1 = getUnlocalizedName(itemStack) + ".tooltip";
		if (StatCollector.canTranslate(tooltipName1)) {
			Commons.addTooltip(list, StatCollector.translateToLocalFormatted(tooltipName1));
		}
		
		final String tooltipName2 = getUnlocalizedName() + ".tooltip";
		if ((!tooltipName1.equals(tooltipName2)) && StatCollector.canTranslate(tooltipName2)) {
			Commons.addTooltip(list, StatCollector.translateToLocalFormatted(tooltipName2));
		}
		
		Commons.addTooltip(list, "\n");
		
		EnumForceFieldUpgrade forceFieldUpgrade = EnumForceFieldUpgrade.get(itemStack.getItemDamage());
		if (forceFieldUpgrade.maxCountOnProjector > 0) {
			Commons.addTooltip(list, StatCollector.translateToLocalFormatted("item.warpdrive.forcefield.upgrade.tooltip.usage.projector"));
		}
		if (forceFieldUpgrade.maxCountOnRelay > 0) {
			Commons.addTooltip(list, StatCollector.translateToLocalFormatted("item.warpdrive.forcefield.upgrade.tooltip.usage.relay"));
		}
		Commons.addTooltip(list, StatCollector.translateToLocalFormatted("item.warpdrive.forcefield.upgrade.tooltip.usage.dismount"));
	}
}
