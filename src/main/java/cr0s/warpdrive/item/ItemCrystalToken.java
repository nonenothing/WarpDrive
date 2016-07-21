package cr0s.warpdrive.item;

import cr0s.warpdrive.WarpDrive;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import java.util.List;

public class ItemCrystalToken extends Item {	
	private IIcon[] icons;
	private static ItemStack[] itemStackCache;
	private static int COUNT = 6; 
	
	public ItemCrystalToken() {
		super();
		setHasSubtypes(true);
		setUnlocalizedName("warpdrive.tool.crystalToken");
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		
		icons = new IIcon[COUNT];
		itemStackCache = new ItemStack[COUNT];
	}
	
	public static ItemStack getItemStack(final int damage) {
		if (damage < COUNT) {
			if (itemStackCache[damage] == null) {
				itemStackCache[damage] = new ItemStack(WarpDrive.itemCrystalToken, 1, damage);
			}
			return itemStackCache[damage];
		}
		return null;
	}
	
	public static ItemStack getItemStackNoCache(final int damage, int amount) {
		if (damage < COUNT) {
			return new ItemStack(WarpDrive.itemCrystalToken, amount, damage);
		}
		return new ItemStack(WarpDrive.itemCrystalToken, amount, 0);
	}
	
	@Override
	public void registerIcons(IIconRegister par1IconRegister) {
		for(int damage = 0; damage < COUNT; damage++) {
			icons[damage] = par1IconRegister.registerIcon("warpdrive:tool/crystalToken" + damage);
		}
	}
	
	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		int damage = itemStack.getItemDamage();
		if (damage >= 0 && damage < COUNT) {
			return "item.warpdrive.tool.crystalToken" + damage;
		}
		return getUnlocalizedName();
	}
	
	@Override
	public IIcon getIconFromDamage(int damage) {
		if (damage >= 0 && damage < COUNT) {
			return icons[damage];
		}
		return icons[0];
	}
	
	@Override
	public void getSubItems(Item item, CreativeTabs creativeTab, List list) {
		for(int damage = 0; damage < COUNT; damage++) {
			list.add(new ItemStack(item, 1, damage));
		}
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
	}
}