package cr0s.warpdrive.block.movement;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.block.BlockAbstractBase;
import cr0s.warpdrive.data.VectorI;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockTransporterScanner extends BlockAbstractBase {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] iconBuffer;
	
	public BlockTransporterScanner() {
		super(Material.iron);
		setBlockName("warpdrive.movement.transporter_scanner");
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
		setLightOpacity(255);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		iconBuffer = new IIcon[4];
		// Solid textures
		iconBuffer[0] = iconRegister.registerIcon("warpdrive:movement/transporter_scanner-bottom");
		iconBuffer[1] = iconRegister.registerIcon("warpdrive:movement/transporter_scanner-side");
		iconBuffer[2] = iconRegister.registerIcon("warpdrive:movement/transporter_scanner-top_offline");
		iconBuffer[3] = iconRegister.registerIcon("warpdrive:movement/transporter_scanner-top_online");
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
	
	@Override
	public int getLightValue(final IBlockAccess blockAccess, final int x, final int y, final int z) {
		final int metadata = blockAccess.getBlockMetadata(x, y, z);
		return metadata == 0 ? 0 : 6;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int metadata) {
		if (side == 0) {
			return iconBuffer[0];
		}
		if (side != 1) {
			return iconBuffer[1];
		}
		
		return iconBuffer[metadata == 0 ? 2 : 3];
	}
	
	public boolean isValid(final World worldObj, final VectorI vScanner) {
		boolean isScannerPosition = true;
		for (int x = vScanner.x - 1; x <= vScanner.x + 1; x++)  {
			for (int z = vScanner.z - 1; z <= vScanner.z + 1; z++) {
				// check base block is containment or scanner in checker pattern
				final Block blockBase = worldObj.getBlock(x, vScanner.y, z);
				if ( !(blockBase instanceof BlockTransporterContainment)
				  && (!isScannerPosition || !(blockBase instanceof BlockTransporterScanner)) ) {
					return false;
				}
				isScannerPosition = !isScannerPosition;
				
				// check 2 above blocks are air
				if (!worldObj.isAirBlock(x, vScanner.y + 1, z)) {
					return false;
				}
				if (!worldObj.isAirBlock(x, vScanner.y + 2, z)) {
					return false;
				}
			}
		}
		return true;
	}
}