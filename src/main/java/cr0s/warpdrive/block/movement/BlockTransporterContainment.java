package cr0s.warpdrive.block.movement;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.block.BlockAbstractBase;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockTransporterContainment extends BlockAbstractBase {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] iconBuffer;
	
	public BlockTransporterContainment() {
		super(Material.iron);
		setBlockName("warpdrive.movement.transporter_containment");
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		iconBuffer = new IIcon[3];
		// Solid textures
		iconBuffer[0] = iconRegister.registerIcon("warpdrive:movement/transporter_containment-bottom");
		iconBuffer[1] = iconRegister.registerIcon("warpdrive:movement/transporter_containment-top");
		iconBuffer[2] = iconRegister.registerIcon("warpdrive:movement/transporter_containment-side");
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean isNormalCube() {
		return false;
	}
	
	@Override
	public boolean isSideSolid(final IBlockAccess blockAccess, final int x, final int y, final int z, final ForgeDirection side) {
		return side == ForgeDirection.DOWN;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int metadata) {
		if (side == 0 || side == 1) {
			return iconBuffer[side];
		}
		
		return iconBuffer[2];
	}
}