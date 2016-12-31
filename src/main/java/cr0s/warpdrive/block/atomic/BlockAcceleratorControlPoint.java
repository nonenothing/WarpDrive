package cr0s.warpdrive.block.atomic;

import cr0s.warpdrive.WarpDrive;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockAcceleratorControlPoint extends BlockAbstractAccelerator implements ITileEntityProvider {
		
	public BlockAcceleratorControlPoint() {
		super((byte) 1);
		setBlockName("warpdrive.atomic.accelerator_control_point");
		setBlockTextureName("warpdrive:atomic/accelerator_control_point");
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (entityPlayer.getHeldItem() == null) {
			TileEntity tileEntity = world.getTileEntity(x, y, z);
			
			if (tileEntity instanceof TileEntityAcceleratorControlPoint) {
				WarpDrive.addChatMessage(entityPlayer, ((TileEntityAcceleratorControlPoint)tileEntity).getStatus());
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int p_149915_2_) {
		return new TileEntityAcceleratorControlPoint();
	}
}
