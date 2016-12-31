package cr0s.warpdrive.block.atomic;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public class BlockElectromagnetPlain extends BlockAbstractAccelerator {
	
	protected IIcon iconSide;
	protected IIcon iconTop;
	
	public BlockElectromagnetPlain(final byte tier) {
		super(tier);
		setBlockName("warpdrive.atomic.electromagnet" + tier + ".plain");
	}
	
	@Override
	public void registerBlockIcons(IIconRegister iconRegister) {
		iconSide = iconRegister.registerIcon("warpdrive:atomic/electromagnet" + tier + "_plain-side");
		iconTop = iconRegister.registerIcon("warpdrive:atomic/electromagnet" + tier + "_plain-top");
	}
	
	@Override
	public IIcon getIcon(int side, int metadata) {
		return side == 0 || side == 1 ? iconTop : iconSide;
	}
}
