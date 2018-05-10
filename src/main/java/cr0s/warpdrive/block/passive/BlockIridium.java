package cr0s.warpdrive.block.passive;

import cr0s.warpdrive.block.BlockAbstractBase;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockIridium extends BlockAbstractBase {
	
	public BlockIridium() {
		super(Material.iron);
		setHardness(3.4F);
		setResistance(360.0F * 5 / 3);
		setBlockName("warpdrive.passive.iridium_block");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		blockIcon = iconRegister.registerIcon("warpdrive:passive/iridium_block-side");
	}
}