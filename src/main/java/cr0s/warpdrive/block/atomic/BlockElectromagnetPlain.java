package cr0s.warpdrive.block.atomic;

	
public class BlockElectromagnetPlain extends BlockAbstractAccelerator {
	
	public BlockElectromagnetPlain(final String registryName, final byte tier) {
		super(registryName, tier);
		setUnlocalizedName("warpdrive.atomic.electromagnet" + tier + ".plain");
	}
}
