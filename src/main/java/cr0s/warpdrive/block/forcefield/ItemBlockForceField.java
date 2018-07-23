package cr0s.warpdrive.block.forcefield;

import cr0s.warpdrive.block.ItemBlockAbstractBase;
import net.minecraft.block.Block;

public class ItemBlockForceField extends ItemBlockAbstractBase {
	
	public ItemBlockForceField(final Block block) {
		super(block);
		
		setMaxDamage(0);
		setHasSubtypes(true);
	}
}
