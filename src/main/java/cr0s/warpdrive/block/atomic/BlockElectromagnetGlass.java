package cr0s.warpdrive.block.atomic;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockElectromagnetGlass extends BlockElectromagnetPlain {
	
	public BlockElectromagnetGlass(final byte tier) {
		super(tier);
		setBlockName("warpdrive.atomic.electromagnet" + tier + ".glass");
		setBlockTextureName("warpdrive:atomic/electromagnet");
	}
	
	@Override
	public void registerBlockIcons(IIconRegister iconRegister) {
		iconSide = iconRegister.registerIcon("warpdrive:atomic/electromagnet" + tier + "_glass-side");
		iconTop = iconRegister.registerIcon("warpdrive:atomic/electromagnet" + tier + "_glass-top");
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public int getRenderBlockPass() {
		return 1;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(IBlockAccess blockAccess, int x, int y, int z, int side) {
		if (blockAccess.isAirBlock(x, y, z)) {
			return true;
		}
		Block sideBlock = blockAccess.getBlock(x, y, z);
		return !(sideBlock instanceof BlockElectromagnetGlass);
	}
}
