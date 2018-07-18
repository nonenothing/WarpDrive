package cr0s.warpdrive.block.atomic;

import cr0s.warpdrive.data.EnumTier;

public class BlockParticlesCollider extends BlockAbstractAccelerator {
	
	public BlockParticlesCollider(final String registryName, final EnumTier enumTier) {
		super(registryName, enumTier);
		
		setUnlocalizedName("warpdrive.atomic.particles_collider");
	}
}
