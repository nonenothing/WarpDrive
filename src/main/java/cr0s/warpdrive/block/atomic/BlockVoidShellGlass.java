package cr0s.warpdrive.block.atomic;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class BlockVoidShellGlass extends BlockVoidShellPlain {
	
	public BlockVoidShellGlass(final String registryName) {
		super(registryName);
		setUnlocalizedName("warpdrive.atomic.void_shell_glass");
	}
	
	@SuppressWarnings("deprecation")
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos blockPos, EnumFacing side) {
		final IBlockState blockStateSide = blockAccess.getBlockState(blockPos);
		final Block blockSide = blockStateSide.getBlock();
		if (blockSide.isAir(blockStateSide, blockAccess, blockPos)) {
			return true;
		}
		return !(blockSide instanceof BlockElectromagnetGlass);
	}
}
