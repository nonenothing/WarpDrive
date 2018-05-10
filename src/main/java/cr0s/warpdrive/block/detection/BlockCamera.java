package cr0s.warpdrive.block.detection;

import cr0s.warpdrive.block.BlockAbstractContainer;
import cr0s.warpdrive.render.RenderBlockStandard;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCamera extends BlockAbstractContainer {
	
	@SideOnly(Side.CLIENT)
	private IIcon[] iconBuffer;
	
	private static final int ICON_SIDE = 0;
	
	public BlockCamera() {
		super(Material.iron);
		setBlockName("warpdrive.detection.camera");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(final IIconRegister iconRegister) {
		iconBuffer = new IIcon[1];
		// Solid textures
		iconBuffer[ICON_SIDE] = iconRegister.registerIcon("warpdrive:detection/cameraSide");
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
		return new TileEntityCamera();
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
}