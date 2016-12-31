package cr0s.warpdrive.block.atomic;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.world.IBlockAccess;

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
	
	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
		if (world.isAirBlock(x, y, z)) {
			return true;
		}
		Block sideBlock = world.getBlock(x, y, z);
		return !(sideBlock instanceof BlockElectromagnetGlass);
	}
}
