package cr0s.warpdrive.block.passive;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import cr0s.warpdrive.WarpDrive;

public class BlockDecorative extends Block {
	public static enum decorativeTypes {
		Plain, Energized, Network
	};
	
	private static ItemStack[] isCache = new ItemStack[decorativeTypes.values().length];
	private static IIcon[] iconBuffer = new IIcon[decorativeTypes.values().length];
	
	public BlockDecorative() {
		super(Material.iron);
		setHardness(0.5f);
		setStepSound(Block.soundTypeMetal);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setBlockName("warpdrive.passive.Plain");
	}
	
	private static boolean isValidDamage(final int damage) {
		return damage >= 0 && damage < decorativeTypes.values().length;
	}
	
	@Override
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
		for (decorativeTypes val : decorativeTypes.values()) {
			par3List.add(new ItemStack(par1, 1, val.ordinal()));
		}
	}
	
	@Override
	public void registerBlockIcons(IIconRegister ir) {
		for (decorativeTypes val : decorativeTypes.values()) {
			iconBuffer[val.ordinal()] = ir.registerIcon("warpdrive:passive/decorative" + val.toString());
		}
	}
	
	@Override
	public IIcon getIcon(int side, int damage) {
		if (isValidDamage(damage)) {
			return iconBuffer[damage];
		}
		return iconBuffer[0];
	}
	
	@Override
	public int damageDropped(int damage) {
		return damage;
	}
	
	public static ItemStack getItemStack(int damage) {
		if (!isValidDamage(damage)) {
			return null;
		}
		
		if (isCache[damage] == null) {
			isCache[damage] = getItemStackNoCache(damage, 1);
		}
		return isCache[damage];
	}
	
	public static ItemStack getItemStackNoCache(int damage, int amount) {
		if (!isValidDamage(damage)) {
			return null;
		}
		
		return new ItemStack(WarpDrive.blockDecorative, amount, damage);
	}
}
