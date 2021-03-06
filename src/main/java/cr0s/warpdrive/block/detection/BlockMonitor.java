package cr0s.warpdrive.block.detection;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IVideoChannel;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.data.CameraRegistryItem;
import cr0s.warpdrive.render.ClientCameraHandler;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockMonitor extends BlockAbstractContainer {
	
	@SideOnly(Side.CLIENT)
	private IIcon iconFront;
	@SideOnly(Side.CLIENT)
	private IIcon iconSide;
	
	public BlockMonitor() {
		super(Material.iron);
		isRotating = true;
		setBlockName("warpdrive.detection.monitor");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		iconFront = iconRegister.registerIcon("warpdrive:detection/monitor-front");
		iconSide = iconRegister.registerIcon("warpdrive:detection/monitor-side");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final IBlockAccess blockAccess, final int x, final int y, final int z, final int side) {
		final int metadata  = blockAccess.getBlockMetadata(x, y, z);
		return side == metadata ? iconFront : iconSide;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int metadata) {
		return side == 3 ? iconFront : iconSide;
	}
	
	@Override
	public TileEntity createNewTileEntity(final World world, final int metadata) {
		return new TileEntityMonitor();
	}
	
	@Override
	public boolean onBlockActivated(final World world, final int x, final int y, final int z,
	                                final EntityPlayer entityPlayer,
	                                final int side, final float hitX, final float hitY, final float hitZ) {
		// Monitor is only reacting client side
		if (!world.isRemote) {
			return false;
		}
		
		if (entityPlayer.getHeldItem() == null) {
			final TileEntity tileEntity = world.getTileEntity(x, y, z);
			
			if (tileEntity instanceof TileEntityMonitor) {
				// validate video channel
				final int videoChannel = ((TileEntityMonitor) tileEntity).getVideoChannel();
				if ( videoChannel < IVideoChannel.VIDEO_CHANNEL_MIN
				  || videoChannel > IVideoChannel.VIDEO_CHANNEL_MAX ) {
					Commons.addChatMessage(entityPlayer, ((TileEntityMonitor) tileEntity).getStatus());
					return true;
				}
				
				// validate camera
				final CameraRegistryItem camera = WarpDrive.cameras.getCameraByVideoChannel(world, videoChannel);
				if ( camera == null
				  || entityPlayer.isSneaking() ) {
					Commons.addChatMessage(entityPlayer, ((TileEntityMonitor) tileEntity).getStatus());
					return true;
				}
				
				Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.monitor.viewing_camera",
						videoChannel,
						camera.position.chunkPosX,
						camera.position.chunkPosY,
						camera.position.chunkPosZ ));
				ClientCameraHandler.setupViewpoint(
						camera.type, entityPlayer, entityPlayer.rotationYaw, entityPlayer.rotationPitch,
						x, y, z, this,
						camera.position.chunkPosX, camera.position.chunkPosY, camera.position.chunkPosZ, world.getBlock(camera.position.chunkPosX, camera.position.chunkPosY, camera.position.chunkPosZ));
			}
		}
		
		return false;
	}
}