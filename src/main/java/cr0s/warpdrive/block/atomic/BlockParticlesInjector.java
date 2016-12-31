package cr0s.warpdrive.block.atomic;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockParticlesInjector extends BlockAcceleratorControlPoint {
	
	public BlockParticlesInjector() {
		super();
		setBlockName("warpdrive.atomic.particles_injector");
		setBlockTextureName("warpdrive:atomic/particles_injector");
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int p_149915_2_) {
		return new TileEntityParticlesInjector();
	}
}
