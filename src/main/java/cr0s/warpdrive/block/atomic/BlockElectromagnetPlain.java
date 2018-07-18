package cr0s.warpdrive.block.atomic;

import cr0s.warpdrive.data.EnumTier;

public class BlockElectromagnetPlain extends BlockAbstractAccelerator {
	
	public BlockElectromagnetPlain(final String registryName, final EnumTier enumTier) {
		super(registryName, enumTier);
		
		setUnlocalizedName("warpdrive.atomic.electromagnet" + enumTier.getIndex() + ".plain");
	}
}
