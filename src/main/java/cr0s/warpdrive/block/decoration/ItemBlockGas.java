package cr0s.warpdrive.block.decoration;

import cr0s.warpdrive.block.ItemBlockAbstractBase;
import net.minecraft.block.Block;

public class ItemBlockGas extends ItemBlockAbstractBase {
	
	public ItemBlockGas(final Block block) {
		super(block);
		
		setHasSubtypes(true);
	}
}
