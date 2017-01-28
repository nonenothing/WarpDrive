package cr0s.warpdrive.block.atomic;

import cr0s.warpdrive.WarpDrive;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockParticlesInjector extends BlockAcceleratorControlPoint {
	
	public BlockParticlesInjector(final String registryName) {
		super(registryName);
		setUnlocalizedName("warpdrive.atomic.particles_injector");
		GameRegistry.registerTileEntity(TileEntityParticlesInjector.class, WarpDrive.MODID + ":blockParticlesInjector");
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int p_149915_2_) {
		return new TileEntityParticlesInjector();
	}
}
