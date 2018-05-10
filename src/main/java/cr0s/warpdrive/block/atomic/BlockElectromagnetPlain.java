package cr0s.warpdrive.block.atomic;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockElectromagnetPlain extends BlockAbstractAccelerator {
	
	@SideOnly(Side.CLIENT)
	protected IIcon iconSide;
	@SideOnly(Side.CLIENT)
	protected IIcon iconTop;
	
	public BlockElectromagnetPlain(final byte tier) {
		super(tier);
		setBlockName("warpdrive.atomic.electromagnet" + tier + ".plain");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		iconSide = iconRegister.registerIcon("warpdrive:atomic/electromagnet" + tier + "_plain-side");
		iconTop = iconRegister.registerIcon("warpdrive:atomic/electromagnet" + tier + "_plain-top");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int metadata) {
		return side == 0 || side == 1 ? iconTop : iconSide;
	}
}
