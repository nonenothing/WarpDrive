package cr0s.warpdrive.block.movement;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.block.BlockAbstractContainer;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockTransporterCore extends BlockAbstractContainer {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] iconBuffer;
	
	public BlockTransporterCore() {
		super(Material.iron);
		setBlockName("warpdrive.movement.transporter_core");
	}
	
	@Override
	public TileEntity createNewTileEntity(final World world, final int metadata) {
		return new TileEntityTransporterCore();
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		iconBuffer = new IIcon[5];
		iconBuffer[0] = iconRegister.registerIcon("warpdrive:movement/transporter_core-bottom_top");
		iconBuffer[1] = iconRegister.registerIcon("warpdrive:movement/transporter_core-side_invalid");
		iconBuffer[2] = iconRegister.registerIcon("warpdrive:movement/transporter_core-side_offline");
		iconBuffer[3] = iconRegister.registerIcon("warpdrive:movement/transporter_core-side_low_power");
		iconBuffer[4] = iconRegister.registerIcon("warpdrive:movement/transporter_core-side_online");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int metadata) {
		if (side == 0 || side == 1) {
			return iconBuffer[0];
		}
		
		return iconBuffer[(metadata % 4) + 1];
	}
	
	@Override
	public boolean onBlockActivated(final World world, final int x, final int y, final int z, final EntityPlayer entityPlayer,
	                                final int side, final float hitX, final float hitY, final float hitZ) {
		if (world.isRemote) {
			return false;
		}
		
		if (entityPlayer.getHeldItem() == null) {
			final TileEntity tileEntity = world.getTileEntity(x, y, z);
			if (tileEntity instanceof TileEntityTransporterCore) {
				Commons.addChatMessage(entityPlayer, ((TileEntityTransporterCore) tileEntity).getStatus());
				return true;
			}
		}
		
		return super.onBlockActivated(world, x, y, z, entityPlayer, side, hitX, hitY, hitZ);
	}
}