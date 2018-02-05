package cr0s.warpdrive.block.atomic;

import cr0s.warpdrive.WarpDrive;

import javax.annotation.Nonnull;

import net.minecraftforge.fml.common.registry.GameRegistry;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockParticlesInjector extends BlockAcceleratorControlPoint {
	// @TODO: add on/off textures and states
	
	public BlockParticlesInjector(final String registryName) {
		super(registryName, (byte) 1);
		setUnlocalizedName("warpdrive.atomic.particles_injector");
		GameRegistry.registerTileEntity(TileEntityParticlesInjector.class, WarpDrive.MODID + ":blockParticlesInjector");
	}
	
	@Nonnull
	@Override
	public TileEntity createNewTileEntity(@Nonnull final World world, final int metadata) {
		return new TileEntityParticlesInjector();
	}
}
