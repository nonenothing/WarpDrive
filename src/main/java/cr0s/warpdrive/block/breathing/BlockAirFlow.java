package cr0s.warpdrive.block.breathing;

import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.data.StateAir;
import cr0s.warpdrive.event.ChunkHandler;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class BlockAirFlow extends BlockAbstractAir {
	
	public BlockAirFlow() {
		super();
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		if (!world.isRemote) {
			final StateAir stateAir = ChunkHandler.getStateAir(world, x, y, z);
			if (!stateAir.isAirFlow() || stateAir.concentration == 0) {
				if (WarpDrive.isDev) {
					WarpDrive.logger.info(String.format("Recovering: AirFlow removal by collision at %s", stateAir));
				}
				world.setBlockToAir(x, y, z);
			}
		}
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}
	
	@Override
	public boolean onBlockEventReceived(World p_149696_1_, int p_149696_2_, int p_149696_3_, int p_149696_4_, int p_149696_5_, int p_149696_6_) {
		return super.onBlockEventReceived(p_149696_1_, p_149696_2_, p_149696_3_, p_149696_4_, p_149696_5_, p_149696_6_);
	}
}