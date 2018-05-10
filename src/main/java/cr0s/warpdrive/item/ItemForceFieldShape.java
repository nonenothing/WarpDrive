package cr0s.warpdrive.item;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.forcefield.BlockForceFieldProjector;
import cr0s.warpdrive.block.forcefield.BlockForceFieldRelay;
import cr0s.warpdrive.data.EnumForceFieldShape;

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

public class ItemForceFieldShape extends ItemAbstractBase {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;
	
	private static ItemStack[] itemStackCache;
	
	public ItemForceFieldShape() {
		super();
		setHasSubtypes(true);
		setUnlocalizedName("warpdrive.forcefield.shape");
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		
		itemStackCache = new ItemStack[EnumForceFieldShape.length];
	}
	
	public static ItemStack getItemStack(final EnumForceFieldShape forceFieldShape) {
		if (forceFieldShape != null) {
			final int damage = forceFieldShape.ordinal();
			if (itemStackCache[damage] == null) {
				itemStackCache[damage] = new ItemStack(WarpDrive.itemForceFieldShape, 1, damage);
			}
			return itemStackCache[damage];
		}
		return null;
	}
	
	public static ItemStack getItemStackNoCache(final EnumForceFieldShape forceFieldShape, final int amount) {
		return new ItemStack(WarpDrive.itemForceFieldShape, amount, forceFieldShape.ordinal());
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(final IIconRegister iconRegister) {
		icons = new IIcon[EnumForceFieldShape.length];
		for (final EnumForceFieldShape enumForceFieldShape : EnumForceFieldShape.values()) {
			icons[enumForceFieldShape.ordinal()] = iconRegister.registerIcon("warpdrive:forcefield/shape_" + enumForceFieldShape.unlocalizedName);
		}
	}
	
	@Override
	public String getUnlocalizedName(final ItemStack itemStack) {
		final int damage = itemStack.getItemDamage();
		if (damage >= 0 && damage < EnumForceFieldShape.length) {
			return getUnlocalizedName() + "." + EnumForceFieldShape.get(damage).unlocalizedName;
		}
		return getUnlocalizedName();
	}
	
	@Override
	public IIcon getIconFromDamage(final int damage) {
		if (damage >= 0 && damage < EnumForceFieldShape.length) {
			return icons[damage];
		}
		return icons[0];
	}
	
	@Override
	public void getSubItems(final Item item, final CreativeTabs creativeTab, final List list) {
		for (final EnumForceFieldShape enumForceFieldShape : EnumForceFieldShape.values()) {
			if (enumForceFieldShape != EnumForceFieldShape.NONE) {
				list.add(new ItemStack(item, 1, enumForceFieldShape.ordinal()));
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
		
		Commons.addTooltip(list, StatCollector.translateToLocalFormatted("item.warpdrive.forcefield.shape.tooltip.usage"));
	}
}