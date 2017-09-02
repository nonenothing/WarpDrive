package cr0s.warpdrive.block.atomic;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class BlockElectromagnetGlass extends BlockElectromagnetPlain {
	
	
	public BlockElectromagnetGlass(final String registryName, final byte tier) {
		super(registryName, tier);
		setUnlocalizedName("warpdrive.atomic.electromagnet" + tier + ".glass");
	}
	
	@SuppressWarnings("deprecation")
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos blockPos, EnumFacing side) {
		final IBlockState blockStateSide = blockAccess.getBlockState(blockPos);
		if (blockStateSide.getBlock().isAir(blockStateSide, blockAccess, blockPos)) {
			return true;
		}
		return !(blockStateSide.getBlock() instanceof BlockElectromagnetGlass);
	}
}
