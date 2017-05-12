package cr0s.warpdrive.block.passive;

import cr0s.warpdrive.WarpDrive;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockHighlyAdvancedMachine extends Block {
	
	public BlockHighlyAdvancedMachine() {
		super(Material.iron);
		setHardness(5.0F);
		setStepSound(Block.soundTypeMetal);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setBlockName("warpdrive.passive.HighlyAdvancedMachine");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister iconRegister) {
		blockIcon = iconRegister.registerIcon("warpdrive:passive/highlyAdvancedMachineSide");
	}
	
	@Override
	public Item getItemDropped(int var1, Random var2, int var3) {
		return Item.getItemFromBlock(this);
	}
	
	@Override
	public int quantityDropped(Random par1Random) {
		return 1;
	}
}