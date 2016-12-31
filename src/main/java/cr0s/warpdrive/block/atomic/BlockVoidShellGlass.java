package cr0s.warpdrive.block.atomic;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;

public class BlockVoidShellGlass extends BlockVoidShellPlain {
	
	public BlockVoidShellGlass() {
		super();
		setBlockName("warpdrive.atomic.void_shell_glass");
		setBlockTextureName("warpdrive:atomic/void_shell_glass");
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
