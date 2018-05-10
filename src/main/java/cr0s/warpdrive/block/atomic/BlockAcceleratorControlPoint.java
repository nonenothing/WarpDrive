package cr0s.warpdrive.block.atomic;

import cr0s.warpdrive.Commons;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockAcceleratorControlPoint extends BlockAbstractAccelerator implements ITileEntityProvider {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;
	
	public BlockAcceleratorControlPoint() {
		super((byte) 1);
		setBlockName("warpdrive.atomic.accelerator_control_point");
		setBlockTextureName("warpdrive:atomic/accelerator_control_point");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		icons = new IIcon[2];
		
		icons[0] = iconRegister.registerIcon(getTextureName() + "-off");
		icons[1] = iconRegister.registerIcon(getTextureName() + "-on");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int metadata) {
		return icons[metadata % 2];
	}
	
	@Override
	public boolean onBlockActivated(final World world, final int x, final int y, final int z,
	                                final EntityPlayer entityPlayer,
	                                final int side, final float hitX, final float hitY, final float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (entityPlayer.getHeldItem() == null) {
			final TileEntity tileEntity = world.getTileEntity(x, y, z);
			
			if (tileEntity instanceof TileEntityAcceleratorControlPoint) {
				Commons.addChatMessage(entityPlayer, ((TileEntityAcceleratorControlPoint) tileEntity).getStatus());
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public TileEntity createNewTileEntity(final World world, final int metadata) {
		return new TileEntityAcceleratorControlPoint();
	}
}
