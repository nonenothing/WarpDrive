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
	public void registerBlockIcons(final IIconRegister iconRegister) {
		blockIcon = iconRegister.registerIcon("warpdrive:passive/highlyAdvancedMachineSide");
	}
	
	@Override
	public Item getItemDropped(final int metadata, final Random random, final int fortune) {
		return Item.getItemFromBlock(this);
	}
	
	@Override
	public int quantityDropped(Random par1Random) {
		return 1;
	}
}