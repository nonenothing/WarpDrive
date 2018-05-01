package cr0s.warpdrive.block.passive;

import cr0s.warpdrive.WarpDrive;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockIridium extends Block {
	
	public BlockIridium() {
		super(Material.iron);
		setHardness(3.4F);
		setResistance(360.0F * 5 / 3);
		setStepSound(Block.soundTypeMetal);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setBlockName("warpdrive.passive.IridiumBlock");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		blockIcon = iconRegister.registerIcon("warpdrive:passive/iridiumSide");
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