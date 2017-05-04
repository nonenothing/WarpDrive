package cr0s.warpdrive.block.atomic;

import net.minecraft.block.Block;
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
	public boolean shouldSideBeRendered(IBlockState blockState, @Nonnull IBlockAccess world, @Nonnull BlockPos blockPos, EnumFacing side) {
		if (world.isAirBlock(blockPos)) {
			return true;
		}
		Block sideBlock = world.getBlockState(blockPos).getBlock();
		return !(sideBlock instanceof BlockElectromagnetGlass);
	}
}
