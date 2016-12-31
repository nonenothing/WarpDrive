package cr0s.warpdrive.block.atomic;

public class BlockChiller extends BlockAbstractAccelerator {
	
	public BlockChiller(final byte tier) {
		super(tier);
		setBlockName("warpdrive.atomic.chiller" + tier);
		setBlockTextureName("warpdrive:atomic/chiller" + tier + "-on");
	}
}
