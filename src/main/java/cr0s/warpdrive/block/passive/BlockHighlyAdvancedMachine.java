package cr0s.warpdrive.block.passive;

import cr0s.warpdrive.block.BlockAbstractBase;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockHighlyAdvancedMachine extends BlockAbstractBase {
	
	public BlockHighlyAdvancedMachine() {
		super(Material.iron);
		setHardness(5.0F);
		setBlockName("warpdrive.passive.HighlyAdvancedMachine");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		blockIcon = iconRegister.registerIcon("warpdrive:passive/highlyAdvancedMachineSide");
	}
}