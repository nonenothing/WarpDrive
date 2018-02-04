package cr0s.warpdrive.block.passive;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.data.EnumDecorativeType;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockDecorative extends Block {
	
	@SideOnly(Side.CLIENT)
	private static IIcon[] icons;
	
	private static ItemStack[] itemStackCache;
	
	public BlockDecorative() {
		super(Material.iron);
		setHardness(1.5f);
		setStepSound(Block.soundTypeMetal);
		setBlockName("warpdrive.decoration.decorative.plain");
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		
		itemStackCache = new ItemStack[EnumDecorativeType.length];
	}
	
	@Override
	public void getSubBlocks(Item item, CreativeTabs creativeTabs, List list) {
		for (final EnumDecorativeType enumDecorativeType : EnumDecorativeType.values()) {
			list.add(new ItemStack(item, 1, enumDecorativeType.ordinal()));
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		icons = new IIcon[EnumDecorativeType.length];
		for (final EnumDecorativeType enumDecorativeType : EnumDecorativeType.values()) {
			icons[enumDecorativeType.ordinal()] = iconRegister.registerIcon("warpdrive:decoration/decorative-" + enumDecorativeType.unlocalizedName);
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int damage) {
		if (damage >= 0 && damage < EnumDecorativeType.length) {
			return icons[damage];
		}
		return icons[0];
	}
	
	@Override
	public int damageDropped(int damage) {
		return damage;
	}
	
	public static ItemStack getItemStack(EnumDecorativeType enumDecorativeType) {
		if (enumDecorativeType != null) {
			int damage = enumDecorativeType.ordinal();
			if (itemStackCache[damage] == null) {
				itemStackCache[damage] = new ItemStack(WarpDrive.blockDecorative, 1, damage);
			}
			return itemStackCache[damage];
		}
		return null;
	}
	
	public static ItemStack getItemStackNoCache(EnumDecorativeType enumDecorativeType, int amount) {
		return new ItemStack(WarpDrive.blockDecorative, amount, enumDecorativeType.ordinal());
	}
}
