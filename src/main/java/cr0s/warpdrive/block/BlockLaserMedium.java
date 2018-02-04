package cr0s.warpdrive.block;

import cr0s.warpdrive.Commons;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockLaserMedium extends BlockAbstractContainer {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] iconBuffer;
	
	public BlockLaserMedium() {
		super(Material.iron);
		setBlockName("warpdrive.machines.LaserMedium");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		iconBuffer = new IIcon[9];
		iconBuffer[0] = iconRegister.registerIcon("warpdrive:laser_medium-side0");
		iconBuffer[1] = iconRegister.registerIcon("warpdrive:laser_medium-side1");
		iconBuffer[2] = iconRegister.registerIcon("warpdrive:laser_medium-side2");
		iconBuffer[3] = iconRegister.registerIcon("warpdrive:laser_medium-side3");
		iconBuffer[4] = iconRegister.registerIcon("warpdrive:laser_medium-side4");
		iconBuffer[5] = iconRegister.registerIcon("warpdrive:laser_medium-side5");
		iconBuffer[6] = iconRegister.registerIcon("warpdrive:laser_medium-side6");
		iconBuffer[7] = iconRegister.registerIcon("warpdrive:laser_medium-side7");
		iconBuffer[8] = iconRegister.registerIcon("warpdrive:laser_medium-top_bottom");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final IBlockAccess blockAccess, final int x, final int y, final int z, final int side) {
		final int metadata  = blockAccess.getBlockMetadata(x, y, z);
		if (side == 0 || side == 1) {
			return iconBuffer[8];
		}
		
		return iconBuffer[Math.min(metadata, 7)];
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int metadata) {
		if (side == 0 || side == 1) {
			return iconBuffer[8];
		}
		
		return iconBuffer[6];
	}
	
	@Override
	public TileEntity createNewTileEntity(final World world, final int metadata) {
		return new TileEntityLaserMedium();
	}
	
	@Override
	public boolean onBlockActivated(final World world, final int x, final int y, final int z, final EntityPlayer entityPlayer,
	                                final int side, final float hitX, final float hitY, final float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (entityPlayer.getHeldItem() == null) {
			final TileEntity tileEntity = world.getTileEntity(x, y, z);
			if (tileEntity instanceof TileEntityLaserMedium) {
				Commons.addChatMessage(entityPlayer, ((TileEntityLaserMedium) tileEntity).getStatus());
				return true;
			}
		}
		
		return false;
	}
}
