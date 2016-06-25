package cr0s.warpdrive.item;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.forcefield.BlockForceFieldProjector;
import cr0s.warpdrive.block.forcefield.BlockForceFieldRelay;
import cr0s.warpdrive.data.EnumForceFieldUpgrade;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

public class ItemForceFieldUpgrade extends Item {
	private final IIcon[] icons;
	private static ItemStack[] itemStackCache;
	
	public ItemForceFieldUpgrade() {
		super();
		setHasSubtypes(true);
		setUnlocalizedName("warpdrive.forcefield.upgrade");
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		
		icons = new IIcon[EnumForceFieldUpgrade.length];
		itemStackCache = new ItemStack[EnumForceFieldUpgrade.length];
	}
	
	public static ItemStack getItemStack(EnumForceFieldUpgrade enumForceFieldUpgrade) {
		if (enumForceFieldUpgrade != null) {
			int damage = enumForceFieldUpgrade.ordinal();
			if (itemStackCache[damage] == null) {
				itemStackCache[damage] = new ItemStack(WarpDrive.itemForceFieldUpgrade, 1, damage);
			}
			return itemStackCache[damage];
		}
		return null;
	}
	
	public static ItemStack getItemStackNoCache(EnumForceFieldUpgrade enumForceFieldUpgrade, int amount) {
		return new ItemStack(WarpDrive.itemForceFieldUpgrade, amount, enumForceFieldUpgrade.ordinal());
	}
	
	@Override
	public void registerIcons(IIconRegister par1IconRegister) {
		for(EnumForceFieldUpgrade enumForceFieldUpgrade : EnumForceFieldUpgrade.values()) {
			icons[enumForceFieldUpgrade.ordinal()] = par1IconRegister.registerIcon("warpdrive:forcefield/upgrade_" + enumForceFieldUpgrade.unlocalizedName);
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
		for(EnumForceFieldUpgrade enumForceFieldUpgrade : EnumForceFieldUpgrade.values()) {
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
		
		String tooltipName1 = getUnlocalizedName(itemStack) + ".tooltip";
		if (StatCollector.canTranslate(tooltipName1)) {
			WarpDrive.addTooltip(list, StatCollector.translateToLocalFormatted(tooltipName1));
		}
		
		String tooltipName2 = getUnlocalizedName() + ".tooltip";
		if ((!tooltipName1.equals(tooltipName2)) && StatCollector.canTranslate(tooltipName2)) {
			WarpDrive.addTooltip(list, StatCollector.translateToLocalFormatted(tooltipName2));
		}
		
		WarpDrive.addTooltip(list, "\n");
		
		EnumForceFieldUpgrade enumForceFieldUpgrade = EnumForceFieldUpgrade.get(itemStack.getItemDamage());
		if (enumForceFieldUpgrade.maxCountOnProjector > 0) {
			WarpDrive.addTooltip(list, StatCollector.translateToLocalFormatted("item.warpdrive.forcefield.upgrade.tooltip.usage.projector"));
		}
		if (enumForceFieldUpgrade.maxCountOnRelay > 0) {
			WarpDrive.addTooltip(list, StatCollector.translateToLocalFormatted("item.warpdrive.forcefield.upgrade.tooltip.usage.relay"));
		}
		WarpDrive.addTooltip(list, StatCollector.translateToLocalFormatted("item.warpdrive.forcefield.upgrade.tooltip.usage.dismount"));
	}
}
