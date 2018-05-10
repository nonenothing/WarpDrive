package cr0s.warpdrive.block.energy;

import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.data.EnumReactorFace;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockEnanReactorCore extends BlockAbstractContainer {
	
	@SideOnly(Side.CLIENT)
	IIcon[] iconBuffer;
	
	public BlockEnanReactorCore() {
		super(Material.iron);
		setBlockName("warpdrive.energy.enan_reactor_core");
	}
	
	@Override
	public TileEntity createNewTileEntity(final World world, final int metadata) {
		return new TileEntityEnanReactorCore();
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		iconBuffer = new IIcon[17];
		iconBuffer[16] = iconRegister.registerIcon("warpdrive:energy/enan_reactor_core-top_bottom");
		iconBuffer[ 0] = iconRegister.registerIcon("warpdrive:energy/enan_reactor_core-side00");
		iconBuffer[ 1] = iconRegister.registerIcon("warpdrive:energy/enan_reactor_core-side01");
		iconBuffer[ 2] = iconRegister.registerIcon("warpdrive:energy/enan_reactor_core-side02");
		iconBuffer[ 3] = iconRegister.registerIcon("warpdrive:energy/enan_reactor_core-side03");
		iconBuffer[ 4] = iconRegister.registerIcon("warpdrive:energy/enan_reactor_core-side10");
		iconBuffer[ 5] = iconRegister.registerIcon("warpdrive:energy/enan_reactor_core-side11");
		iconBuffer[ 6] = iconRegister.registerIcon("warpdrive:energy/enan_reactor_core-side12");
		iconBuffer[ 7] = iconRegister.registerIcon("warpdrive:energy/enan_reactor_core-side13");
		iconBuffer[ 8] = iconRegister.registerIcon("warpdrive:energy/enan_reactor_core-side20");
		iconBuffer[ 9] = iconRegister.registerIcon("warpdrive:energy/enan_reactor_core-side21");
		iconBuffer[10] = iconRegister.registerIcon("warpdrive:energy/enan_reactor_core-side22");
		iconBuffer[11] = iconRegister.registerIcon("warpdrive:energy/enan_reactor_core-side23");
		iconBuffer[12] = iconRegister.registerIcon("warpdrive:energy/enan_reactor_core-side30");
		iconBuffer[13] = iconRegister.registerIcon("warpdrive:energy/enan_reactor_core-side31");
		iconBuffer[14] = iconRegister.registerIcon("warpdrive:energy/enan_reactor_core-side32");
		iconBuffer[15] = iconRegister.registerIcon("warpdrive:energy/enan_reactor_core-side33");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final IBlockAccess blockAccess, final int x, final int y, final int z, final int side) {
		final int metadata  = blockAccess.getBlockMetadata(x, y, z);
		if (side == 0 || side == 1) {
			return iconBuffer[16];
		}
		if (metadata >= 0 && metadata < 16) {
			return iconBuffer[metadata];
		}
		return iconBuffer[0];
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int metadata) {
		if (side == 0 || side == 1) {
			return iconBuffer[16];
		}
		return iconBuffer[7];
	}
	
	@Override
	public void breakBlock(final World world, final int x, final int y, final int z, final Block block, final int metadata) {
		super.breakBlock(world, x, y, z, block, metadata);
		
		for (final EnumReactorFace reactorFace : EnumReactorFace.values()) {
			if (reactorFace.indexStability < 0) {
				continue;
			}
			
			final TileEntity tileEntity = world.getTileEntity(
				x + reactorFace.x,
				y + reactorFace.y,
				z + reactorFace.z);
			if (tileEntity instanceof TileEntityEnanReactorLaser) {
				if (((TileEntityEnanReactorLaser) tileEntity).getReactorFace() == reactorFace) {
					((TileEntityEnanReactorLaser) tileEntity).setReactorFace(EnumReactorFace.UNKNOWN, null);
				}
			}
		}
	}
	
	@Override
	public byte getTier(final ItemStack itemStack) {
		return 3;
	}
}