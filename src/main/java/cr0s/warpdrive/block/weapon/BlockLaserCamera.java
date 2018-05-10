package cr0s.warpdrive.block.weapon;

import cr0s.warpdrive.Commons;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.render.ClientCameraHandler;
import cr0s.warpdrive.render.RenderBlockStandard;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockLaserCamera extends BlockAbstractContainer {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] iconBuffer;
	
	private static final int ICON_SIDE = 0;
	
	public BlockLaserCamera() {
		super(Material.iron);
		setHardness(50.0F);
		setResistance(20.0F * 5 / 3);
		setBlockName("warpdrive.weapon.LaserCamera");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		iconBuffer = new IIcon[1];
		// Solid textures
		iconBuffer[ICON_SIDE] = iconRegister.registerIcon("warpdrive:weapon/laserCameraSide");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int metadata) {
		return iconBuffer[ICON_SIDE];
	}
	
	@Override
	public int getRenderType() {
		return RenderBlockStandard.renderId;
	}
	
	@Override
	public TileEntity createNewTileEntity(final World world, final int metadata) {
		return new TileEntityLaserCamera();
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean onBlockActivated(final World world, final int x, final int y, final int z,
	                                final EntityPlayer entityPlayer,
	                                final int side, final float hitX, final float hitY, final float hitZ) {
		if (entityPlayer.getHeldItem() == null) {
			final TileEntity tileEntity = world.getTileEntity(x, y, z);
			if (!ClientCameraHandler.isOverlayEnabled) {
				if (tileEntity instanceof TileEntityLaserCamera) {
					Commons.addChatMessage(entityPlayer, ((TileEntityLaserCamera) tileEntity).getStatus());
				} else {
					Commons.addChatMessage(entityPlayer, StatCollector.translateToLocalFormatted("warpdrive.guide.prefix",
							getLocalizedName()) + StatCollector.translateToLocalFormatted("warpdrive.error.badTileEntity"));
					WarpDrive.logger.error("Block " + this + " with invalid tile entity " + tileEntity);
				}
				return false;
			}
		}
		
		return false;
	}
}