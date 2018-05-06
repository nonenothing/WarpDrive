package cr0s.warpdrive.block.detection;

import cr0s.warpdrive.WarpDrive;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockWarpIsolation extends Block {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] iconBuffer;

	public BlockWarpIsolation() {
		super(Material.iron);
		setHardness(3.5F);
		setStepSound(Block.soundTypeMetal);
		setCreativeTab(WarpDrive.creativeTabWarpDrive);
		setBlockName("warpdrive.detection.warp_isolation");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		iconBuffer = new IIcon[1];
		iconBuffer[0] = iconRegister.registerIcon("warpdrive:detection/warpIsolation");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int metadata) {
		return iconBuffer[0];
	}
	
	@Override
	public int quantityDropped(final Random random) {
		return 1;
	}
	
	@Override
	public Item getItemDropped(final int metadata, final Random random, final int fortune) {
			return Item.getItemFromBlock(this);
	}
}