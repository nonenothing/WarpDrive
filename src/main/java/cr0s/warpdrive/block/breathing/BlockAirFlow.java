package cr0s.warpdrive.block.breathing;

import cr0s.warpdrive.data.StateAir;
import cr0s.warpdrive.event.ChunkHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockAirFlow extends BlockAbstractAir {
	
	public BlockAirFlow(final String registryName) {
		super(registryName);
	}
	
	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(final IBlockState blockState, @Nonnull final World world, @Nonnull final BlockPos blockPos) {
		if (!world.isRemote) {
			final StateAir stateAir = ChunkHandler.getStateAir(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
			if ( stateAir != null
			  && (!stateAir.isAirFlow() || stateAir.concentration == 0) ) {
				world.setBlockToAir(blockPos);
			}
		}
		return super.getCollisionBoundingBox(blockState, world, blockPos);
	}
}